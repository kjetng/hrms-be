package org.httt2.hrms.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userEmail;

    // Check Authorization header first
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      jwt = authHeader.substring("Bearer ".length());
    }
    // For SSE endpoints, check query parameter (EventSource doesn't support custom
    // headers)
    else if (request.getRequestURI().contains("/notifications/stream")) {
      jwt = request.getParameter("token");
      if (jwt == null || jwt.isEmpty()) {
        filterChain.doFilter(request, response);
        return;
      }
    } else {
      filterChain.doFilter(request, response);
      return;
    }

    try {

      userEmail = jwtService.extractUsername(jwt);
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
        if (jwtService.isTokenValid(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
      filterChain.doFilter(request, response);
    } catch (ExpiredJwtException ex) {
      handleJwtException(response, "Token has expired. Please login again.", HttpStatus.UNAUTHORIZED);
    } catch (MalformedJwtException | SignatureException ex) {
      handleJwtException(response, "Invalid token. Please login again.", HttpStatus.UNAUTHORIZED);
    } catch (JwtException ex) {
      handleJwtException(response, "Token validation failed. Please login again.", HttpStatus.UNAUTHORIZED);
    } catch (Exception ex) {
      handleJwtException(response, "Authentication failed. Please login again.", HttpStatus.UNAUTHORIZED);
    }
  }

  private void handleJwtException(HttpServletResponse response, String message, HttpStatus status) throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);

    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getWriter(), body);
  }
}
