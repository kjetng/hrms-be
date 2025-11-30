package org.httt2.hrms.config;

import lombok.RequiredArgsConstructor;
import org.httt2.hrms.activity.entity.Campaign;
import org.httt2.hrms.activity.repository.CampaignRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final CampaignRepository campaignRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (campaignRepository.count() == 0) {
                System.out.println("🌱 Starting seeding data...");

                // 1. Campaign 1: Running
                Campaign c1 = new Campaign();
                c1.setCampaignName("Summer Running Challenge 2025");
                c1.setCampaignType("running"); // Dùng String thường
                c1.setPrimaryMetric("Distance (km)");
                c1.setDescription("Chiến dịch chạy bộ mùa hè dành cho toàn thể nhân viên.");
                c1.setStartDate(LocalDate.of(2025, 6, 1));
                c1.setEndDate(LocalDate.of(2025, 6, 30));
                c1.setStartTime(LocalTime.of(9, 00)); 
                c1.setEndTime(LocalTime.of(23, 0));
                c1.setStatus("active"); // Dùng String thường
                c1.setImageUrl("https://images.unsplash.com/photo-1552674605-db6ffd4facb5");

                // 2. Campaign 2: Walking
                Campaign c2 = new Campaign();
                c2.setCampaignName("Office Walking Day");
                c2.setCampaignType("walking");
                c2.setPrimaryMetric("Steps");
                c2.setDescription("Đi bộ nhẹ nhàng quanh văn phòng.");
                c2.setStartDate(LocalDate.of(2025, 7, 15));
                c2.setEndDate(LocalDate.of(2025, 7, 20));
                c2.setStartTime(LocalTime.of(9, 00)); 
                c2.setEndTime(LocalTime.of(17, 0));
                c2.setStatus("draft");
                c2.setImageUrl("https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b");

                // 3. Campaign 3: Cycling
                Campaign c3 = new Campaign();
                c3.setCampaignName("Tour de City Cycling");
                c3.setCampaignType("cycling");
                c3.setPrimaryMetric("Distance (km)");
                c3.setDescription("Đạp xe quanh thành phố vào cuối tuần.");
                c3.setStartDate(LocalDate.of(2024, 1, 1));
                c3.setEndDate(LocalDate.of(2024, 1, 30));
                c3.setStartTime(LocalTime.of(9, 00)); 
                c3.setEndTime(LocalTime.of(17, 0));
                c3.setStatus("completed");
                c3.setImageUrl("https://images.unsplash.com/photo-1536922246289-88c42f957773");

                // Lưu vào DB
                campaignRepository.saveAll(List.of(c1, c2, c3));
                System.out.println("✅ Data seeded successfully!");
            }
        };
    }
}