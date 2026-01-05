package org.httt2.hrms.bonus.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TeamMembersResponseDto {
    private List<TeamMemberDto> teamMembers;
    private long totalRecords;
    private String role;
}
