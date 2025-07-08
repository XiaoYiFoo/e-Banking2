package com.ebanking.unit.security;

import com.ebanking.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.coyote.http11.filters.IdentityOutputFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private final String jwtSecret = "my-very-secret-key-for-jwt-signing-1234567890";
    private final long jwtExpiration = 1000 * 60 * 60; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
    }

    @Test
    @DisplayName("Should generate a valid JWT token for a customer ID")
    void shouldGenerateValidJwtToken() {
        // Given
        String customerId = "sherry";

        // When
        String token = jwtService.generateToken(customerId);
        System.out.println("Token: " + token);

        // Then
        assertThat(token).isNotBlank();
        String subject = jwtService.extractCustomerId(token);
        assertThat(subject).isEqualTo(customerId);
    }

    @Test
    @DisplayName("Should extract claims from JWT token")
    void shouldExtractClaimsFromJwtToken() {
        // Given
        String customerId = "CUST123456";
        String token = jwtService.generateToken(customerId);

        // When
        Claims claims = ReflectionTestUtils.invokeMethod(jwtService, "extractAllClaims", token);

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(customerId);
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("Should extract expiration date from JWT token")
    void shouldExtractExpirationDateFromJwtToken() {
        // Given
        String customerId = "CUST123456";
        String token = jwtService.generateToken(customerId);

        // When
        Date expiration = jwtService.extractExpiration(token);

        // Then
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Should validate token for correct user details")
    void shouldValidateTokenForCorrectUserDetails() {
        // Given
        String customerId = "CUST123456";
        String token = jwtService.generateToken(customerId);
        UserDetails userDetails = User.builder()
                .username(customerId)
                .password("")
                .authorities("ROLE_CUSTOMER")
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should not validate token for incorrect user details")
    void shouldNotValidateTokenForIncorrectUserDetails() {
        // Given
        String customerId = "CUST123456";
        String token = jwtService.generateToken(customerId);
        UserDetails userDetails = User.builder()
                .username("OTHER_USER")
                .password("")
                .authorities("ROLE_CUSTOMER")
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        // Given
        String customerId = "CUST123456";
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1 ms
        String token = jwtService.generateToken(customerId);

        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        UserDetails userDetails = User.builder()
                .username(customerId)
                .password("")
                .authorities("ROLE_CUSTOMER")
                .build();

        // When / Then
        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Should extract custom claims using extractClaim")
    void shouldExtractCustomClaimsUsingExtractClaim() {
        // Given
        String customerId = "CUST123456";
        String token = jwtService.generateToken(customerId);

        // When
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Then
        assertThat(subject).isEqualTo(customerId);
    }

    @Test
    @DisplayName("Should create token with custom claims")
    void shouldCreateTokenWithCustomClaims() {
        // Given
        String customerId = "CUST123456";
        Map<String, Object> claims = Map.of("role", "customer", "foo", "bar");

        // Use reflection to call private createToken method
        String token = ReflectionTestUtils.invokeMethod(
                jwtService, "createToken", claims, customerId);

        // When
        Claims parsedClaims = ReflectionTestUtils.invokeMethod(jwtService, "extractAllClaims", token);

        // Then
        assertThat(parsedClaims.get("role")).isEqualTo("customer");
        assertThat(parsedClaims.get("foo")).isEqualTo("bar");
        assertThat(parsedClaims.getSubject()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void shouldThrowExceptionForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.value";

        // When / Then
        assertThatThrownBy(() -> jwtService.extractCustomerId(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should use correct signing key")
    void shouldUseCorrectSigningKey() {
        // Given
        SecretKey expectedKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String customerId = "CUST123456";
        String token = jwtService.generateToken(customerId);

        // When
        Claims claims = Jwts.parser()
                .verifyWith(expectedKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Then
        assertThat(claims.getSubject()).isEqualTo(customerId);
    }
}