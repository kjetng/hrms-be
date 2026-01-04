package org.httt2.hrms.common.external.employee.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.common.external.employee.EmployeeRepository;
import org.httt2.hrms.common.external.employee.dto.EmployeeResponse;
import org.httt2.hrms.common.external.employee.dto.ManagerEmployeeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EmployeeRepositoryImpl implements EmployeeRepository {

  private final RestTemplate restTemplate;

  @Value("${application.external.employee-service.base-url}")
  private String baseUrl;

  @Value("${application.external.employee-service.token:}")
  private String token;

  @Override
  public EmployeeResponse getOneById(Long id) {
    if (id == null)
      return null;
    if (baseUrl == null || baseUrl.isBlank()) {
      log.warn("Employee service base url is not configured");
      return null;
    }

    String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
        .path("/api/employees/{id}")
        .buildAndExpand(id)
        .toUriString();

    HttpHeaders headers = buildHeaders();
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<EmployeeResponse> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          EmployeeResponse.class);
      return response.getBody();
    } catch (HttpClientErrorException.NotFound e) {
      return null;
    } catch (RestClientResponseException e) {
      log.warn("Employee service responded with error for id {}: status={} body={}", id, e.getRawStatusCode(),
          e.getResponseBodyAsString());
      return null;
    } catch (RestClientException e) {
      log.warn("Failed to call employee service for id {}", id, e);
      return null;
    }
  }

  @Override
  public List<ManagerEmployeeResponse> getDirectReports(Long managerId) {
    if (managerId == null)
      return List.of();
    if (baseUrl == null || baseUrl.isBlank()) {
      log.warn("Employee service base url is not configured");
      return List.of();
    }

    String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
        .path("/api/employees/manager/{id}")
        .buildAndExpand(managerId)
        .toUriString();

    HttpHeaders headers = buildHeaders();
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<List<ManagerEmployeeResponse>> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          entity,
          new ParameterizedTypeReference<List<ManagerEmployeeResponse>>() {
          });
      return response.getBody() == null ? List.of() : response.getBody();
    } catch (RestClientResponseException e) {
      log.warn("Employee service responded with error for manager {}: status={} body={}", managerId,
          e.getRawStatusCode(), e.getResponseBodyAsString());
      return List.of();
    } catch (RestClientException e) {
      log.warn("Failed to call employee service for manager {}", managerId, e);
      return List.of();
    }
  }

  private HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    String bearer = resolveBearerToken();
    if (bearer != null && !bearer.isBlank()) {
      headers.setBearerAuth(bearer);
    }
    return headers;
  }

  private String resolveBearerToken() {
    var attributes = RequestContextHolder.getRequestAttributes();
    if (attributes instanceof ServletRequestAttributes servletAttributes) {
      String authHeader = servletAttributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        return authHeader.substring("Bearer ".length());
      }
    }
    return token;
  }
}
