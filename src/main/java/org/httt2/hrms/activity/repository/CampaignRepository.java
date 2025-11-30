package org.httt2.hrms.activity.repository;

import org.httt2.hrms.activity.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}