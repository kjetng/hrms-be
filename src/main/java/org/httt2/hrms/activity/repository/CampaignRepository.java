package org.httt2.hrms.activity.repository;

import org.httt2.hrms.activity.dto.approval.PendingCampaignDTO;
import org.httt2.hrms.activity.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    
    List<Campaign> findAllByOrderByCreatedAtDesc();
    
    List<Campaign> findByStatusOrderByCreatedAtDesc(String status);
    
    List<Campaign> findByCampaignTypeOrderByCreatedAtDesc(String campaignType);
    
    @Query("SELECT c FROM Campaign c WHERE LOWER(c.campaignName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY c.createdAt DESC")
    List<Campaign> searchCampaigns(String searchTerm);
    
    Optional<Campaign> findByCampaignId(Long campaignId);

    List<Campaign> findByStatus(String status);

    // Lấy danh sách chiến dịch mà nhân viên đã tham gia
    @Query(value = """
    SELECT c.* FROM campaign c 
    JOIN campaign_participant cp ON c.campaign_id = cp.campaign_id 
    WHERE cp.emp_id = :empId
    """, nativeQuery = true)
    List<Campaign> findByEmpId(@Param("empId") Long empId);
    
    // Lấy danh sách chiến dịch có hoạt động đang chờ duyệt
    @Query("SELECT new org.httt2.hrms.activity.dto.approval.PendingCampaignDTO(c.campaignId, c.campaignName, COUNT(a)) " +
           "FROM Campaign c JOIN EmployeeActivity a ON c.campaignId = a.campaign.campaignId " +
           "WHERE a.status = 'pending' " +
           "GROUP BY c.campaignId, c.campaignName")
    List<PendingCampaignDTO> findCampaignsWithPendingActivities();


    // Lấy các chiến dịch Active mà nhân viên CHƯA TỪNG tham gia (Chưa có record trong bảng Campaign_Participant)
    @Query("SELECT c FROM Campaign c " +
           "WHERE c.status = 'active' " +
           "AND c.campaignId NOT IN " +
           "(SELECT cp.campaignId FROM CampaignParticipant cp WHERE cp.empId = :empId)")
    List<Campaign> findAvailableCampaignsForEmployee(@Param("empId") Long empId);
}