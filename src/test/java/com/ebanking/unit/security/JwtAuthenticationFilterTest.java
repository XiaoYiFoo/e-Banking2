package com.ebanking.unit.security;

import com.ebanking.security.JwtAuthenticationFilter;
import com.ebanking.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        testUserDetails = User.builder()
                .username("sherry")
                .password("")
                .authorities(Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should authenticate user with valid JWT token")
    void shouldAuthenticateUserWithValidJwtToken() throws Exception {
        // Given
        String jwtToken = "valid.jwt.token";
        String customerId = "sherry";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(jwtService.extractCustomerId(jwtToken)).thenReturn(customerId);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userDetailsService.loadUserByUsername(customerId)).thenReturn(testUserDetails);
        when(jwtService.isTokenValid(jwtToken, testUserDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractCustomerId(jwtToken);
        verify(userDetailsService).loadUserByUsername(customerId);
        verify(jwtService).isTokenValid(jwtToken, testUserDetails);
        verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when Authorization header is missing")
    void shouldNotAuthenticateWhenAuthorizationHeaderIsMissing() throws Exception {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService, never()).extractCustomerId(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not authenticate when JWT token is invalid")
    void shouldNotAuthenticateWhenJwtTokenIsInvalid() throws Exception {
        // Given
        String jwtToken = "invalid.jwt.token";
        String customerId = "sherry";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(jwtService.extractCustomerId(jwtToken)).thenReturn(customerId);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(userDetailsService.loadUserByUsername(customerId)).thenReturn(testUserDetails);
        when(jwtService.isTokenValid(jwtToken, testUserDetails)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractCustomerId(jwtToken);
        verify(userDetailsService).loadUserByUsername(customerId);
        verify(jwtService).isTokenValid(jwtToken, testUserDetails);
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should continue filter chain when JWT service throws exception")
    void shouldContinueFilterChainWhenJwtServiceThrowsException() throws Exception {
        // Given
        String jwtToken = "valid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(jwtService.extractCustomerId(jwtToken)).thenThrow(new RuntimeException("JWT parsing error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractCustomerId(jwtToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }
}