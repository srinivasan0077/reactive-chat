package com.veprojects.reactivechat.controllers;

import com.veprojects.reactivechat.entities.Response;
import com.veprojects.reactivechat.entities.Room;
import com.veprojects.reactivechat.entities.User;
import com.veprojects.reactivechat.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/room")
    public Mono<ResponseEntity<Response>> createRoom(@RequestBody Room room, Mono<Authentication> authenticationMono){

        return authenticationMono
                .flatMap(authentication -> {
                    User user=(User) authentication.getPrincipal();
                    room.setCreatedBy(user);
                    return chatService.createRoom(room);
                })
                .map(r->ResponseEntity.ok(new Response("Room created successfully!",r)))
                .onErrorResume(e->Mono.just(ResponseEntity.badRequest().body(new Response(e.getMessage(),null))));
    }

}
