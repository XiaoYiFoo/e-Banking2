package com.ebanking.unit.security;

import com.ebanking.domain.Customer;
import com.ebanking.repository.CustomerRepository;
import com.ebanking.security.CustomerUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerUserDetailsService Unit Tests")
class CustomerUserDetailsServiceTest {

    private CustomerUserDetailsService customerUserDetailsService;

    @Mock
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerUserDetailsService = new CustomerUserDetailsService(customerRepository);
    }

    @Test
    @DisplayName("Should load user details for valid customer ID")
    void shouldLoadUserDetailsForValidCustomerId() {
        // Given
        String customerId = "sherry";
        Customer customer = Customer.builder()
                .id(customerId)
                .password("encoded-password")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(customerId);

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(customerId);
        assertThat(userDetails.getPassword()).isEqualTo("encoded-password");
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();

        // Verify authorities
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CUSTOMER");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException for non-existent customer")
    void shouldThrowUsernameNotFoundExceptionForNonExistentCustomer() {
        // Given
        String customerId = "nonexistent";
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerUserDetailsService.loadUserByUsername(customerId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Customer not found: " + customerId);
    }

    @Test
    @DisplayName("Should handle null customer ID")
    void shouldHandleNullCustomerId() {
        // When & Then
        assertThatThrownBy(() -> customerUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Customer not found: null");
    }

    @Test
    @DisplayName("Should handle empty customer ID")
    void shouldHandleEmptyCustomerId() {
        // Given
        when(customerRepository.findById("")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customerUserDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Customer not found: ");
    }
}