package org.httt2.hrms.bonus.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TeamMemberViewService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    public TeamMembersResponseDto getTeamMembers(Integer page, Integer size, String search) {
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
        Long managerId = null;

        // Get requester details to find their manager
        org.httt2.hrms.common.external.employee.dto.EmployeeResponse requesterDetails = employeeRepository
                .getOneById(requesterId);

        log.debug("Requester ID: {}, Details: {}", requesterId, requesterDetails);

        // If user is a MANAGER, show their direct reports
        if ("MANAGER".equals(userRole)) {
            reports = employeeRepository.getDirectReports(requesterId);
            // For managers, their manager is their own managerId
            if (requesterDetails != null) {
                managerId = requesterDetails.managerId();
            }
            log.debug("MANAGER role - Reports count: {}, Manager ID: {}", reports.size(), managerId);
        } else {
            // For non-managers, show peers (teammates with same manager)
            // If requester not found or has no manager, return empty list
            if (requesterDetails == null || requesterDetails.managerId() == null) {
                log.debug("Non-manager with no manager ID, returning empty list");
                return TeamMembersResponseDto.builder()
                        .teamMembers(List.of())
                        .totalRecords(0)
                        .role(userRole)
                        .build();
            }

            managerId = requesterDetails.managerId();
            // Fetch all employees under the requester's manager (peers)
            reports = employeeRepository.getDirectReports(managerId);
            log.debug("Non-MANAGER role - Manager ID: {}, Peers count: {}", managerId, reports.size());
        }

        List<ManagerEmployeeResponse> filtered = reports.stream()
                .filter(r -> r.id() != null && !r.id().equals(requesterId))
                .filter(r -> r.status() != null && r.status().equalsIgnoreCase(ACTIVE_STATUS))
                .sorted(Comparator
                        .comparing(ManagerEmployeeResponse::fullName, Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(ManagerEmployeeResponse::id))
                .toList();

        // Add manager to the list if they exist and are active
        List<ManagerEmployeeResponse> allMembers = new java.util.ArrayList<>(filtered);
        if (managerId != null) {
            log.debug("Fetching manager with ID: {}", managerId);
            org.httt2.hrms.common.external.employee.dto.EmployeeResponse managerDetails = employeeRepository
                    .getOneById(managerId);
            log.debug("Manager details: {}", managerDetails);
            if (managerDetails != null && managerDetails.status() != null
                    && managerDetails.status().equalsIgnoreCase(ACTIVE_STATUS)) {
                // Convert to ManagerEmployeeResponse and add to the beginning of the list
                ManagerEmployeeResponse managerResponse = new ManagerEmployeeResponse(
                        managerDetails.id(),
                        managerDetails.fullName(),
                        managerDetails.email(),
                        null, // positionId
                        null, // departmentId
                        managerDetails.status(),
                        managerDetails.departmentName(),
                        managerDetails.positionTitle());
                allMembers.add(0, managerResponse);
                log.debug("Added manager to team list: {}", managerDetails.fullName());
            } else {
                log.debug("Manager not added. Details null: {}, Status: {}",
                        managerDetails == null,
                        managerDetails != null ? managerDetails.status() : "N/A");
            }
        } else {
            log.debug("Manager ID is null, not adding manager to team list");
        }

        // Apply search filter BEFORE pagination
        List<ManagerEmployeeResponse> searchFiltered = allMembers;
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            searchFiltered = allMembers.stream()
                    .filter(member -> matchesSearch(member, searchLower))
                    .toList();
            log.debug("Search applied: '{}' - filtered {} members from {}", search, searchFiltered.size(),
                    allMembers.size());
        }

        // Count total records AFTER search filter is applied
        long totalRecords = searchFiltered.size();

        // Apply pagination to filtered results
        int fromIndex = (pageNumber - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, searchFiltered.size());
        List<ManagerEmployeeResponse> pageSlice = fromIndex >= searchFiltered.size()
                ? List.of()
                : searchFiltered.subList(fromIndex, toIndex);

        Long finalManagerId = managerId;
        List<TeamMemberDto> members = pageSlice.stream()
                .map(r -> TeamMemberDto.builder()
                        .id(r.id())
                        .name(r.fullName())
                        .email(r.email())
                        .position(r.positionTitle())
                        .department(r.departmentName())
                        .avatar(null)
                        .isManager(r.id().equals(finalManagerId))
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

    /**
     * Check if a team member matches the search criteria.
     * Performs case-insensitive partial matching against:
     * - Full name (first name, last name, or full name)
     * - Email address
     * 
     * @param member      the team member to check
     * @param searchLower the search term (already converted to lowercase)
     * @return true if the member matches the search criteria
     */
    private boolean matchesSearch(ManagerEmployeeResponse member, String searchLower) {
        // Check full name (case-insensitive partial match)
        if (member.fullName() != null && member.fullName().toLowerCase().contains(searchLower)) {
            return true;
        }

        // Check email (case-insensitive partial match)
        if (member.email() != null && member.email().toLowerCase().contains(searchLower)) {
            return true;
        }

        return false;
    }

}
