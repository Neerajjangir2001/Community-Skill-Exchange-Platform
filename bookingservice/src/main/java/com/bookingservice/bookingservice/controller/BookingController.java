package com.bookingservice.bookingservice.controller;


import com.bookingservice.bookingservice.DTO.BookingCreateRequest;
import com.bookingservice.bookingservice.DTO.BookingResponse;
import com.bookingservice.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ==================== STUDENT ENDPOINTS ====================

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<BookingResponse> createBooking(
            Authentication authentication,
            @Valid @RequestBody BookingCreateRequest request) {

        UUID studentId = (UUID) authentication.getPrincipal();
        log.info("Student {} creating booking for skill {}", studentId, request.getSkillId());

        BookingResponse response = bookingService.createBooking(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication authentication) {
        UUID studentId = (UUID) authentication.getPrincipal();
        log.info("Student {} fetching bookings", studentId);
        return ResponseEntity.ok(bookingService.getBookingsByUser(studentId));
    }

    @PutMapping("/{id}/cancel-student")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<BookingResponse> cancelBookingByStudent(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID studentId = (UUID) authentication.getPrincipal();
        log.info("Student {} canceling booking {}", studentId, id);
        BookingResponse bookingResponse = bookingService.cancelBookingAsStudent(id, studentId);
        return ResponseEntity.ok(bookingResponse);
    }

    // ==================== TEACHER ENDPOINTS ====================

    @GetMapping("/my-provider-bookings")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<BookingResponse>> getMyProviderBookings(Authentication authentication) {
        UUID teacherId = (UUID) authentication.getPrincipal();
        log.info("Teacher {} fetching booking requests", teacherId);
        return ResponseEntity.ok(bookingService.getBookingsByProvider(teacherId));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<BookingResponse> acceptBooking(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID teacherId = (UUID) authentication.getPrincipal();
        log.info("Teacher {} accepting booking {}", teacherId, id);

        BookingResponse response = bookingService.acceptBooking(id, teacherId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<BookingResponse> rejectBooking(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        UUID teacherId = (UUID) authentication.getPrincipal();
        String reason = body.getOrDefault("reason", "Not available");
        log.info("Teacher {} rejecting booking {}", teacherId, id);

        BookingResponse response = bookingService.rejectBooking(id, teacherId, reason);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<BookingResponse> completeBooking(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID teacherId = (UUID) authentication.getPrincipal();
        log.info("Teacher {} completing booking {}", teacherId, id);

        BookingResponse response = bookingService.completeBooking(id, teacherId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel-teacher")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<BookingResponse> cancelBookingByTeacher(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        UUID teacherId = (UUID) authentication.getPrincipal();
        log.info("Teacher {} canceling booking {}", teacherId, id);
        String reason = body.getOrDefault("reason", "Cancelled");
        BookingResponse bookingResponse = bookingService.cancelBookingAsTeacher(id, teacherId, reason);
        return ResponseEntity.ok(bookingResponse);
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        log.info("Admin fetching all bookings");
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBookingStats() {
        log.info("Admin fetching booking statistics");
        return ResponseEntity.ok(bookingService.getBookingStats());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID id) {
        log.info("Admin deleting booking {}", id);
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable UUID userId) {
        log.info("Admin fetching bookings for user {}", userId);
        return ResponseEntity.ok(bookingService.getBookingsByUser(userId));
    }

    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByProvider(@PathVariable UUID providerId) {
        log.info("Admin fetching bookings for provider {}", providerId);
        return ResponseEntity.ok(bookingService.getBookingsByProvider(providerId));
    }

    // ==================== COMMON ENDPOINTS ====================

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID requesterId = (UUID) authentication.getPrincipal();
        log.info("User {} fetching booking {}", requesterId, id);
        return ResponseEntity.ok(bookingService.getBooking(id, requesterId));
    }

}
