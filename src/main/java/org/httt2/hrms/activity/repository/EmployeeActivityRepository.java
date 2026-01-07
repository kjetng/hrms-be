package org.httt2.hrms.activity.repository;

import org.httt2.hrms.activity.entity.EmployeeActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeActivityRepository extends JpaRepository<EmployeeActivity, Long> {
    
    /**
     * Count pending activity submissions for a specific campaign.
     */
    long countByCampaign_CampaignIdAndStatus(Long campaignId, String status);
    
    /**
     * Count all pending activity submissions across all campaigns.
     */
    long countByStatus(String status);
    
    /**
     * Sum total distance for a specific campaign (assuming metrics contains distance).
     * For MVP, this returns count of verified activities. 
     * In production, you would parse the metrics JSON field.
     */
    @Query("SELECT COUNT(ea) FROM EmployeeActivity ea WHERE ea.campaign.campaignId = :campaignId AND ea.status = 'verified'")
    Long countVerifiedByCampaignId(@Param("campaignId") Long campaignId);
}
