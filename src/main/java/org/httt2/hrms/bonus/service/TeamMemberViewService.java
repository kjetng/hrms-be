package org.httt2.hrms.bonus.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.httt2.hrms.auth.config.JwtService;
import org.httt2.hrms.auth.entity.User;
import org.httt2.hrms.bonus.dto.TeamMemberDto;
import org.httt2.hrms.bonus.dto.TeamMembersResponseDto;
import org.httt2.hrms.common.external.employee.EmployeeRepository;
import org.httt2.hrms.common.external.employee.dto.ManagerEmployeeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamMemberViewService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    public TeamMembersResponseDto getTeamMembers(Integer page, Integer size) {
        int pageNumber = page == null ? 1 : page;
        int pageSize = size == null ? 10 : size;

        if (pageNumber <= 0 || pageSize <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page and size must be positive");
        }

        Long requesterId = resolveCurrentEmployeeId();
        if (requesterId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid auth token");
        }

        String userRole = extractRoleFromRequest();
        List<ManagerEmployeeResponse> reports;

        // If user is a MANAGER, show their direct reports
        if ("MANAGER".equals(userRole)) {
            reports = employeeRepository.getDirectReports(requesterId);
        } else {
            // For non-managers, show peers (teammates with same manager)
            org.httt2.hrms.common.external.employee.dto.EmployeeResponse requesterDetails = employeeRepository
                    .getOneById(requesterId);

            // If requester not found or has no manager, return empty list
            if (requesterDetails == null || requesterDetails.managerId() == null) {
                return TeamMembersResponseDto.builder()
                        .teamMembers(List.of())
                        .totalRecords(0)
                        .role(userRole)
                        .build();
            }

            // Fetch all employees under the requester's manager (peers)
            reports = employeeRepository.getDirectReports(requesterDetails.managerId());
        }

        List<ManagerEmployeeResponse> filtered = reports.stream()
                .filter(r -> r.id() != null && !r.id().equals(requesterId))
                .filter(r -> r.status() != null && r.status().equalsIgnoreCase(ACTIVE_STATUS))
                .sorted(Comparator
                        .comparing(ManagerEmployeeResponse::fullName, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(ManagerEmployeeResponse::id))
                .toList();

        long totalRecords = filtered.size();
        int fromIndex = (pageNumber - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        List<ManagerEmployeeResponse> pageSlice = fromIndex >= filtered.size()
                ? List.of()
                : filtered.subList(fromIndex, toIndex);

        List<TeamMemberDto> members = pageSlice.stream()
                .map(r -> TeamMemberDto.builder()
                        .id(r.id())
                        .name(r.fullName())
                        .email(r.email())
                        .position(null)
                        .department(null)
                        .avatar(null)
                        .build())
                .toList();

        return TeamMembersResponseDto.builder()
                .teamMembers(members)
                .totalRecords(totalRecords)
                .role(userRole)
                .build();
    }

    private Long resolveCurrentEmployeeId() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            Long empId = jwtService.extractEmpIdFromRequest(request);
            if (empId != null) {
                return empId;
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails && userDetails instanceof User user) {
                return user.getEmpId();
            }
        }
        return null;
    }

    private String extractRoleFromRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails && userDetails instanceof User user) {
                return user.getRole() != null ? user.getRole().name() : null;
            }
        }
        return null;
    }

}
