package com.notification_service.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification_service.DTO.BookingCreatedEvent;
import com.notification_service.DTO.BookingStatusChangedEvent;
import com.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "booking-events", groupId = "notification-service-group")
    public void consumeBookingEvents(String message) {
        try {
            log.info(" Received booking-events message: {}", message);

            //  Parse with ObjectMapper that has JavaTimeModule
            BookingCreatedEvent event = objectMapper.readValue(message, BookingCreatedEvent.class);
            BookingStatusChangedEvent changedEvent = objectMapper.readValue(message, BookingStatusChangedEvent.class);

            String eventType = event.getEventType();
            log.info(" Event type: {}", eventType);

            switch (eventType) {
                case "BOOKING_CREATED":
                    notificationService.sendBookingRequestNotification(event);
                    log.info("✓ Processed BOOKING_CREATED for booking: {}", event.getBookingId());
                    break;

                case "BOOKING_STATUS_CHANGED":
                    String status = changedEvent.getNewStatus(); // Get the status from event
                    log.info("Debug - Status value: {}", status);
                    if ("CONFIRMED".equals(status)) {
                        notificationService.sendBookingConfirmedNotification(event);
                        log.info(" Processed BOOKING_CONFIRMED for booking: {}", event.getBookingId());
                    } else if ("CANCELLED".equals(status) || "REJECTED".equals(status)) {
                        notificationService.sendBookingRejectedNotification(event);
                        log.info(" Processed {} for booking: {}", status, event.getBookingId());
                    } else {
                        log.warn(" Unknown booking status: {}", status);
                    }
                    break;

                default:
                    log.warn(" Unknown event type: {}", eventType);
            }
            

        } catch (Exception e) {
            log.error(" Error processing booking-events message: {}", e.getMessage(), e);
        }
    }



}
