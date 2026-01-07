package org.httt2.hrms.activity.repository;

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

    @Query(value = """
    SELECT c.* FROM campaign c 
    JOIN campaign_participant cp ON c.campaign_id = cp.campaign_id 
    WHERE cp.emp_id = :empId
    """, nativeQuery = true)
    List<Campaign> findByEmpId(@Param("empId") Long empId);
    
    /**
     * Count campaigns by status for dashboard statistics.
     */
    long countByStatus(String status);
}