package org.httt2.hrms.activity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.activity.entity.Campaign;
import org.httt2.hrms.activity.repository.CampaignRepository;
import org.springframework.stereotype.Service;
import org.httt2.hrms.activity.dto.CampaignCreateRequest;

import org.httt2.hrms.auth.entity.User;
import org.httt2.hrms.auth.repository.UserRepository;
import org.httt2.hrms.employee.entity.Employee;

import org.httt2.hrms.activity.entity.CampaignParticipant;
import org.httt2.hrms.activity.entity.id.CampaignParticipantId;
import org.httt2.hrms.activity.repository.CampaignParticipantRepository;
import org.springframework.transaction.annotation.Transactional;

import org.httt2.hrms.auth.repository.UserRepository;

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

    public List<Campaign> getActiveCampaigns() {
        log.info("Fetching active campaigns");
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

    public Campaign publishCampaign(Long id) {
    Campaign campaign = campaignRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Campaign not found"));
            
    // Validate: Chỉ publish được khi đang là draft
    if (!"draft".equalsIgnoreCase(campaign.getStatus())) {
        throw new IllegalStateException("Only draft campaigns can be published");
    }
    
    campaign.setStatus("active"); // Hoặc "published" tùy quy ước nhóm bạn
    return campaignRepository.save(campaign);
    }

    // Helper method để xác định đơn vị đo lường
    private String determinePrimaryMetric(String type) {
        if (type == null) return "Points";
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
    public void registerForCampaign(Long campaignId, String userEmail) {
        // 1. Tìm Campaign & Validate
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        // Chỉ cho phép đăng ký khi chiến dịch đang Active
        if (!"active".equalsIgnoreCase(campaign.getStatus())) {
            throw new RuntimeException("Cannot register. Campaign is not active.");
        }

        // 2. Tìm User dựa trên Email đăng nhập
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User account not found"));

        // 3. Lấy thông tin Employee từ User
        // Trong User entity bạn đưa: @OneToOne ... private Employee employee;
        Employee employee = currentUser.getEmployee();

        // Validate: User này có phải là nhân viên chính thức không?
        // (Trường hợp tạo User admin nhưng chưa link vào hồ sơ nhân viên)
        if (employee == null) {
            throw new RuntimeException("This account is not linked to an employee profile. Please contact HR.");
        }

        Long empId = employee.getEmpId();

        // 4. Kiểm tra đã đăng ký chưa (tránh trùng lặp)
        if (participantRepository.existsByIdEmpIdAndIdCampaignId(empId, campaignId)) {
            throw new RuntimeException("You have already registered for this campaign.");
        }

        // 5. Tạo và Lưu thông tin tham gia
        CampaignParticipantId id = new CampaignParticipantId(empId, campaignId);

        CampaignParticipant participant = CampaignParticipant.builder()
                .id(id)
                .employee(employee) // Set object Employee
                .campaign(campaign) // Set object Campaign
                .joinedAt(LocalDateTime.now())
                .build();

        participantRepository.save(participant);
    }

    //  EMPLOYEE: Lấy danh sách Campaign mà nhân viên ĐÃ đăng ký
    public List<Campaign> getMyCampaigns(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Employee employee = user.getEmployee();
        if (employee == null) return List.of(); // Nếu chưa là nhân viên thì trả về rỗng

        // Lấy danh sách tham gia từ bảng trung gian
        List<CampaignParticipant> participants = participantRepository.findByEmployee_EmpId(employee.getEmpId());

        // Lấy ra list Campaign từ list Participants
        return participants.stream()
                .map(CampaignParticipant::getCampaign)
                .toList();
    }
}
