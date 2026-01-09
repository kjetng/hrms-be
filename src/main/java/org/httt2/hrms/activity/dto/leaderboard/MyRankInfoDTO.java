package org.httt2.hrms.activity.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyRankInfoDTO {
    private int rank;
    private double totalPoints;
    private int completedActivities;
    private double pointsToNextRank; // Điểm cần để leo hạng
    private String nextRankName;     // Tên hạng tiếp theo (VD: Rank #2)
}