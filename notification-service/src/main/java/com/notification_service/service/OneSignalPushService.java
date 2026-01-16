package com.notification_service.service;


import com.nimbusds.jose.shaded.gson.Gson;

import com.notification_service.config.OneSignalConfig;
import com.notification_service.entity.PushNotification;
import com.notification_service.entity.UserDeviceToken;
import com.notification_service.repository.UserDeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OneSignalPushService {


    private final OneSignalConfig oneSignalConfig;
    private final RestTemplate restTemplate;
    private final Gson gson = new Gson();
    private final UserDeviceTokenRepository userDeviceTokenRepository;


    // Send push notification to a specific user by external user ID
    public String sendToUser(String userId, String title, String message, Map<String, String> data ){

        if (!oneSignalConfig.isConfigured()){
            log.warn(" OneSignal is not configured. Skipping notification.");
            return null;
        }

        try {


            log.info(" Sending OneSignal notification to user: {}", userId);




            Map<String, Object> payload = buildNotificationPayload(
                    List.of(userId.toString()),
                    title,
                    message,
                    data,
                    NotificationTargetType.EXTERNAL_USER_ID
            );
            return sendNotification(payload);

        }catch (Exception e) {
            log.error("Failed to send OneSignal notification to user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    //Send push notification to multiple users

    public String sendToMultipleUsers(List<String> userIds, String title, String message, Map<String, String> data){
        if (!oneSignalConfig.isConfigured()) {
            log.warn(" OneSignal is not configured. Skipping notification.");
            return null;
        }

        try {



            log.info("Sending OneSignal notification to {} users", userIds.size());

            Map<String, Object> payload = buildNotificationPayload(
                    userIds,
                    title,
                    message,
                    data,
                    NotificationTargetType.EXTERNAL_USER_ID
            );

            return sendNotification(payload);
        }catch (Exception e) {
            log.error(" Failed to send OneSignal notification to multiple users: {}", e.getMessage());
            return null;
        }
    }

    //Send broadcast notification to all users

    public String sendToAll(String title, String message, Map<String, String> data){
        if (!oneSignalConfig.isConfigured()) {
            log.warn(" OneSignal is not configured. Skipping notification.");
            return null;
        }

        try {
            log.info(" Broadcasting OneSignal notification to all users");


            Map<String, Object> payload = buildNotificationPayload(
                    List.of("All"),
                    title,
                    message,
                    data,
                    NotificationTargetType.SEGMENT
            );

            return sendNotification(payload);
        } catch (Exception e) {
            log.error(" Failed to broadcast OneSignal notification: {}", e.getMessage());
            return null;
        }
    }

    //Send notification with image attachment

    public String sendWithImage(String  userId, String title, String message, String imageUrl ,  Map<String, String> data){
        if (!oneSignalConfig.isConfigured()) {
            log.warn(" OneSignal is not configured. Skipping notification.");
            return null;
        }

        try {


            log.info(" Sending OneSignal notification with image to user: {}", userId);

            Map<String, Object> payload = buildNotificationPayload(
                    List.of(userId.toString()),
                    title,
                    message,
                    data,
                    NotificationTargetType.EXTERNAL_USER_ID
            );

            // Add image for rich notification
            payload.put("big_picture", imageUrl);
            payload.put("ios_attachments", Map.of("id", imageUrl));
            payload.put("chrome_web_image", imageUrl);

            return sendNotification(payload);
        }catch (Exception e) {
            log.error(" Failed to send OneSignal notification with image: {}", e.getMessage());
            return null;
        }

    }


    public String sendWithAction(String userId, String title, String message, String url, Map<String, String> data) {
        if (!oneSignalConfig.isConfigured()) {
            log.warn("⚠️ OneSignal is not configured. Skipping notification.");
            return null;
        }

        try {


            log.info(" Sending OneSignal notification with action URL to user: {}", userId);


            Map<String, Object> payload = buildNotificationPayload(
                    List.of(userId.toString()),
                    title,
                    message,
                    data,
                    NotificationTargetType.EXTERNAL_USER_ID
            );

            // Add clickable URL
            payload.put("url", url); // When user clicks notification, opens this URL

            return sendNotification(payload);

        } catch (Exception e) {
            log.error(" Failed to send OneSignal notification with action: {}", e.getMessage());
            return null;
        }
    }

    //Send notification from PushNotification entity

    public String sendPushNotification(PushNotification pushNotification, String userId) {
        Map<String, String> data = new HashMap<>();
        data.put("type", pushNotification.getType());
        data.put("actionUrl", pushNotification.getActionUrl());
        data.put("referenceId", pushNotification.getReferenceId());
        data.put("priority", pushNotification.getPriority().toString());

        // Use sendWithAction to make notification clickable
        String fullUrl = "http://localhost:5174/" + pushNotification.getActionUrl();

        return sendWithAction(
                userId,
                pushNotification.getTitle(),
                pushNotification.getMessage(),
                fullUrl,
                data
        );
    }



    // ========== PRIVATE HELPER METHODS ==========

    private Map<String,Object> buildNotificationPayload(
            List<String> targets,
            String title,
            String message,
            Map<String, String> data,
            NotificationTargetType targetType
    ) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("app_id", oneSignalConfig.getAppId());

        if (targetType == NotificationTargetType.EXTERNAL_USER_ID) {
            payload.put("include_external_user_ids", targets);
        } else if (targetType == NotificationTargetType.SEGMENT) {
            payload.put("included_segments", targets);
        }

        payload.put("headings", Map.of("en", title));
        payload.put("contents", Map.of("en", message));

        if (data != null && !data.isEmpty()) {
            payload.put("data", data);
        }

        payload.put("ttl", 259200);
        payload.put("web_push_topic", "notification");

        return payload;
        }

        // Notification content
//        Map<String, String> contents = new HashMap<>();
//        contents.put("en", message);
//        payload.put("contents", contents);
//
//        Map<String, String> headings = new HashMap<>();
//        headings.put("en", title);
//        payload.put("headings", headings);
//
//        // Custom data
//        if (data != null && !data.isEmpty()) {
//            payload.put("data", data);
//        }
//
//        // Platform-specific configurations
//        configurePlatformSettings(payload);
//
//        return payload;
//    }

    //Configure platform-specific settings
    private void configurePlatformSettings(Map<String, Object> payload) {
//        // Android configuration
//        payload.put("android_channel_id", "skill-exchange-notifications");
//        payload.put("priority", 10); // Max priority
//        payload.put("android_accent_color", "FF6B6B00");
//        payload.put("small_icon", "ic_notification");
//        payload.put("large_icon", "ic_launcher");

//        // iOS configuration
//        payload.put("ios_sound", "notification.wav");
//        payload.put("ios_badgeType", "Increase");
//        payload.put("ios_badgeCount", 1);
//        payload.put("content_available", true);

        // Web configuration
        payload.put("web_push_topic", "skill-exchange");

        // Delivery optimization
        payload.put("ttl", 259200); // 3 days in seconds
        payload.put("delivery_time_of_day", "9:00AM"); // Optimize delivery time
    }

    //Send notification to OneSignal API

    private String sendNotification(Map<String, Object> payload) {
        try {
            String jsonPayload = gson.toJson(payload);
            log.debug(" OneSignal Payload: {}", jsonPayload);



            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", oneSignalConfig.getAuthorizationHeader());

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    oneSignalConfig.getApiUrl(),
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.debug(" OneSignal Response: {}", responseBody);

                Map<String, Object> responseMap = gson.fromJson(responseBody, Map.class);
                String notificationId = (String) responseMap.get("id");
                Double recipients = (Double) responseMap.getOrDefault("recipients", 0.0);

                log.info(" OneSignal notification sent successfully");
                log.info(" Notification ID: {}", notificationId);
                log.info(" Recipients: {}", recipients.intValue());

                return notificationId;
            } else {
                log.error(" OneSignal API error: Status {}", response.getStatusCode());
                log.error(" Response: {}", response.getBody());
                return null;
            }

        } catch (RestClientException e) {
            log.error(" OneSignal REST client error: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error(" Unexpected error sending OneSignal notification: {}", e.getMessage(), e);
            return null;
        }
    }


    private enum NotificationTargetType{
        EXTERNAL_USER_ID,
        PLAYER_ID,
        SEGMENT
    }

}


