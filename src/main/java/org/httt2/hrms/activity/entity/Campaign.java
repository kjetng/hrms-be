package org.httt2.hrms.activity.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;   
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "campaign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long campaignId;

    @Column(nullable = false)
    private String campaignName;

    @Column(nullable = false)
    private String campaignType; // 'walking', 'running', 'cycling'

    private String primaryMetric;
    
    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(name = "start_time") 
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(nullable = false)
    private String status; // 'draft', 'active', 'completed'

    private String imageUrl; // S3 URL for campaign image

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "draft";
        }
    }

    // Đếm số lượng người tham gia
    @Formula("(SELECT COUNT(*) FROM campaign_participant cp WHERE cp.campaign_id = campaign_id AND cp.status = 'JOINED')")
    private Long participantCount;

    // Tính tổng điểm (đã cộng dồn từ EmployeeActivity)
    @Formula("(SELECT COALESCE(SUM(cp.current_score), 0) FROM campaign_participant cp WHERE cp.campaign_id = campaign_id)")
    private Double totalDistance;

    // Getter cho trường này (Lombok @Getter đã tự sinh, nhưng nếu không có thì bạn thêm thủ công)
    public Long getParticipantCount() {
        return participantCount == null ? 0 : participantCount;
    }

    public Double getTotalDistance() {
        return totalDistance == null ? 0.0 : totalDistance;
    }
}