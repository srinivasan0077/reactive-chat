package com.veprojects.reactivechat.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.veprojects.reactivechat.entities.Message;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class KafkaReactiveProducer {

    private static final Logger log = Logger.getLogger(KafkaReactiveProducer.class.getName());

    private static final String TOPIC = "chat-messages";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private KafkaSender<String, String> kafkaSender;

    public KafkaReactiveProducer(){
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public Mono<Void> sendMessage(Message message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Failed to serialize message", e));
        }

        String key = message.getId() != null ? String.valueOf(message.getId()) : UUID.randomUUID().toString();

        ProducerRecord<String, String> producerRecord =
                new ProducerRecord<>(TOPIC, key, json);

        SenderRecord<String, String, String> senderRecord =
                SenderRecord.create(producerRecord, key);

        return kafkaSender.send(Mono.just(senderRecord))
                .doOnNext(result -> {
                    RecordMetadata metadata = result.recordMetadata();
                    log.info("Sent to partition=" + metadata.partition()
                            + ", offset=" + metadata.offset());
                })
                .doOnError(e -> log.log(Level.SEVERE, "Kafka send error", e))
                .then();
    }
}
