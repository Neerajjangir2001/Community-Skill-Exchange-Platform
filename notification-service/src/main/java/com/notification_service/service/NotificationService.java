package com.notification_service.service;

import com.notification_service.DTO.*;
import com.notification_service.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final WebSocketNotificationService webSocketService;
    private final OneSignalPushService oneSignalPushService;
    private final NotificationLogService notificationLogService;


    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ========== BOOKING NOTIFICATIONS ==========


     //Send booking request notification to provider
    public void sendBookingRequestNotification(BookingCreatedEvent event) {

        log.info(" BOOKING REQUEST NOTIFICATION");

        log.info(" Booking ID: {}", event.getBookingId());
        log.info(" Provider: {} ({})", event.getProviderName(), event.getProviderEmail());
        log.info(" Student: {}", event.getUserName());
        log.info(" Skill: {}", event.getSkillName());

        UUID providerId = UUID.fromString(event.getProviderId());
        String title = "New Booking Request";
        String message = event.getUserName() + " wants to book " + event.getSkillName();

        try {
            // 1. Send Email
            sendBookingEmail(event, "booking-request", " New Booking Request from " + event.getUserName());

            // 2. Send WebSocket Notification
            sendWebSocketNotification(String.valueOf(providerId), title, message, "BOOKING", "/bookings/" + event.getBookingId());

            // 3. Send Push Notification
            sendPushNotification(String.valueOf(providerId), title, message, "BOOKING", "/bookings/" + event.getBookingId(), event.getBookingId());

            // 4. Notify Student (Confirmation)
            sendBookingCreatedConfirmationToUser(event);

            log.info(" Booking request notification completed");

        } catch (Exception e) {
            log.error(" Error sending booking request notification: {}", e.getMessage(), e);
        }


    }


     // Send booking confirmed notification to student

    public void sendBookingConfirmedNotification(BookingCreatedEvent event) {
        log.info(" Sending booking confirmed notification");

        String  userId = event.getUserId();
        String title = "Booking Confirmed!";
        String message = event.getProviderName() + " confirmed your " + event.getSkillName() + " session";

        // Email
        sendBookingEmail(event, "booking-confirmed", " Booking Confirmed with " + event.getProviderName());

        // WebSocket
        sendWebSocketNotification(userId, title, message, "BOOKING", "/bookings/" + event.getBookingId());

        // Push
        sendPushNotification(userId, title, message, "BOOKING", "/bookings/" + event.getBookingId(), event.getBookingId());

        log.info(" Booking confirmed notification sent");
    }


     // Send booking rejected notification to student

    public void sendBookingRejectedNotification(BookingCreatedEvent event) {
        log.info(" Sending booking rejected notification");

        String userId = event.getUserId();
        String title = "Booking Declined";
        String message = event.getProviderName() + " declined your " + event.getSkillName() + " request";

        // Email
        sendBookingEmail(event, "booking-rejected", " Booking Request Declined - " + event.getSkillName());

        // WebSocket
        sendWebSocketNotification(userId, title, message, "BOOKING", "/bookings/" + event.getBookingId());

        // Push
        sendPushNotification(userId, title, message, "BOOKING", "/bookings/" + event.getBookingId(), event.getBookingId());

        log.info(" Booking rejected notification sent");
    }

    // ========== REVIEW NOTIFICATIONS ==========


     // Send new review notification

    public void sendNewReviewNotification(ReviewCreatedEvent event) {
        log.info(" Sending new review notification");

        String title = "New Review Received";
        String message = event.getReviewerName() + " gave you " + event.getRating() + " stars!";

        // Email
        Context context = new Context();
        context.setVariable("teacherName", event.getTeacherName());
        context.setVariable("reviewerName", event.getReviewerName());
        context.setVariable("rating", event.getRating());
        context.setVariable("comment", event.getComment());

        emailService.sendEmail(
                event.getTeacherEmail(),
                " You received a new " + event.getRating() + "-star review!",
                "new-review",
                context
        );

        // WebSocket
        sendWebSocketNotification(String.valueOf(event.getTeacherId()), title, message, "REVIEW", "/reviews/" + event.getReviewId());

        // Push
        sendPushNotification(String.valueOf(event.getTeacherId()), title, message, "REVIEW", "/reviews/" + event.getReviewId(), event.getReviewId().toString());

        log.info(" Review notification sent");
    }

    // ========== MESSAGE NOTIFICATIONS ==========


     //Send new message notification

    public void sendNewMessageNotification(MessageReceivedEvent event) {

        log.info(" NEW MESSAGE NOTIFICATION");
        log.info(" From: {} ({})", event.getSenderName(), event.getSenderId());
        log.info(" To: {} ({})", event.getReceiverEmail(), event.getReceiverId());

        String title = "New Message from " + event.getSenderName();
        String message = truncateMessage(event.getMessageContent(), 100);

        try {
            // 1. Email
            Context context = new Context();
            context.setVariable("senderName", event.getSenderName());
            context.setVariable("messagePreview", message);

            emailService.sendEmail(event.getReceiverEmail(), " " + title, "new-message", context);

            // 2. WebSocket
            sendWebSocketNotification(String.valueOf(event.getReceiverId()), title, message, "MESSAGE", "/chat/" + event.getSenderId());

            // 3. Push Notification
            Map<String, String> data = new HashMap<>();
            data.put("type", "MESSAGE");
            data.put("senderId", event.getSenderId().toString());
            data.put("senderName", event.getSenderName());
            data.put("messageId", event.getMessageId().toString());
            data.put("actionUrl", "/chat/" + event.getSenderId());

            oneSignalPushService.sendToUser(String.valueOf(event.getReceiverId()), title, message, data);

            // 4. Save Log
            notificationLogService.saveLog(
                    event.getReceiverId(),
                    event.getReceiverEmail(),
                    "MESSAGE",
                    title,
                    message,
                    "message:" + event.getMessageId()
            );

            log.info(" Message notification completed");

        } catch (Exception e) {
            log.error(" Error sending message notification: {}", e.getMessage(), e);
        }


    }

    // ========== USER NOTIFICATIONS ==========


     //Send welcome email to new user

    public void sendWelcomeEmail(UserRegisteredEvent event) {
        log.info(" Sending welcome email to: {}", event.getEmail());

        String title = "Welcome to Skill Exchange!";
        String message = "Welcome aboard, " + event.getName() + "! Start learning and teaching today.";

        // Email
        Context context = new Context();
        context.setVariable("userName", event.getName());
        emailService.sendEmail(event.getEmail(), " Welcome to Skill Exchange Platform!", "welcome-email", context);

        // WebSocket
        sendWebSocketNotification(String.valueOf(event.getUserId()), title, message, "USER", "/profile");

        // Push
        sendPushNotification(String.valueOf(event.getUserId()), title, message, "USER", "/profile", event.getUserId().toString());

        log.info(" Welcome email sent");
    }

    // ========== SKILL MATCH NOTIFICATIONS ==========


     // Send skill match notification to both learner and teacher

    public void sendSkillMatchNotification(SkillMatchFoundEvent event) {
        log.info(" Sending skill match notifications");

        // Notify Learner
        String learnerTitle = "Skill Match Found!";
        String learnerMessage = event.getTeacherName() + " can teach you " + event.getSkillName();

        Context learnerContext = new Context();
        learnerContext.setVariable("learnerName", event.getLearnerName());
        learnerContext.setVariable("teacherName", event.getTeacherName());
        learnerContext.setVariable("skillName", event.getSkillName());

        emailService.sendEmail(event.getLearnerEmail(), " Great News! Someone can teach you " + event.getSkillName(), "skill-match-learner", learnerContext);
        sendWebSocketNotification(String.valueOf(event.getLearnerId()), learnerTitle, learnerMessage, "SKILL_MATCH", "/matches/" + event.getMatchId());
        sendPushNotification(String.valueOf(event.getLearnerId()), learnerTitle, learnerMessage, "SKILL_MATCH", "/matches/" + event.getMatchId(), event.getMatchId().toString());

        // Notify Teacher
        String teacherTitle = "New Learning Request";
        String teacherMessage = event.getLearnerName() + " wants to learn " + event.getSkillName() + " from you";

        Context teacherContext = new Context();
        teacherContext.setVariable("teacherName", event.getTeacherName());
        teacherContext.setVariable("learnerName", event.getLearnerName());
        teacherContext.setVariable("skillName", event.getSkillName());

        emailService.sendEmail(event.getTeacherEmail(), " Someone wants to learn " + event.getSkillName() + " from you!", "skill-match-teacher", teacherContext);
        sendWebSocketNotification(String.valueOf(event.getTeacherId()), teacherTitle, teacherMessage, "SKILL_MATCH", "/matches/" + event.getMatchId());
        sendPushNotification(String.valueOf(event.getTeacherId()), teacherTitle, teacherMessage, "SKILL_MATCH", "/matches/" + event.getMatchId(), event.getMatchId().toString());

        log.info(" Skill match notifications sent");
    }

    // ========== PRIVATE HELPER METHODS ==========


     // Send booking email (DRY principle)

    private void sendBookingEmail(BookingCreatedEvent event, String template, String subject) {
        Context context = new Context();
        context.setVariable("studentName", event.getUserName());
        context.setVariable("teacherName", event.getProviderName());
        context.setVariable("skillName", event.getSkillName());
        context.setVariable("sessionTime", event.getSessionTime());
        context.setVariable("totalPrice", event.getTotalPrice());
        context.setVariable("bookingId", event.getBookingId());
        context.setVariable("status", event.getStatus());
        context.setVariable("message", event.getMessage() != null ? event.getMessage() : "");

        emailService.sendEmail(event.getProviderEmail(), subject, template, context);
    }


     // Send booking confirmation to user
    private void sendBookingCreatedConfirmationToUser(BookingCreatedEvent event) {
        String userId = event.getUserId();
        String title = "Booking Request Sent";
        String message = "Your request for " + event.getSkillName() + " has been sent to " + event.getProviderName();

        Context context = new Context();
        context.setVariable("studentName", event.getUserName());
        context.setVariable("teacherName", event.getProviderName());
        context.setVariable("skillName", event.getSkillName());
        context.setVariable("sessionTime", event.getSessionTime());
        context.setVariable("totalPrice", event.getTotalPrice());
        context.setVariable("bookingId", event.getBookingId());
        context.setVariable("status", event.getStatus());

        emailService.sendEmail(event.getUserEmail(), " Booking Request Submitted - " + event.getSkillName(), "booking-created-user", context);
        sendWebSocketNotification(userId, title, message, "BOOKING", "/bookings/" + event.getBookingId());
        sendPushNotification(userId, title, message, "BOOKING", "/bookings/" + event.getBookingId(), event.getBookingId());
    }


     //Send WebSocket notification
    private void sendWebSocketNotification(String userId, String title, String message, String type, String actionUrl) {
        try {
            PushNotification notification = PushNotification.builder()
                    .recipientId(UUID.fromString(userId))
                    .title(title)
                    .message(message)
                    .type(type)
                    .actionUrl(actionUrl)
                    .priority(NotificationPriority.HIGH)
                    .build();

            webSocketService.sendToUser(UUID.fromString(userId), notification);
            log.info(" WebSocket notification sent to user: {}", userId);

        } catch (Exception e) {
            log.error(" Failed to send WebSocket notification: {}", e.getMessage());
        }
    }


     // Send push notification via OneSignal

    private void sendPushNotification(String userId, String title, String message, String type, String actionUrl, String referenceId) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("type", type);
            data.put("actionUrl", actionUrl);
            data.put("referenceId", referenceId);

            // Just pass the relative path - OneSignalPushService handles full URL
            String notificationId = oneSignalPushService.sendWithAction(
                    userId,
                    title,
                    message,
                    actionUrl,  // e.g., "/bookings/123" or "bookings/123"
                    data
            );

            if (notificationId != null) {
                log.info(" Push notification sent. ID: {}", notificationId);
            }

        } catch (Exception e) {
            log.error(" Failed to send push notification: {}", e.getMessage());
        }
    }


     // Truncate message to specified length
    private String truncateMessage(String message, int maxLength) {
        if (message == null) return "";
        return message.length() > maxLength ? message.substring(0, maxLength) + "..." : message;
    }
}
