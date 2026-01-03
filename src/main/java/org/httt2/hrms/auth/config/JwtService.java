package org.httt2.hrms.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.httt2.hrms.auth.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

  @Value("${application.security.jwt.secret-key}")
  private String secretKey;

  @Value("${application.security.jwt.expiration}")
  private long jwtExpiration;

  /**
   * Extracts JWT token from Authorization header.
   * Expected format: "Bearer <token>"
   *
   * @param request the HTTP request
   * @return JWT token, or null if not found
   */
  public String extractJwtFromRequest(HttpServletRequest request) {
    final String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring("Bearer ".length());
    }
    return null;
  }

  /**
   * Safely extracts empId claim from JWT token.
   * Returns null if token is invalid or empId claim doesn't exist.
   *
   * @param token the JWT token
   * @return empId as Long, or null if not found or invalid
   */
  public Long extractEmpIdFromToken(String token) {
    try {
      Object empIdClaim = extractClaim(token, claims -> claims.get("empId"));
      if (empIdClaim != null) {
        return ((Number) empIdClaim).longValue();
      }
    } catch (Exception e) {
      // Log but return null - empId is optional
      System.err.println("Failed to extract empId from token: " + e.getMessage());
    }
    return null;
  }

  /**
   * Extracts empId from HTTP request's JWT token.
   * Combines extractJwtFromRequest and extractEmpIdFromToken.
   *
   * @param request the HTTP request
   * @return empId as Long, or null if not found or invalid
   */
  public Long extractEmpIdFromRequest(HttpServletRequest request) {
    String jwt = extractJwtFromRequest(request);
    return jwt != null ? extractEmpIdFromToken(jwt) : null;
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(User user) {
    var claims = new HashMap<String, Object>() {
      {
        put("roles", user.getAuthorities().stream()
            .map(Object::toString).toList());
        put("mail", user.getUsername());
        put("empId", user.getEmpId());
      }
    };
    return generateToken(claims, user);
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(getSignInKey(), Jwts.SIG.HS256)
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSignInKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
