package tech.sjiale.hoyo_achievement_server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service("sseService")
public class SseServiceImpl {

    private final Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * Create a new SSE connection for a user
     *
     * @param userId   user id
     * @param clientId client id
     * @return SseEmitter
     */
    public SseEmitter connect(long userId, String clientId) {
        // Create a new SseEmitter
        SseEmitter emitter = new SseEmitter(0L); // 0L means no timeout

        // Add the emitter to the map
        emitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(clientId, emitter);

        // Set up event handlers
        emitter.onCompletion(() -> removeEmitter(userId, clientId));
        emitter.onTimeout(() -> removeEmitter(userId, clientId));
        emitter.onError(e -> removeEmitter(userId, clientId));

        // Send handshake event right after connection is established
        try {
            emitter.send(SseEmitter.event().name("handshake").data("established"));
        } catch (IOException e) {
            log.error("Failed to send handshake event to client {} for user {}", clientId, userId);
            removeEmitter(userId, clientId);
        }

        log.info("User {} and client {} connected to SSE.", userId, clientId);
        return emitter;
    }

    private void removeEmitter(long userId, String clientId) {
        // Get the emitter map by user ID
        Map<String, SseEmitter> map = emitters.get(userId);

        // Remove the emitter from the map by client ID
        if (map != null) {
            map.remove(clientId);
            if (map.isEmpty()) {
                emitters.remove(userId);
            }
        }

        log.info("Removed SSE emitter for user {} and client {}", userId, clientId);
    }

    /**
     * Broadcast an update to all connected clients by user ID
     *
     * @param userId         user ID, 0 for all users
     * @param originClientId the client ID of the user who sent the update
     * @param payload        the payload to send
     */
    public void broadcastUpdate(long userId, String originClientId, Object payload) {
        // Check if the payload is not null
        if (payload == null) {
            return;
        }

        if (userId == 0) {
            // Broadcast to all users
            for (Map<String, SseEmitter> map : emitters.values()) {
                broadcastToUser(map, userId, originClientId, payload);
            }
        } else {
            // Get the emitter map by user ID
            Map<String, SseEmitter> map = emitters.get(userId);
            broadcastToUser(map, userId, originClientId, payload);
        }
    }

    private void broadcastToUser(Map<String, SseEmitter> map, long userId, String originClientId, Object payload) {
        if (map == null || map.isEmpty()) {
            return;
        }

        map.forEach((clientId, emitter) -> {
            if (!clientId.equals(originClientId)) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("data_changed")
                            .data(payload));
                    log.info("Sent SSE update to user {}, client {}", userId, clientId);
                } catch (IOException e) {
                    log.error("Failed to send SSE update to user {}, client {}", userId, clientId);
                    removeEmitter(userId, clientId);
                }
            }
        });
    }
}
