package org.httt2.hrms.bankaccount.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.httt2.hrms.bankaccount.dto.BankAccountRecordDto;
import org.httt2.hrms.bankaccount.repository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BankAccountRepositoryImpl implements BankAccountRepository {

    private final RestTemplate restTemplate;

    @Value("${application.external.bankaccount-service.base-url}")
    private String baseUrl;

    @Value("${application.external.bankaccount-service.token:}")
    private String token;

    @Override
    public List<BankAccountRecordDto> getMyBankAccounts() {
        if (baseUrl == null || baseUrl.isBlank()) {
            log.warn("Bank account service base url is not configured");
            return List.of();
        }

        String url = baseUrl + "/api/bankaccount/me";

        HttpHeaders headers = buildHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<BankAccountRecordDto>>() {
                    });
            return response.getBody() != null ? response.getBody() : List.of();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Bank account service returned 404");
            return List.of();
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Unauthorized access to bank account service");
            return List.of();
        } catch (RestClientResponseException e) {
            log.warn("Bank account service responded with error: status={} body={}",
                    e.getRawStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (RestClientException e) {
            log.error("Failed to call bank account service", e);
            return List.of();
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String authorizationHeader = getAuthorizationHeader();
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set("Authorization", authorizationHeader);
        }

        return headers;
    }

    private String getAuthorizationHeader() {
        if (token != null && !token.isBlank()) {
            return "Bearer " + token;
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null && !authHeader.isBlank()) {
                return authHeader;
            }
        }

        return null;
    }
}
