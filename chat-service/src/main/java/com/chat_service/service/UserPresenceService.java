package com.chat_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserPresenceService {

    // Thread-safe map to store user heartbeat timestamps
    private final ConcurrentHashMap<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    // Fallback set for WebSocket connections (legacy support)
    private final Set<String> connectedSockets = ConcurrentHashMap.newKeySet();

    public void markUserOnline(String userId) {
        connectedSockets.add(userId);
        updateHeartbeat(userId); // Also update heartbeat
        log.info("User marked online (Socket): {}", userId);
    }

    public void markUserOffline(String userId) {
        connectedSockets.remove(userId);
        // We don't remove from heartbeat immediately, let it expire
        log.info("User marked offline (Socket): {}", userId);
    }

    public void updateHeartbeat(String userId) {
        lastHeartbeat.put(userId, System.currentTimeMillis());
    }

    public boolean isUserOnline(String userId) {
        return getOnlineUsers().contains(userId);
    }

    public Set<String> getOnlineUsers() {
        // Cleanup expired heartbeats (e.g. older than 45 seconds)
        long now = System.currentTimeMillis();
        long threshold = 45000; // 45 seconds

        // Remove expired
        lastHeartbeat.entrySet().removeIf(entry -> (now - entry.getValue()) > threshold);

        // Combine socket users and heartbeat users
        Set<String> activeUsers = ConcurrentHashMap.newKeySet();
        activeUsers.addAll(connectedSockets);
        activeUsers.addAll(lastHeartbeat.keySet());

        return Collections.unmodifiableSet(activeUsers);
    }
}
