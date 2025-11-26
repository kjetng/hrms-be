package org.httt2.hrms.repository;

import org.httt2.hrms.activity.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    
    List<Campaign> findAllByOrderByCreatedAtDesc();
    
    List<Campaign> findByStatusOrderByCreatedAtDesc(Campaign.CampaignStatus status);
    
    @Query("SELECT c FROM Campaign c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY c.createdAt DESC")
    List<Campaign> searchCampaigns(@Param("search") String search);
}