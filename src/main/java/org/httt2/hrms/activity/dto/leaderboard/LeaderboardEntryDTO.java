package org.httt2.hrms.activity.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDTO {
    private int rank;
    private Long employeeId;
    private String employeeName;
    private String department;
    private double totalPoints; // Tổng distance
    private int completedActivities;
    private LocalDateTime lastActivityDate;
}