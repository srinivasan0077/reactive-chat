package com.veprojects.reactivechat.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veprojects.reactivechat.entities.Message;
import com.veprojects.reactivechat.entities.Room;
import com.veprojects.reactivechat.entities.User;
import com.veprojects.reactivechat.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {

    @Autowired
    private ChatService chatService;
    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger LOGGER=Logger.getLogger(ReactiveWebSocketHandler.class.getName());

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Long roomId = extractRoomId(session);

        Mono<Authentication> authMono = session.getHandshakeInfo().getPrincipal()
                .cast(Authentication.class);

        Mono<Void> incoming = authMono.flatMapMany(auth ->
                session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .flatMap(json -> handleClientMessage(json, roomId, auth))
        ).then();

        Flux<WebSocketMessage> outgoing = chatService
                .subscribeToRoom(roomId)
                .map(msg -> session.textMessage(toJson(msg)));

        return session.send(outgoing)
                .and(incoming)
                .onErrorResume(ex -> {
                    LOGGER.log(Level.SEVERE, "WebSocket error", ex);
                    return Mono.empty();
                });
    }

    private Long extractRoomId(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        if (query == null) throw new IllegalArgumentException("Missing roomId");

        for (String param : query.split("&")) {
            String[] parts = param.split("=");
            if (parts.length == 2 && parts[0].equals("roomId")) {
                return Long.parseLong(parts[1]);
            }
        }
        throw new IllegalArgumentException("Missing roomId");
    }

    private Mono<Void> handleClientMessage(String json, Long roomId, Authentication authentication) {
        try {
            User user= (User) authentication.getPrincipal();
            Message message = objectMapper.readValue(json, Message.class);
            message.setRoom(new Room().setId(roomId));
            message.setCreatedAt(Instant.now());
            message.setSender(user);
            return chatService.sendMessage(message).then();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Error while sending message:",e);
        }
        return Mono.empty();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
