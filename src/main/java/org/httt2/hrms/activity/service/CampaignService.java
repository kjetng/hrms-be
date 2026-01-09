package org.httt2.hrms.activity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.activity.entity.Campaign;
import org.httt2.hrms.activity.repository.CampaignRepository;
import org.springframework.stereotype.Service;
import org.httt2.hrms.activity.dto.CampaignCreateRequest;

import org.httt2.hrms.auth.repository.UserRepository;

import org.httt2.hrms.activity.entity.CampaignParticipant;
import org.httt2.hrms.activity.repository.CampaignParticipantRepository;
import org.springframework.transaction.annotation.Transactional;

import org.httt2.hrms.activity.entity.EmployeeActivity;
import org.httt2.hrms.activity.repository.EmployeeActivityRepository;
import org.httt2.hrms.activity.dto.ActivitySubmissionRequest;

import org.httt2.hrms.activity.dto.leaderboard.LeaderboardEntryDTO;
import org.httt2.hrms.activity.dto.leaderboard.MyRankInfoDTO;
import org.httt2.hrms.common.external.employee.EmployeeRepository;
import org.httt2.hrms.common.external.employee.dto.EmployeeResponse;

import org.httt2.hrms.activity.entity.ParticipantStatus;

import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import RabbitTemplate
import org.springframework.beans.factory.annotation.Value; // Import Value

import org.httt2.hrms.common.email.SendEmailEvent; // Import Event Email
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import RabbitTemplate

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignParticipantRepository participantRepository;
    private final UserRepository userRepository;

    private final EmployeeActivityRepository activityRepository;
    private final EmployeeRepository employeeRepository;
    private final ObjectMapper objectMapper;

    // INJECT THÊM RabbitTemplate
    private final RabbitTemplate rabbitTemplate;

    // Lấy tên Queue từ config
    @Value("${rabbitmq.queue.send-email}")
    private String emailQueue;

    public List<Campaign> getAllCampaigns() {
        log.info("Fetching all campaigns");
        return campaignRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Campaign> getCampaignsByStatus(String status) {
        log.info("Fetching campaigns by status: {}", status);
        return campaignRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Campaign> searchCampaigns(String searchTerm) {
        log.info("Searching campaigns with term: {}", searchTerm);
        return campaignRepository.searchCampaigns(searchTerm);
    }

    public Optional<Campaign> getCampaignById(Long campaignId) {
        log.info("Fetching campaign by ID: {}", campaignId);
        return campaignRepository.findByCampaignId(campaignId);
    }

    // Sửa lại hàm này để nhận tham số empId (có thể null)
    public List<Campaign> getActiveCampaigns(Long empId) {
        if (empId != null) {
            log.info("Fetching active campaigns available for empId: {}", empId);
            // Dùng hàm query mới để loại bỏ cả JOINED và LEFT
            return campaignRepository.findAvailableCampaignsForEmployee(empId);
        }
        
        // Fallback cho trường hợp guest hoặc admin xem chung
        log.info("Fetching all active campaigns (Guest mode)");
        return campaignRepository.findByStatusOrderByCreatedAtDesc("active");
    }

    public Long getTotalCampaigns() {
        return campaignRepository.count();
    }

    public Campaign createCampaign(CampaignCreateRequest request) {
        log.info("Creating campaign: {}", request.getCampaignName());

        // 1. Validate Business Logic
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // 2. Tự động xác định Primary Metric dựa trên Activity Type
        String metric = determinePrimaryMetric(request.getCampaignType());

        // 3. Map DTO -> Entity
        Campaign campaign = Campaign.builder()
                .campaignName(request.getCampaignName())
                .description(request.getDescription())
                .campaignType(request.getCampaignType())
                .primaryMetric(metric) // Tự động set
                .targetGoal(request.getTargetGoal() != null ? request.getTargetGoal() : 100.0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .imageUrl(request.getImageUrl())
                // Status sẽ được @PrePersist trong Entity xử lý thành 'draft',
                // hoặc bạn có thể set logic 'active' nếu startDate là hôm nay tại đây.
                .status("draft")
                .build();

        return campaignRepository.save(campaign);
    }

    public Campaign updateCampaign(Long id, CampaignCreateRequest request) {
        log.info("Updating campaign ID: {}", id);

        // 1. Tìm campaign cũ, nếu không thấy thì báo lỗi
        Campaign existingCampaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));

        // 2. Validate Logic (Ngày kết thúc không được trước ngày bắt đầu)
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // 3. Cập nhật thông tin từ Request vào Entity
        existingCampaign.setCampaignName(request.getCampaignName());
        existingCampaign.setDescription(request.getDescription());
        existingCampaign.setCampaignType(request.getCampaignType());
        existingCampaign.setStartDate(request.getStartDate());
        existingCampaign.setEndDate(request.getEndDate());
        existingCampaign.setStartTime(request.getStartTime());
        existingCampaign.setEndTime(request.getEndTime());

        if (request.getTargetGoal() != null) {
            existingCampaign.setTargetGoal(request.getTargetGoal());
        }

        // 4. Cập nhật ảnh (Chỉ cập nhật nếu Frontend có gửi chuỗi khác rỗng/null)
        if (request.getImageUrl() != null) {
            // Nếu frontend gửi chuỗi rỗng "" nghĩa là user muốn xóa ảnh -> set null
            // Nếu frontend gửi link s3 -> set link đó
            existingCampaign.setImageUrl(request.getImageUrl().isEmpty() ? null : request.getImageUrl());
        }

        // 5. Tính toán lại Primary Metric nếu user đổi loại activity
        existingCampaign.setPrimaryMetric(determinePrimaryMetric(request.getCampaignType()));

        // 6. Cập nhật Status dựa trên ngày tháng mới (Optional - Logic thông minh)
        // Ví dụ: Nếu sửa ngày bắt đầu thành tương lai -> status về 'draft' hoặc 'upcoming'
        // Ở đây mình giữ nguyên logic đơn giản là lưu lại thôi.
        
        return campaignRepository.save(existingCampaign);
    }

    @Transactional
    public Campaign publishCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
                
        if (!"draft".equalsIgnoreCase(campaign.getStatus())) {
            throw new IllegalStateException("Only draft campaigns can be published");
        }
        
        campaign.setStatus("active");
        Campaign savedCampaign = campaignRepository.save(campaign);
        
        // Gọi hàm gửi email
        notifyAllEmployeesAboutNewCampaign(savedCampaign);
        
        return savedCampaign;
    }
    
    private void notifyAllEmployeesAboutNewCampaign(Campaign campaign) {
        // Chạy trong Thread mới để không làm admin phải chờ lâu (vì phải gọi API chi tiết nhiều lần)
        new Thread(() -> {
            try {
                log.info("--- START NOTIFYING EMPLOYEES (Fetching details for personalEmail) ---");
                
                // 1. Lấy danh sách sơ bộ (chỉ có ID, Name, chưa có Personal Email)
                List<EmployeeResponse> basicList = employeeRepository.getAllEmployees();
                
                if (basicList == null || basicList.isEmpty()) {
                    log.warn("No employees found.");
                    return;
                }
                
                String subject = "🚀 New Campaign Published: " + campaign.getCampaignName();
                String htmlContent = String.format(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                    "<h2 style='color: #2c3e50;'>🎯 New Campaign Published!</h2>" +
                    "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                    "<h3 style='color: #27ae60; margin-top: 0;'>%s</h3>" +
                    "<p><strong>Description:</strong> %s</p>" +
                    "<p><strong>Type:</strong> %s | <strong>Goal:</strong> %s</p>" +
                    "<p><strong>Duration:</strong> %s to %s</p>" +
                    "</div>" +
                    "<p>Log in to HRMS to join now!</p>" +
                    "</div>",
                    campaign.getCampaignName(),
                    campaign.getDescription() != null ? campaign.getDescription() : "",
                    campaign.getCampaignType(),
                    campaign.getTargetGoal(),
                    campaign.getStartDate(),
                    campaign.getEndDate()
                );
                
                int count = 0;
                
                // 2. Duyệt qua từng nhân viên để lấy Email thật
                for (EmployeeResponse basicEmp : basicList) {
                    try {
                        // QUAN TRỌNG: Gọi API chi tiết để lấy personalEmail
                        // Vì bên .NET chỉ API chi tiết mới trả về trường này
                        EmployeeResponse fullEmpInfo = employeeRepository.getOneById(basicEmp.id());
                        
                        if (fullEmpInfo != null && fullEmpInfo.personalEmail() != null && !fullEmpInfo.personalEmail().isBlank()) {
                            
                            SendEmailEvent event = SendEmailEvent.builder()
                                    .emailToSend(fullEmpInfo.personalEmail()) // Dùng email cá nhân lấy từ chi tiết
                                    .subject(subject)
                                    .htmlContent(htmlContent)
                                    .build();
                            
                            rabbitTemplate.convertAndSend(emailQueue, event);
                            count++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to fetch/notify employee ID: " + basicEmp.id(), e);
                    }
                }
                
                log.info(">>> Queued {} notification emails.", count);
                
            } catch (Exception e) {
                log.error("Error in notification thread", e);
            }
        }).start();
    }

    // Helper method để xác định đơn vị đo lường
    private String determinePrimaryMetric(String type) {
        if (type == null)
            return "Points";
        switch (type.toLowerCase()) {
            case "walking":
            case "running":
            case "cycling":
                return "Distance (km)";
            default:
                return "Points";
        }
    }

    @Transactional
    public void registerForCampaign(Long campaignId, String userEmail, Long empId) {
        // 1. Tìm Campaign
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if (!"active".equalsIgnoreCase(campaign.getStatus())) {
            throw new RuntimeException("Cannot register. Campaign is not active.");
        }

        // 2. Validate User
        userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User account not found"));
        if (empId == null) throw new RuntimeException("Account not linked to employee profile.");

        // 3. CHECK LOGIC: Đã từng tham gia chưa?
        Optional<CampaignParticipant> existing = participantRepository.findByEmpIdAndCampaignId(empId, campaignId);

        if (existing.isPresent()) {
            CampaignParticipant p = existing.get();
            // Nếu đã LEFT -> Chặn không cho Join lại (theo Business Rule)
            if (p.getStatus() == ParticipantStatus.LEFT) {
                throw new RuntimeException("You cannot rejoin this campaign after leaving.");
            }
            // Nếu đang JOINED -> Báo đã tham gia
            if (p.getStatus() == ParticipantStatus.JOINED) {
                throw new RuntimeException("You have already registered for this campaign.");
            }
        }

        // 4. Tạo mới (Nếu chưa từng có record)
        CampaignParticipant participant = CampaignParticipant.builder()
                .empId(empId)
                .campaignId(campaignId)
                .campaign(campaign)
                .joinedAt(LocalDateTime.now())
                .status(ParticipantStatus.JOINED) // Set trạng thái JOINED
                .currentScore(0.0)
                .build();

        participantRepository.save(participant);
    }

    // EMPLOYEE: Rời khỏi Campaign
    // ... Imports

    @Transactional
    public void leaveCampaign(Long campaignId, Long empId) {
        // 1. Kiểm tra Campaign
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        if ("completed".equalsIgnoreCase(campaign.getStatus()) || "closed".equalsIgnoreCase(campaign.getStatus())) {
            throw new RuntimeException("Cannot leave a campaign that is already closed or completed.");
        }

        // 2. Tìm record tham gia
        CampaignParticipant participant = participantRepository.findByEmpIdAndCampaignId(empId, campaignId)
                .orElseThrow(() -> new RuntimeException("You are not a participant of this campaign."));

        if (participant.getStatus() == ParticipantStatus.LEFT) {
            throw new RuntimeException("You have already left this campaign.");
        }

        // 3. XỬ LÝ RỜI CHIẾN DỊCH (Tối ưu hóa nhờ @Formula)
        
        // A. Cập nhật trạng thái và Reset điểm cá nhân
        // Việc reset currentScore về 0 sẽ làm cho @Formula totalDistance tự động giảm khi query lại
        participant.setStatus(ParticipantStatus.LEFT);
        participant.setCurrentScore(0.0); 
        participantRepository.save(participant);

        // B. Xóa sạch lịch sử hoạt động (Hard Delete)
        activityRepository.deleteAllByCampaign_CampaignIdAndEmpId(campaignId, empId);
        // Vì Entity Campaign dùng @Formula, nó sẽ tự tính lại chính xác khi API gọi GET danh sách.
        
        log.info("Employee {} left campaign {}. Activities deleted and score reset.", empId, campaignId);
    }

    //  EMPLOYEE: Lấy danh sách Campaign mà nhân viên ĐÃ đăng ký
    public List<Campaign> getMyCampaigns(String userEmail, Long empId) {
        userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        if (empId == null) return List.of();

        // Chỉ lấy những campaign đang ở trạng thái JOINED
        List<CampaignParticipant> participants = participantRepository.findByEmpIdAndStatus(empId, ParticipantStatus.JOINED);

        return participants.stream()
                .map(CampaignParticipant::getCampaign)
                .toList();
    }

    @Transactional
    public EmployeeActivity submitActivity(Long campaignId, Long empId, ActivitySubmissionRequest request) {
        // a. Tìm Campaign
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // b. Validate: Ngày hoạt động phải nằm trong thời gian diễn ra Campaign
        if (request.getActivityDate().isBefore(campaign.getStartDate()) || 
            request.getActivityDate().isAfter(campaign.getEndDate())) {
            throw new IllegalArgumentException("Activity date must be within campaign duration (" 
                + campaign.getStartDate() + " to " + campaign.getEndDate() + ")");
        }

        // c. Validate: Employee đã join campaign chưa? (Optional)
        if (!participantRepository.existsByEmpIdAndCampaignId(empId, campaignId)) {
            throw new RuntimeException("You must join the campaign before submitting activities.");
        }

        // d. Tạo Entity và Lưu
        EmployeeActivity activity = EmployeeActivity.builder()
                .empId(empId)
                .campaign(campaign)
                .activityDate(request.getActivityDate())
                .metrics(request.getMetrics())
                .proofImage(request.getProofImage())
                .status("pending") // Luôn là pending khi mới submit
                .build();

        return activityRepository.save(activity);
    }

    // 2. Get My Submissions History Logic
    public List<EmployeeActivity> getMyCampaignActivities(Long campaignId, Long empId) {
        return activityRepository.findByCampaign_CampaignIdAndEmpIdOrderByCreatedAtDesc(campaignId, empId);
    }

    // 4. Xóa Activity đã submit (Chỉ khi status là pending)
    @Transactional
    public void deleteActivity(Long activityId, Long empId) {
        // 1. Tìm Activity
        EmployeeActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        // 2. Validate: Có phải của chính nhân viên này không?
        if (!activity.getEmpId().equals(empId)) {
            throw new RuntimeException("Unauthorized: You do not own this activity.");
        }

        // 3. Validate: Status phải là 'pending'
        if (!"pending".equalsIgnoreCase(activity.getStatus())) {
            throw new RuntimeException("Cannot delete processed activity (Status: " + activity.getStatus() + ")");
        }

        // 4. Xóa
        activityRepository.delete(activity);
        log.info("Deleted activity {} by emp {}", activityId, empId);
    }

    // 3. Cập nhật Activity đã submit (Chỉ khi status là pending)
    @Transactional
    public EmployeeActivity updateActivity(Long activityId, Long empId, ActivitySubmissionRequest request) {
        // 1. Tìm Activity
        EmployeeActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        // 2. Validate Owner (Quyền sở hữu)
        if (!activity.getEmpId().equals(empId)) {
            throw new RuntimeException("Unauthorized: You do not own this activity.");
        }

        // 3. Validate Status (Chỉ cho sửa khi còn pending)
        if (!"pending".equalsIgnoreCase(activity.getStatus())) {
            throw new RuntimeException("Cannot edit processed activity (Status: " + activity.getStatus() + ")");
        }

        // 4. Validate Date Range (Giống hàm submit)
        Campaign campaign = activity.getCampaign(); // Lấy chiến dịch từ activity
        
        if (request.getActivityDate().isBefore(campaign.getStartDate()) || 
            request.getActivityDate().isAfter(campaign.getEndDate())) {
            throw new IllegalArgumentException("Activity date must be within campaign duration (" 
                + campaign.getStartDate() + " to " + campaign.getEndDate() + ")");
        }

        // 5. Cập nhật thông tin
        activity.setActivityDate(request.getActivityDate());
        activity.setMetrics(request.getMetrics());
        
        // Nếu có ảnh mới thì update
        if (request.getProofImage() != null && !request.getProofImage().isEmpty()) {
            activity.setProofImage(request.getProofImage());
        }

        // 6. Lưu lại
        return activityRepository.save(activity);
    }
    

    // ----------------------------------------------------------------
    // 1. LẤY BẢNG XẾP HẠNG (ĐÃ FIX LOGIC TÍNH TỔNG)
    // ----------------------------------------------------------------
    public List<LeaderboardEntryDTO> getLeaderboard(Long campaignId) {
        // A. Lấy tất cả activity đã APPROVE
        List<EmployeeActivity> activities = activityRepository.findByCampaign_CampaignIdAndStatus(campaignId, "approved");

        // B. Tính tổng điểm (Group by EmployeeID)
        Map<Long, LeaderboardEntryDTO> statsMap = new HashMap<>();

        for (EmployeeActivity act : activities) {
            double distance = parseDistance(act.getMetrics()); 
            
            statsMap.compute(act.getEmpId(), (k, v) -> {
                // 1. Nếu chưa có thì khởi tạo mới
                if (v == null) {
                    v = LeaderboardEntryDTO.builder()
                            .employeeId(k)
                            .totalPoints(0.0) // Khởi tạo bằng 0
                            .completedActivities(0)
                            .lastActivityDate(act.getActivityDate().atStartOfDay())
                            .build();
                }

                // 2. QUAN TRỌNG: Luôn cộng dồn distance (kể cả vừa mới tạo xong)
                v.setTotalPoints(v.getTotalPoints() + distance);
                v.setCompletedActivities(v.getCompletedActivities() + 1);
                
                // 3. Cập nhật ngày hoạt động gần nhất
                if (act.getCreatedAt().isAfter(v.getLastActivityDate())) {
                    v.setLastActivityDate(act.getCreatedAt());
                }
                
                return v;
            });
        }

        // C. Sắp xếp điểm từ cao -> thấp
        List<LeaderboardEntryDTO> leaderboard = new ArrayList<>(statsMap.values());
        leaderboard.sort(Comparator.comparingDouble(LeaderboardEntryDTO::getTotalPoints).reversed());

        // D. Gán Rank và Gọi .NET lấy tên
        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntryDTO entry = leaderboard.get(i);
            entry.setRank(i + 1);
            
            // Làm tròn 2 số thập phân
            double roundedPoints = Math.round(entry.getTotalPoints() * 100.0) / 100.0;
            entry.setTotalPoints(roundedPoints);

            try {
                // Gọi sang .NET lấy thông tin
                EmployeeResponse emp = employeeRepository.getOneById(entry.getEmployeeId());
                
                if (emp != null) {
                    // 1. Set Tên
                    entry.setEmployeeName(emp.fullName() != null ? emp.fullName() : "Employee #" + entry.getEmployeeId());
                    
                    // 2. 👇 LOGIC MỚI: Lấy ID từ .NET -> Map sang Tên thủ công
                    String deptName = getDepartmentNameById(emp.departmentId());
                    entry.setDepartment(deptName);
                    
                } else {
                    entry.setEmployeeName("Employee #" + entry.getEmployeeId());
                    entry.setDepartment("N/A");
                }
            } catch (Exception e) {
                log.error("Failed to fetch/map employee info for ID: " + entry.getEmployeeId(), e);
                entry.setEmployeeName("Unknown");
                entry.setDepartment("Unknown");
            }
        }

        return leaderboard;
    }

    // Helper method để lấy tên phòng ban từ ID
    private String getDepartmentNameById(Long deptId) {
        if (deptId == null) return "Unknown Dept";
        
        return switch (deptId.intValue()) {
            case 1 -> "Engineering";
            case 2 -> "Product";       
            case 3 -> "Quality Assurance";
            case 4 -> "DevOps";
            case 5 -> "Data & Analytics";
            case 6 -> "Human Resources";
            case 7 -> "Finance";
            default -> "Dept #" + deptId; // Fallback nếu có ID mới
        };
    }

    // ----------------------------------------------------------------
    // 2. LẤY HẠNG CỦA TÔI
    // ----------------------------------------------------------------
    public MyRankInfoDTO getMyRank(Long campaignId, Long empId) {
        List<LeaderboardEntryDTO> leaderboard = getLeaderboard(campaignId);

        Optional<LeaderboardEntryDTO> myEntryOpt = leaderboard.stream()
                .filter(e -> e.getEmployeeId().equals(empId))
                .findFirst();

        if (myEntryOpt.isEmpty()) {
            return MyRankInfoDTO.builder()
                    .rank(0).totalPoints(0).completedActivities(0).pointsToNextRank(0)
                    .nextRankName("Leaderboard").build();
        }

        LeaderboardEntryDTO me = myEntryOpt.get();
        double pointsToNext = 0;
        String nextRankName = null;

        if (me.getRank() > 1) {
            LeaderboardEntryDTO personAbove = leaderboard.get(me.getRank() - 2);
            pointsToNext = Math.round((personAbove.getTotalPoints() - me.getTotalPoints()) * 100.0) / 100.0;
            nextRankName = "Rank #" + (me.getRank() - 1);
        }

        return MyRankInfoDTO.builder()
                .rank(me.getRank())
                .totalPoints(me.getTotalPoints())
                .completedActivities(me.getCompletedActivities())
                .pointsToNextRank(pointsToNext)
                .nextRankName(nextRankName)
                .build();
    }

    // Helper parse JSON
    private double parseDistance(String metricsJson) {
        try {
            if (metricsJson == null || metricsJson.isEmpty()) return 0.0;
            JsonNode node = objectMapper.readTree(metricsJson);
            return node.has("distance") ? node.get("distance").asDouble() : 0.0;
        } catch (Exception e) { return 0.0; }
    } 


    // ADMIN: Đóng chiến dịch (Close Campaign)
    public Campaign closeCampaign(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Validate 1: Chỉ đóng được khi đang active
        if (!"active".equalsIgnoreCase(campaign.getStatus())) {
            throw new IllegalStateException("Only active campaigns can be closed.");
        }

        // Validate 2: Chỉ đóng được khi không còn activity pending
        if (activityRepository.existsByCampaign_CampaignIdAndStatus(id, "pending")) {
            throw new IllegalStateException("Cannot close campaign. There are pending approvals that must be processed first.");
        }

        // Cập nhật trạng thái thành 'completed'
        // Lưu ý: Dùng từ khóa 'completed' để khớp với logic filter ở Frontend
        campaign.setStatus("completed");
        
        // (Optional) Tại đây có thể trigger tính toán reward, notification...
        
        return campaignRepository.save(campaign);
    }
}


