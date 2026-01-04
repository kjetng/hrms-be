package org.httt2.hrms.bonus.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamMemberDto {
    private Long id;
    private String name;
    private String email;
    private String position;
    private String department;
    private String avatar;
}
