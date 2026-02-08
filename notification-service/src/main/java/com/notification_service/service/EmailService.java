package com.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String senderEmail;

    public boolean sendEmail(String to, String subject, String templateName, Context context) {
        try {
            log.info(" Attempting to send email to: {}", to);

            // Validate recipient email
            if (to == null || to.trim().isEmpty()) {
                log.error(" Recipient email is empty!");
                return false;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Process template
            String htmlContent = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(senderEmail); // Use configured username

            mailSender.send(message);

            log.info(" Email sent successfully to: {}", to);
            return true;

        } catch (MessagingException e) {
            log.error(" Failed to send email to {}: {}", to, e.getMessage());
            log.error(" Stack trace: ", e);
            return false;

        } catch (Exception e) {
            log.error(" Unexpected error sending email to {}: {}", to, e.getMessage());
            log.error(" Stack trace: ", e);
            return false;
        }
    }

    public boolean sendSimpleEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            helper.setFrom("skillexchange.noreply@gmail.com");

            mailSender.send(message);
            log.info(" Simple email sent successfully to: {}", to);
            return true;
        } catch (MessagingException e) {
            log.error(" Failed to send simple email to: {}", to, e);
            return false;
        }
    }
}
