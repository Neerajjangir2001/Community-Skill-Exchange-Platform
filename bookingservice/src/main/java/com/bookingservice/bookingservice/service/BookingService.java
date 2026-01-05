package com.bookingservice.bookingservice.service;

import com.bookingservice.bookingservice.DTO.BookingCreateRequest;
import com.bookingservice.bookingservice.DTO.BookingResponse;
import com.bookingservice.bookingservice.config.ExternalServiceClient;
import com.bookingservice.bookingservice.events.BookingCreatedEvent;
import com.bookingservice.bookingservice.events.BookingStatusChangedEvent;
import com.bookingservice.bookingservice.exception.BookingNotFoundException;
import com.bookingservice.bookingservice.mapper.Mapper;
import com.bookingservice.bookingservice.model.Booking;
import com.bookingservice.bookingservice.model.BookingHistory;
import com.bookingservice.bookingservice.model.Status;
import com.bookingservice.bookingservice.repository.BookingHistoryRepository;
import com.bookingservice.bookingservice.repository.BookingRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.UnsupportedByAuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ExternalServiceClient externalClient;

    @Value("${kafka.topic.booking-events}")
    private String bookingEventsTopic;

    // ========== STUDENT METHODS ==========


    @Transactional
    public BookingResponse createBooking(UUID userId, BookingCreateRequest request) {
        log.info("Creating booking for user {} with skill {}", userId, request.getSkillId());

        // 1. Validate user exists (optional - token already validated)
        if (!externalClient.validateUser(userId)) {
            throw new ValidationException("User not found: " + userId);
        }

        // 2. Get skill details
        ExternalServiceClient.SkillDetails skill = externalClient.getSkill(request.getSkillId());
        if (skill == null) {
            throw new ValidationException("Skill not found: " + request.getSkillId());
        }

        UUID providerId = skill.getUserId();

        // 3. Validate provider exists
        if (!externalClient.validateUser(providerId)) {
            throw new ValidationException("Provider not found: " + providerId);
        }

        // 4.  Validate cannot book own skill
        if (userId.equals(providerId)) {
            throw new ValidationException("Cannot book your own skill");
        }

        // 5.  Validate time
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new ValidationException("End time must be after start time");
        }

        if (request.getStartTime().isBefore(OffsetDateTime.now())) {
            throw new ValidationException("Cannot book in the past");
        }

        // 6. Calculate pricing
        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        BigDecimal totalHours = BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        BigDecimal pricePerHour = skill.getPricePerHour();
        BigDecimal totalPrice = pricePerHour.multiply(totalHours)
                .setScale(2, RoundingMode.HALF_UP);

        // 7. Create booking
        Booking booking = Booking.builder()
                .userId(userId)
                .providerId(providerId)
                .skillId(request.getSkillId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .totalHours(totalHours)
                .pricePerHour(pricePerHour)
                .totalPrice(totalPrice)
                .status(Status.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created: {} with total price: {}", saved.getId(), totalPrice);

        // 8. Save history and publish event
        saveHistory(saved.getId(), null, Status.PENDING, "Booking created by student");
        publishBookingCreated(saved);

        return Mapper.toResponse(saved);
    }


    public List<BookingResponse> getBookingsByUser(UUID userId) {
        log.info("Fetching bookings for student: {}", userId);
        return bookingRepository.findByUserId(userId).stream()
                .map(Mapper::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public BookingResponse cancelBookingAsStudent(UUID bookingId, UUID userId) {
        log.info("Student {} cancelling booking {}", userId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        //  Verify ownership
        if (!booking.getUserId().equals(userId)) {
            throw new UnsupportedByAuthenticationException("You can only cancel your own bookings");
        }

        //  Validate status
        if (booking.getStatus() == Status.COMPLETED || booking.getStatus() == Status.CANCELLED) {
            throw new ValidationException("Cannot cancel completed or already cancelled booking");
        }

        Status oldStatus = booking.getStatus();
        booking.setStatus(Status.CANCELLED);
        bookingRepository.save(booking);

        saveHistory(bookingId, oldStatus, Status.CANCELLED, "Cancelled by student");
        publishStatusChanged(booking, oldStatus, Status.CANCELLED, "Cancelled by student");

        log.info(" Booking {} cancelled by student", bookingId);
        return Mapper.toResponse(booking);
    }

    // ========== TEACHER METHODS ==========


    public List<BookingResponse> getBookingsByProvider(UUID providerId) {
        log.info("Fetching bookings for provider: {}", providerId);
        return bookingRepository.findByProviderId(providerId).stream()
                .map(Mapper::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public BookingResponse acceptBooking(UUID bookingId, UUID teacherId) {
        log.info("Teacher {} accepting booking {}", teacherId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        //  Verify ownership
        if (!booking.getProviderId().equals(teacherId)) {
            throw new UnsupportedByAuthenticationException("Only the skill provider can accept this booking");
        }

        // Validate status
        if (booking.getStatus() != Status.PENDING) {
            throw new ValidationException("Can only accept PENDING bookings");
        }

        Status oldStatus = booking.getStatus();
        booking.setStatus(Status.CONFIRMED);
        bookingRepository.save(booking);

        saveHistory(bookingId, oldStatus, Status.CONFIRMED, "Accepted by teacher");
        publishStatusChanged(booking, oldStatus, Status.CONFIRMED, "Accepted by teacher");

        log.info(" Booking {} accepted by teacher", bookingId);
        return Mapper.toResponse(booking);
    }


    @Transactional
    public BookingResponse rejectBooking(UUID bookingId, UUID teacherId, String reason) {
        log.info("Teacher {} rejecting booking {}", teacherId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        if (!booking.getProviderId().equals(teacherId)) {
            throw new UnsupportedByAuthenticationException("Only the skill provider can reject this booking");
        }

        if (booking.getStatus() != Status.PENDING) {
            throw new ValidationException("Can only reject PENDING bookings");
        }

        Status oldStatus = booking.getStatus();
        booking.setStatus(Status.REJECTED);
        bookingRepository.save(booking);

        String metadata = reason != null ? "Rejected by teacher: " + reason : "Rejected by teacher";
        saveHistory(bookingId, oldStatus, Status.REJECTED, metadata);
        publishStatusChanged(booking, oldStatus, Status.REJECTED, metadata);

        log.info(" Booking {} rejected by teacher", bookingId);
        return Mapper.toResponse(booking);
    }


    @Transactional
    public BookingResponse completeBooking(UUID bookingId, UUID teacherId) {
        log.info("Teacher {} completing booking {}", teacherId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        if (!booking.getProviderId().equals(teacherId)) {
            throw new UnsupportedByAuthenticationException("Only the skill provider can complete this booking");
        }

        if (booking.getStatus() != Status.CONFIRMED) {
            throw new ValidationException("Can only complete CONFIRMED bookings");
        }

        Status oldStatus = booking.getStatus();
        booking.setStatus(Status.COMPLETED);
        bookingRepository.save(booking);

        saveHistory(bookingId, oldStatus, Status.COMPLETED, "Completed by teacher");
        publishStatusChanged(booking, oldStatus, Status.COMPLETED, "Completed by teacher");

        log.info(" Booking {} completed by teacher", bookingId);
        return Mapper.toResponse(booking);
    }


    @Transactional
    public BookingResponse cancelBookingAsTeacher(UUID bookingId, UUID teacherId, String reason) {
        log.info("Teacher {} cancelling booking {}", teacherId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        if (!booking.getProviderId().equals(teacherId)) {
            throw new UnsupportedByAuthenticationException("Only the skill provider can cancel this booking");
        }

        if (booking.getStatus() == Status.COMPLETED || booking.getStatus() == Status.CANCELLED) {
            throw new ValidationException("Cannot cancel completed or already cancelled booking");
        }

        Status oldStatus = booking.getStatus();
        booking.setStatus(Status.CANCELLED);
        bookingRepository.save(booking);

        String metadata = reason != null ? "Cancelled by teacher: " + reason : "Cancelled by teacher";
        saveHistory(bookingId, oldStatus, Status.CANCELLED, metadata);
        publishStatusChanged(booking, oldStatus, Status.CANCELLED, metadata);

        log.info(" Booking {} cancelled by teacher", bookingId);
        return Mapper.toResponse(booking);
    }

    // ========== COMMON METHODS ==========


    public BookingResponse getBooking(UUID id, UUID requesterId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + id));

        // Verify access (only student, teacher, or admin can view)
        if (!booking.getUserId().equals(requesterId) &&
                !booking.getProviderId().equals(requesterId)) {
            throw new UnsupportedByAuthenticationException("You don't have access to this booking");
        }

        return Mapper.toResponse(booking);
    }

    // ========== ADMIN METHODS ==========


    public List<BookingResponse> getAllBookings() {
        log.info("Admin fetching all bookings");
        return bookingRepository.findAll().stream()
                .map(Mapper::toResponse)
                .collect(Collectors.toList());
    }


    public Map<String, Object> getBookingStats() {
        log.info("Admin calculating booking statistics");
        Map<String, Object> stats = new HashMap<>();

        List<Booking> allBookings = bookingRepository.findAll();

        stats.put("total", allBookings.size());
        stats.put("pending", allBookings.stream().filter(b -> b.getStatus() == Status.PENDING).count());
        stats.put("confirmed", allBookings.stream().filter(b -> b.getStatus() == Status.CONFIRMED).count());
        stats.put("completed", allBookings.stream().filter(b -> b.getStatus() == Status.COMPLETED).count());
        stats.put("cancelled", allBookings.stream().filter(b -> b.getStatus() == Status.CANCELLED).count());
        stats.put("rejected", allBookings.stream().filter(b -> b.getStatus() == Status.REJECTED).count());

        BigDecimal totalRevenue = allBookings.stream()
                .filter(b -> b.getStatus() == Status.COMPLETED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }

    @Transactional
    public void deleteBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        bookingRepository.delete(booking);
        log.info(" Booking {} deleted by admin", bookingId);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private void saveHistory(UUID bookingId, Status oldStatus, Status newStatus, String metadata) {
        BookingHistory history = BookingHistory.builder()
                .bookingId(bookingId)
                .oldStatus(oldStatus == null ? null : oldStatus.name())
                .newStatus(newStatus.name())
                .metadata(metadata)
                .build();
        bookingHistoryRepository.save(history);
    }

    private void publishBookingCreated(Booking booking) {
        try {
            // Fetch skill details
            ExternalServiceClient.SkillDetails skill = externalClient.getSkill(booking.getSkillId());

            // Fetch user (student) details
            Map<String, Object> user = externalClient.getUserDetails(booking.getUserId());

            // Fetch provider (teacher) details
            Map<String, Object> provider = externalClient.getUserDetails(booking.getProviderId());

            // Build complete event
            BookingCreatedEvent event = BookingCreatedEvent.builder()
                    .eventType("BOOKING_CREATED")
                    .bookingId(booking.getId())

                    // User (student) details
                    .userId(booking.getUserId())
                    .userName((String) user.get("name"))
                    .userEmail((String) user.get("email"))

                    // Provider (teacher) details
                    .providerId(booking.getProviderId())
                    .providerName((String) provider.get("name"))
                    .providerEmail((String) provider.get("email"))

                    // Skill details
                    .skillId(booking.getSkillId())
                    .skillName(skill != null ? skill.getName() : "Unknown Skill")

                    // Session time
                    .startTime(booking.getStartTime())
                    .endTime(booking.getEndTime())
                    .sessionTime(formatSessionTime(booking.getStartTime(), booking.getEndTime()))


                    // Price and status
                    .totalPrice(booking.getTotalPrice())
                    .status(booking.getStatus().name())
                    .message("")
                    .createdAt(booking.getCreatedAt())
                    .build();

            kafkaTemplate.send(bookingEventsTopic, "booking.created", event);
            log.info(" Published BookingCreatedEvent for booking {} with complete details", booking.getId());

        } catch (Exception e) {
            log.error(" Failed to publish BookingCreatedEvent for booking {}", booking.getId(), e);
        }
    }



    private void publishStatusChanged(Booking booking, Status oldStatus, Status newStatus, String reason) {
        try {
            // Fetch skill, user, and provider details
            ExternalServiceClient.SkillDetails skill = externalClient.getSkill(booking.getSkillId());
            Map<String, Object> user = externalClient.getUserDetails(booking.getUserId());
            Map<String, Object> provider = externalClient.getUserDetails(booking.getProviderId());

            // Build complete event
            BookingStatusChangedEvent event = BookingStatusChangedEvent.builder()
                    .eventType("BOOKING_STATUS_CHANGED")
                    .bookingId(booking.getId())

                    // User details
                    .userId(booking.getUserId())
                    .userName((String) user.getOrDefault("name", "User"))
                    .userEmail((String) user.getOrDefault("email", "user@example.com"))

                    // Provider details
                    .providerId(booking.getProviderId())
                    .providerName((String) provider.getOrDefault("name", "Provider"))
                    .providerEmail((String) provider.getOrDefault("email", "provider@example.com"))

                    // Skill details
                    .skillId(booking.getSkillId())
                    .skillName(skill != null ? skill.getName() : "Unknown Skill")

                    // Session time
                    .startTime(booking.getStartTime())
                    .endTime(booking.getEndTime())
                    .sessionTime(formatSessionTime(booking.getStartTime(), booking.getEndTime()))

                    // Status change
                    .oldStatus(oldStatus.name())
                    .newStatus(newStatus.name())
                    .reason(reason)
                    .changedAt(OffsetDateTime.now())
                    .build();

            kafkaTemplate.send(bookingEventsTopic, "booking.status.changed", event);
            log.info(" Published BookingStatusChangedEvent for booking {} with complete details", booking.getId());

        } catch (Exception e) {
            log.error(" Failed to publish BookingStatusChangedEvent for booking {}", booking.getId(), e);
        }
    }
    private String formatSessionTime(OffsetDateTime startTime, OffsetDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return startTime.format(formatter) + " - " + endTime.format(formatter);
    }

}







