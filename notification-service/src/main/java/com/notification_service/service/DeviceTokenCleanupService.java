//package com.notification_service.service;
//
//
//import com.notification_service.entity.UserDeviceToken;
//import com.notification_service.repository.UserDeviceTokenRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class DeviceTokenCleanupService {
//
//    public final UserDeviceTokenRepository userDeviceTokenRepository;
//
//    @Scheduled(cron = "0 0 2 * * ?")
//    public void cleanupInactiveTokens(){
//        try {
//            LocalDateTime thirtyDaysAgo =  LocalDateTime.now().minusDays(30);
//
//            List<UserDeviceToken> inactiveTokens = userDeviceTokenRepository.findByActiveFalseAndLastUsedBefore(thirtyDaysAgo);
//            if(!inactiveTokens.isEmpty()){
//                userDeviceTokenRepository.deleteAll(inactiveTokens);
//                log.info(" Cleaned up {} inactive device tokens older than 30 days",
//                        inactiveTokens.size());
//            } else {
//                log.debug(" No inactive tokens to clean up");
//            }
//        }catch (Exception e) {
//            log.error(" Failed to clean up inactive tokens", e);
//        }
//    }
//
//
//    public void markTokenAsInactive(String deviceToken){
//        userDeviceTokenRepository.findByDeviceToken(deviceToken).ifPresent(userDeviceToken -> {
//            userDeviceToken.setActive(false);
//            userDeviceToken.setUpdatedAt(LocalDateTime.now());
//            userDeviceTokenRepository.save(userDeviceToken);
//            log.info(" Marked token as inactive: {}", userDeviceToken.getId());
//        });
//    }
//}
