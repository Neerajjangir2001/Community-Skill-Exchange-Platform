package com.notification_service.repository;

import com.notification_service.entity.UserDeviceToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceTokenRepository extends MongoRepository<UserDeviceToken, String> {

    List<UserDeviceToken> findByUserIdAndActiveTrue(String userId);
    List<UserDeviceToken> findByActiveTrue();
    boolean existsByDeviceToken(String deviceToken);
    Optional<UserDeviceToken> findByDeviceToken(String deviceToken);
    Optional<UserDeviceToken> findByDeviceTokenAndActive(String deviceToken, boolean active);


    // Find all devices for a user (active + inactive)
    List<UserDeviceToken> findByUserId(UUID userId);

    // Find specific user's device token
    Optional<UserDeviceToken> findByUserIdAndDeviceToken(UUID userId, String deviceToken);

    // Delete specific token
    void deleteByUserIdAndDeviceToken(UUID userId, String deviceToken);

    // For cleanup - find old inactive tokens
    List<UserDeviceToken> findByActiveFalseAndLastUsedBefore(LocalDateTime dateTime);

    // Count active devices for user
    long countByUserIdAndActive(UUID userId, boolean active);
}
