package com.veprojects.reactivechat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veprojects.reactivechat.entities.Message;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;


import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class KafkaReactiveConsumer {


    @Autowired
    private  KafkaReceiver<String, String> receiver;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger log=Logger.getLogger(KafkaReactiveConsumer.class.getName());

    public KafkaReactiveConsumer(){
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Autowired
    private ChatService chatService;


    @PostConstruct
    public void subscribe() {
        receiver.receive()
                .doOnNext(record -> log.info("Received message: " + record.value()))
                .publishOn(Schedulers.parallel())
                .flatMap(record -> chatService.sendMessage(deserialize(record.value()))
                        .doFinally(signal -> record.receiverOffset().acknowledge()), 5) // concurrency
                .doOnError(e -> log.log(Level.SEVERE,"Kafka error : ",e))
                .subscribe();
    }

    private static Message deserialize(String json) {
        try {
            return objectMapper.readValue(json, Message.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize: " + json, e);
        }
    }

}
