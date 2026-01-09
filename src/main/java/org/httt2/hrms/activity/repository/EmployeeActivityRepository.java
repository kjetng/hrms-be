package org.httt2.hrms.activity.repository;

import org.httt2.hrms.activity.entity.EmployeeActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeActivityRepository extends JpaRepository<EmployeeActivity, Long> {

    // Lấy danh sách hoạt động của 1 nhân viên trong 1 chiến dịch cụ thể
    // Sắp xếp giảm dần theo thời gian nộp (Mới nhất lên đầu)
    List<EmployeeActivity> findByCampaign_CampaignIdAndEmpIdOrderByCreatedAtDesc(Long campaignId, Long empId);

    // Lấy danh sách hoạt động đang chờ duyệt của 1 chiến dịch
    @Query("SELECT a FROM EmployeeActivity a " +
            "WHERE a.campaign.campaignId = :campaignId AND a.status = 'pending' " +
            "ORDER BY a.createdAt ASC")
    List<EmployeeActivity> findPendingByCampaignId(@Param("campaignId") Long campaignId);

    // Lấy danh sách hoạt động của 1 chiến dịch theo trạng thái
    List<EmployeeActivity> findByCampaign_CampaignIdAndStatus(Long campaignId, String status);

    // Xóa tất cả hoạt động của nhân viên trong chiến dịch (Hard Delete)
    void deleteAllByCampaign_CampaignIdAndEmpId(Long campaignId, Long empId);

    boolean existsByCampaign_CampaignIdAndStatus(Long campaignId, String status);

    /**
     * Count pending activity submissions for a specific campaign.
     */
    long countByCampaign_CampaignIdAndStatus(Long campaignId, String status);

    /**
     * Count all pending activity submissions across all campaigns.
     */
    long countByStatus(String status);

    /**
     * Sum total distance for a specific campaign (assuming metrics contains
     * distance).
     * For MVP, this returns count of verified activities.
     * In production, you would parse the metrics JSON field.
     */
    @Query("SELECT COUNT(ea) FROM EmployeeActivity ea WHERE ea.campaign.campaignId = :campaignId AND ea.status = 'verified'")
    Long countVerifiedByCampaignId(@Param("campaignId") Long campaignId);
}