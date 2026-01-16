package com.notification_service.config;


import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;

@Configuration
@Slf4j
@Data
public class OneSignalConfig {

    @Value("${onesignal.app-id}")
    private String appId;

    @Value("${onesignal.rest-api-key:}")
    private String restApiKey;

    @Value("${onesignal.enabled:false}")
    private boolean enabled;

    private static final String ONESIGNAL_API_URL = "https://onesignal.com/api/v1/notifications";
    private static final String ONESIGNAL_PLAYERS_URL = "https://onesignal.com/api/v1/players";

    @PostConstruct
    public void initialize(){
        log.info(" ONESIGNAL INITIALIZATION");

        if (!enabled){
            log.warn("OneSignal is DISABLED in configuration");
            return;
        }
        if (isConfigured()){
            log.info(" OneSignal App ID: {}", maskAppId(appId));
            log.info(" OneSignal REST API Key: {}", maskApiKey(restApiKey));
            log.info(" OneSignal Status: ENABLED");
            log.info(" Push Notifications: READY");
        }else {
            log.error(" OneSignal NOT CONFIGURED!");
            log.error(" Please set onesignal.app-id and onesignal.rest-api-key in application.yml");
            log.warn(" Push notifications will be DISABLED");
        }

    }

    public boolean isConfigured(){
        return enabled
                && appId != null && !appId.isEmpty()
                && restApiKey != null && !restApiKey.isEmpty();
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiUrl() {
        return ONESIGNAL_API_URL;
    }

    public String getPlayersUrl() {
        return ONESIGNAL_PLAYERS_URL;
    }

    public String getAuthorizationHeader() {
        return "Basic " + restApiKey;
    }


    private String maskAppId(String appId) {
        if (appId == null || appId.length() <= 8) return "****";
        return appId.substring(0, 8) + "****";
    }

    private String maskApiKey(String key) {
        if (key == null || key.length() <= 8) return "****";
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }


}
