package com.veprojects.reactivechat.services;

import com.veprojects.reactivechat.broadcast.MessageBroadCaster;
import com.veprojects.reactivechat.entities.Message;
import com.veprojects.reactivechat.entities.Room;
import com.veprojects.reactivechat.entities.User;
import com.veprojects.reactivechat.repository.MessageRepository;
import com.veprojects.reactivechat.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatService {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MessageBroadCaster messageBroadCaster;

    public Mono<Message> sendMessage(Message message) {
        return messageRepository.createMessage(message)
                .doOnNext(saved -> messageBroadCaster.publishToRoom(saved));
    }

    public Flux<Message> subscribeToRoom(Long roomId) {
        return messageBroadCaster.subscribeToRoom(roomId);
    }

    public Flux<Message> getMessageHistory(Long roomId, Long page, int size) {
        return messageRepository.getMessagesOfRoom(roomId,page,size);
    }

    public Mono<Room> createRoom(Room room) {
        return roomRepository.createRoom(room);
    }
}
