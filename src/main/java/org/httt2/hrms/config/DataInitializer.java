package org.httt2.hrms.config;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.activity.entity.Campaign;
import org.httt2.hrms.repository.CampaignRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final CampaignRepository campaignRepository;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Only add sample data if no campaigns exist
            if (campaignRepository.count() == 0) {
                Campaign campaign1 = new Campaign();
                campaign1.setName("Company Running Challenge 2025");
                campaign1.setDescription("Join our annual running challenge and compete with colleagues");
                campaign1.setStartDate(LocalDate.of(2025, 1, 15));
                campaign1.setStartTime(LocalTime.of(9, 0));
                campaign1.setEndDate(LocalDate.of(2025, 3, 15));
                campaign1.setEndTime(LocalTime.of(17, 0));
                campaign1.setActivityType(Campaign.ActivityType.RUNNING);
                campaign1.setStatus(Campaign.CampaignStatus.ACTIVE);
                campaign1.setCreatedBy(UUID.randomUUID());
                campaign1.setImageUrl("https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=600&h=400&fit=crop");
                
                Campaign campaign2 = new Campaign();
                campaign2.setName("Fitness Month - March");
                campaign2.setDescription("Complete fitness activities and earn points");
                campaign2.setStartDate(LocalDate.of(2025, 3, 1));
                campaign2.setStartTime(LocalTime.of(9, 0));
                campaign2.setEndDate(LocalDate.of(2025, 3, 31));
                campaign2.setEndTime(LocalTime.of(17, 0));
                campaign2.setActivityType(Campaign.ActivityType.WALKING);
                campaign2.setStatus(Campaign.CampaignStatus.DRAFT);
                campaign2.setCreatedBy(UUID.randomUUID());
                campaign2.setImageUrl("https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=600&h=400&fit=crop");
                
                Campaign campaign3 = new Campaign();
                campaign3.setName("Q4 2024 Marathon");
                campaign3.setDescription("Long-distance running competition");
                campaign3.setStartDate(LocalDate.of(2024, 10, 1));
                campaign3.setStartTime(LocalTime.of(9, 0));
                campaign3.setEndDate(LocalDate.of(2024, 12, 31));
                campaign3.setEndTime(LocalTime.of(17, 0));
                campaign3.setActivityType(Campaign.ActivityType.RUNNING);
                campaign3.setStatus(Campaign.CampaignStatus.COMPLETED);
                campaign3.setCreatedBy(UUID.randomUUID());
                campaign3.setImageUrl("https://images.unsplash.com/photo-1536922246289-88c42f957773?w=600&h=400&fit=crop");
                
                campaignRepository.save(campaign1);
                campaignRepository.save(campaign2);
                campaignRepository.save(campaign3);
                
                System.out.println("Sample campaigns data initialized!");
            }
        };
    }
}