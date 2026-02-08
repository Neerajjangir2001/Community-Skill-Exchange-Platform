package com.bookingservice.bookingservice.kafka;

import com.bookingservice.bookingservice.events.UserDeletedEvent;
import com.bookingservice.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {

    private final BookingRepository bookingRepository;

    @KafkaListener(topics = "${kafka.topic.user-deleted:user-deleted}", groupId = "booking-service-group")
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Received UserDeletedEvent for userId: {}", event.getUserId());
        try {
            // Delete bookings where user is the student
            bookingRepository.deleteByUserId(event.getUserId());
            log.info("Deleted student bookings for userId: {}", event.getUserId());

            // Delete bookings where user is the provider
            bookingRepository.deleteByProviderId(event.getUserId());
            log.info("Deleted provider bookings for userId: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Failed to delete bookings for userId: {}", event.getUserId(), e);
        }
    }
}
