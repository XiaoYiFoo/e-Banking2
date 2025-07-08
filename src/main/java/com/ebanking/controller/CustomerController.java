//package com.ebanking.controller;
//
//import com.ebanking.domain.Customer;
//import com.ebanking.repository.CustomerRepository;
//import com.ebanking.service.CustomerService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/customers")
//@RequiredArgsConstructor
//public class CustomerController {
//
//    private final CustomerService customerService;
//    private final CustomerRepository customerRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @GetMapping("/{customerId}")
//    public ResponseEntity<Customer> getCustomerById(@PathVariable String customerId) {
//        Customer customer = customerService.getCustomerById(customerId);
//        return ResponseEntity.ok(customer);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Customer>> getAllCustomers() {
//        List<Customer> customers = customerService.getAllCustomers();
//        return ResponseEntity.ok(customers);
//    }
//
//    @PostMapping
//    public ResponseEntity<Customer> createCustomer(@RequestBody Map<String, String> request) {
//        String customerId = request.get("id");
//        String password = request.get("password");
//
//        if (customerRepository.existsById(customerId)) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        Customer customer = Customer.builder()
//                .id(customerId)
//                .password(passwordEncoder.encode(password))
//                .build();
//
//        Customer saved = customerService.saveCustomer(customer);
//        return ResponseEntity.ok(saved);
//    }
//}