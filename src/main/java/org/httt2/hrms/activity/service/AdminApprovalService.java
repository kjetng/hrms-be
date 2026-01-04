package org.httt2.hrms.activity.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.activity.dto.approval.PendingActivityDTO;
import org.httt2.hrms.activity.dto.approval.PendingCampaignDTO;
import org.httt2.hrms.activity.entity.CampaignParticipant;
import org.httt2.hrms.activity.entity.EmployeeActivity;
import org.httt2.hrms.activity.entity.id.CampaignParticipantId;
import org.httt2.hrms.activity.repository.CampaignParticipantRepository;
import org.httt2.hrms.activity.repository.CampaignRepository;
import org.httt2.hrms.activity.repository.EmployeeActivityRepository;
import org.httt2.hrms.auth.entity.User;
import org.httt2.hrms.auth.repository.UserRepository;
import org.httt2.hrms.common.email.SendEmailEvent; // Import Event Email
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import RabbitTemplate
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.httt2.hrms.common.external.employee.EmployeeRepository; // Import Repo External
import org.httt2.hrms.common.external.employee.dto.EmployeeResponse; // Import DTO

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminApprovalService {

    private final CampaignRepository campaignRepository;
    private final EmployeeActivityRepository activityRepository;
    private final CampaignParticipantRepository participantRepository;
    private final UserRepository userRepository; // Dùng để lấy email User
    private final ObjectMapper objectMapper; // Spring Boot tự inject cái này

    private final EmployeeRepository employeeRepository; 

    // Inject RabbitTemplate để bắn event
    private final RabbitTemplate rabbitTemplate;

    // Lấy tên queue từ file application.properties
    @Value("${rabbitmq.queue.send-email}")
    private String emailQueue;

    // 1. Lấy danh sách Campaign có bài pending
    public List<PendingCampaignDTO> getCampaignsWithPending() {
        return campaignRepository.findCampaignsWithPendingActivities();
    }

    // 2. Lấy chi tiết bài pending
    public List<PendingActivityDTO> getPendingActivities(Long campaignId) {
        List<EmployeeActivity> activities = activityRepository.findPendingByCampaignId(campaignId);

        return activities.stream().map(activity -> {
            // Tìm User để lấy email hiển thị
            Optional<User> userOpt = userRepository.findByEmpId(activity.getEmpId());

            String empEmail = userOpt.map(User::getEmail).orElse("Unknown");
            String empName = "Employee #" + activity.getEmpId(); // Tạm thời dùng ID vì User entity chưa có field Name

            return PendingActivityDTO.builder()
                    .id(activity.getActivityId())
                    .employeeName(empName) 
                    .employeeEmail(empEmail)
                    .submittedDate(activity.getCreatedAt())
                    .activityDate(activity.getActivityDate())
                    .metrics(activity.getMetrics())
                    .proofImage(activity.getProofImage())
                    .status(activity.getStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    // 3. APPROVE ACTIVITY
    @Transactional
    public void approveActivity(Long activityId) {
        EmployeeActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        if (!"pending".equalsIgnoreCase(activity.getStatus())) {
            throw new RuntimeException("Activity is not pending, cannot approve.");
        }

        // A. Cập nhật trạng thái
        activity.setStatus("approved");
        activityRepository.save(activity);

        // B. Cộng điểm vào bảng CampaignParticipant
        updateParticipantScore(activity);

        // C. Gửi email thông báo
        String subject = "✅ Activity Approved: " + activity.getCampaign().getCampaignName();
        String content = String.format(
            "<h3>Congratulations!</h3>" +
            "<p>Your activity submitted on <b>%s</b> has been <b>APPROVED</b>.</p>" +
            "<p>The distance has been added to your total progress.</p>" +
            "<hr/><p><i>HRMS System</i></p>",
            activity.getActivityDate()
        );
        sendNotificationEmail(activity.getEmpId(), subject, content);
    }

    // 4. REJECT ACTIVITY
    @Transactional
    public void rejectActivity(Long activityId, String reason) {
        EmployeeActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        if (!"pending".equalsIgnoreCase(activity.getStatus())) {
            throw new RuntimeException("Activity is not pending, cannot reject.");
        }

        activity.setStatus("rejected");
        activity.setRejectionReason(reason);
        activityRepository.save(activity);

        // GỬI EMAIL THÔNG BÁO (Reject)
        String subject = "❌ Activity Rejected: " + activity.getCampaign().getCampaignName();
        String content = String.format(
            "<h3>Action Required</h3>" +
            "<p>Your activity submitted on <b>%s</b> has been <b>REJECTED</b>.</p>" +
            "<p style='color:red;'><b>Reason: %s</b></p>" +
            "<p>Please review the reason, edit your submission, and resubmit.</p>" +
            "<hr/><p><i>HRMS System</i></p>",
            activity.getActivityDate(),
            reason
        );
        sendNotificationEmail(activity.getEmpId(), subject, content);
    }

    // Helper: Logic parse JSON và cộng điểm
    private void updateParticipantScore(EmployeeActivity activity) {
        try {
            // 1. Lấy distance từ JSON string metrics
            // Ví dụ metrics: {"distance": 10.5, "duration": 60}
            double distanceToAdd = 0.0;
            if (activity.getMetrics() != null) {
                JsonNode node = objectMapper.readTree(activity.getMetrics());
                if (node.has("distance")) {
                    distanceToAdd = node.get("distance").asDouble();
                }
            }
            
            if (distanceToAdd > 0) {
                // 2. Tìm bản ghi tham gia chiến dịch của nhân viên
                CampaignParticipantId id = new CampaignParticipantId(activity.getEmpId(), activity.getCampaign().getCampaignId());
                Optional<CampaignParticipant> participantOpt = participantRepository.findById(id);

                if (participantOpt.isPresent()) {
                    CampaignParticipant p = participantOpt.get();
                    Double currentScore = (p.getCurrentScore() == null) ? 0.0 : p.getCurrentScore();
                    
                    // 3. Cộng dồn và lưu
                    p.setCurrentScore(currentScore + distanceToAdd);
                    participantRepository.save(p);
                    
                    log.info("Updated Score for Emp {}: {} + {} = {}", 
                            activity.getEmpId(), currentScore, distanceToAdd, p.getCurrentScore());
                }
            }
        } catch (Exception e) {
            log.error("Failed to update participant score for activity: " + activity.getActivityId(), e);
            // Không throw exception để tránh rollback việc approve activity
            // (Tùy business rule, nếu bắt buộc cộng điểm thành công mới approve thì throw)
        }
    }

    // Helper: Logic gửi RabbitMQ Event
    private void sendNotificationEmail(Long empId, String subject, String htmlContent) {
        try {
            log.info("--- START SENDING EMAIL PROCESS for EmpID: {} ---", empId); // 1. Bắt đầu

            // Gọi sang .NET
            EmployeeResponse employee = employeeRepository.getById(empId);
            log.info("Response from .NET: {}", employee); // 2. Xem kết quả trả về từ .NET

            if (employee != null && employee.personalEmail() != null) {
                String emailToSend = employee.personalEmail();
                log.info("Found email: {}", emailToSend); // 3. Tìm thấy email

                SendEmailEvent event = SendEmailEvent.builder()
                        .emailToSend(emailToSend)
                        .subject(subject)
                        .htmlContent(htmlContent)
                        .build();

                // Bắn RabbitMQ
                rabbitTemplate.convertAndSend(emailQueue, event);
                log.info(">>> SENT to RabbitMQ Queue: {}", emailQueue); // 4. Đã gửi vào hàng đợi
            } else {
                log.warn("!!! Email NOT FOUND or Employee is NULL for EmpID: {}", empId);
            }
        } catch (Exception e) {
            log.error("!!! ERROR sending notification email", e); // 5. Có lỗi xảy ra
        }
    }
}