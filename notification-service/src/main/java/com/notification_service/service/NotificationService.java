package com.notification_service.service;

import com.notification_service.DTO.*;
import com.notification_service.entity.NotificationLog;
import com.notification_service.entity.NotificationStatus;
import com.notification_service.entity.NotificationType;
import com.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final EmailService emailService;
//    private final PushNotificationService pushNotificationService;
//    private final SMSService smsService;
    private final NotificationLogRepository notificationLogRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ========== BOOKING NOTIFICATIONS ==========

    public void sendBookingRequestNotification(BookingCreatedEvent event) {
        log.info(" Sending booking request notification to provider: {}", event.getProviderEmail());

        Context context = new Context();
        context.setVariable("teacherName", event.getProviderName());
        context.setVariable("studentName", event.getUserName());
        context.setVariable("skillName", event.getSkillName());
        context.setVariable("sessionTime", event.getSessionTime());
        context.setVariable("totalPrice", event.getTotalPrice());
        context.setVariable("message", event.getMessage() != null ? event.getMessage() : "");
        context.setVariable("bookingId", event.getBookingId());
        context.setVariable("status", event.getStatus());

        String subject = "🔔 New Booking Request from " + event.getUserName();
        boolean success = emailService.sendEmail(
                event.getProviderEmail(),
                subject,
                "booking-request",
                context
        );

        saveNotificationLog(
                UUID.fromString(event.getProviderId()),
                event.getProviderEmail(),
                NotificationType.EMAIL,
                "BOOKING",
                subject,
                "New booking request notification",
                success ? NotificationStatus.SENT : NotificationStatus.FAILED,
                event.getBookingId(),
                "BOOKING",
                success ? null : "Email delivery failed"
        );

        // Also notify the user (student) that booking was created
        sendBookingCreatedConfirmationToUser(event);
    }

    private void sendBookingCreatedConfirmationToUser(BookingCreatedEvent event) {
        log.info(" Sending booking created confirmation to user: {}", event.getUserEmail());

        Context context = new Context();
        context.setVariable("studentName", event.getUserName());
        context.setVariable("teacherName", event.getProviderName());
        context.setVariable("skillName", event.getSkillName());
        context.setVariable("sessionTime", event.getSessionTime());
        context.setVariable("totalPrice", event.getTotalPrice());
        context.setVariable("bookingId", event.getBookingId());
        context.setVariable("status", event.getStatus());

        String subject = " Booking Request Submitted - " + event.getSkillName();
        boolean success = emailService.sendEmail(
                event.getUserEmail(),
                subject,
                "booking-created-user",
                context
        );

        saveNotificationLog(
                UUID.fromString(event.getUserId()),
                event.getUserEmail(),
                NotificationType.EMAIL,
                "BOOKING",
                subject,
                "Booking created confirmation to user",
                success ? NotificationStatus.SENT : NotificationStatus.FAILED,
                event.getBookingId(),
                "BOOKING",
                success ? null : "Email delivery failed"
        );
    }

    public void sendBookingConfirmedNotification(BookingCreatedEvent event) {
        log.info(" Sending booking confirmed notification to user: {}", event.getUserEmail());

        Context context = new Context();
        context.setVariable("studentName", event.getUserName());
        context.setVariable("teacherName", event.getProviderName());
        context.setVariable("skillName", event.getSkillName());
        context.setVariable("sessionTime", event.getSessionTime());
        context.setVariable("totalPrice", event.getTotalPrice());
        context.setVariable("bookingId", event.getBookingId());

        String subject = " Booking Confirmed with " + event.getProviderName();
        boolean success = emailService.sendEmail(
                event.getUserEmail(),
                subject,
                "booking-confirmed",
                context
        );

        saveNotificationLog(
                UUID.fromString(event.getUserId()),
                event.getUserEmail(),
                NotificationType.EMAIL,
                "BOOKING",
                subject,
                "Booking confirmation notification",
                success ? NotificationStatus.SENT : NotificationStatus.FAILED,
                event.getBookingId(),
                "BOOKING",
                success ? null : "Email delivery failed"
        );
    }


    public void sendBookingRejectedNotification(BookingCreatedEvent event) {
        log.info(" Sending booking rejected notification to user: {}", event.getUserEmail());

        Context context = new Context();
        context.setVariable("studentName", event.getUserName());
        context.setVariable("teacherName", event.getProviderName());
        context.setVariable("skillName", event.getSkillName());

        String subject = " Booking Request Declined - " + event.getSkillName();
        boolean success = emailService.sendEmail(
                event.getUserEmail(),
                subject,
                "booking-rejected",
                context
        );

        saveNotificationLog(
                UUID.fromString(event.getUserId()),
                event.getUserEmail(),
                NotificationType.EMAIL,
                "BOOKING",
                subject,
                "Booking rejection notification",
                success ? NotificationStatus.SENT : NotificationStatus.FAILED,
                event.getBookingId(),
                "BOOKING",
                success ? null : "Email delivery failed"
        );
    }

    // ========== REVIEW NOTIFICATIONS ==========

    public void sendNewReviewNotification(ReviewCreatedEvent event) {
        log.info(" Sending new review notification to teacher: {}", event.getTeacherEmail());

        Context context = new Context();
        context.setVariable("teacherName", event.getTeacherName());
        context.setVariable("reviewerName", event.getReviewerName());
        context.setVariable("rating", event.getRating());
        context.setVariable("comment", event.getComment());

        String subject = " You received a new " + event.getRating() + "-star review!";
        boolean success = emailService.sendEmail(event.getTeacherEmail(), subject, "new-review", context);

        saveNotificationLog(
                event.getTeacherId(),
                event.getTeacherEmail(),
                NotificationType.EMAIL,
                "REVIEW",
                subject,
                "New review notification",
                success ? NotificationStatus.SENT : NotificationStatus.FAILED,
                event.getReviewId().toString(),
                "REVIEW",
                success ? null : "Email delivery failed"
        );
    }

    // ========== MESSAGE NOTIFICATIONS ==========

    public void sendNewMessageNotification(MessageReceivedEvent event) {
        log.info(" Sending new message notification to: {}", event.getReceiverEmail());

        Context context = new Context();
        context.setVariable("senderName", event.getSenderName());
        context.setVariable("messagePreview", truncateMessage(event.getMessageContent(), 100));

        String subject = " New message from " + event.getSenderName();
        boolean success = emailService.sendEmail(event.getReceiverEmail(), subject, "new-message", context);

        saveNotificationLog(
                event.getReceiverId(),
                event.getReceiverEmail(),
                NotificationType.EMAIL,
                "MESSAGE",
                subject,
                "New message notification",
                success ? NotificationStatus.SENT : NotificationStatus.FAILED,
                event.getMessageId().toString(),
                "MESSAGE",
                success ? null : "Email delivery failed"
        );
    }

    // ========== USER NOTIFICATIONS ==========

    public void sendWelcomeEmail(UserRegisteredEvent event) {
        log.info(" Sending welcome email to: {}", event.getEmail());

        Context context = new Context();
        context.setVariable("userName", event.getName());

        String subject = " Welcome to Skill Exchange Platform!";
        boolean success = emailService.sendEmail(event.getEmail(), subject, "welcome-email", context);

        saveNotificationLog(
                event.getUserId(),
                event.getEmail(),
                NotificationType.EMAIL,
                "USER",
                subject,
                "Welcome email",
                success ? NotificationStatus.SENT : NotificationStatus.FAILED,
                event.getUserId().toString(),
                "USER",
                success ? null : "Email delivery failed"
        );
    }

    // ========== SKILL MATCH NOTIFICATIONS ==========

    public void sendSkillMatchNotification(SkillMatchFoundEvent event) {
        log.info(" Sending skill match notification");

        // Notify learner
        Context learnerContext = new Context();
        learnerContext.setVariable("learnerName", event.getLearnerName());
        learnerContext.setVariable("teacherName", event.getTeacherName());
        learnerContext.setVariable("skillName", event.getSkillName());

        String learnerSubject = " Great News! Someone can teach you " + event.getSkillName();
        boolean learnerSuccess = emailService.sendEmail(event.getLearnerEmail(), learnerSubject, "skill-match-learner", learnerContext);

        saveNotificationLog(
                event.getLearnerId(),
                event.getLearnerEmail(),
                NotificationType.EMAIL,
                "MATCHING",
                learnerSubject,
                "Skill match found notification",
                learnerSuccess ? NotificationStatus.SENT : NotificationStatus.FAILED,
                event.getMatchId().toString(),
                "MATCH",
                learnerSuccess ? null : "Email delivery failed"
        );

        // Notify teacher
        Context teacherContext = new Context();
        teacherContext.setVariable("teacherName", event.getTeacherName());
        teacherContext.setVariable("learnerName", event.getLearnerName());
        teacherContext.setVariable("skillName", event.getSkillName());

        String teacherSubject = " Someone wants to learn " + event.getSkillName() + " from you!";
        boolean teacherSuccess = emailService.sendEmail(event.getTeacherEmail(), teacherSubject, "skill-match-teacher", teacherContext);

        saveNotificationLog(
                event.getTeacherId(),
                event.getTeacherEmail(),
                NotificationType.EMAIL,
                "MATCHING",
                teacherSubject,
                "Skill match found notification",
                teacherSuccess ? NotificationStatus.SENT : NotificationStatus.FAILED,
                event.getMatchId().toString(),
                "MATCH",
                teacherSuccess ? null : "Email delivery failed"
        );
    }


    private void saveNotificationLog(UUID userId, String userEmail, NotificationType type,
                                     String channel, String subject, String content,
                                     NotificationStatus status, String referenceId,
                                     String referenceType, String errorMessage) {
        NotificationLog log = NotificationLog.builder()
                 .userId(userId.toString())
                .userEmail(userEmail)
                .type(type)
                .channel(channel)
                .subject(subject)
                .content(content)
                .status(status)
                .errorMessage(errorMessage)
                .sentAt(status == NotificationStatus.SENT ? LocalDateTime.now() : null)
                .createdAt(LocalDateTime.now())
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();

        notificationLogRepository.save(log);
    }

    private String truncateMessage(String message, int maxLength) {
        if (message == null) return "";
        return message.length() > maxLength ? message.substring(0, maxLength) + "..." : message;
    }
}
