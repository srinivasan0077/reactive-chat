package com.veprojects.reactivechat.configurations;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean
    public ReceiverOptions<String, String> kafkaReceiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "chat-consumer-group-"+System.getProperty("instance.id"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual ack

        ReceiverOptions<String, String> options = ReceiverOptions.create(props);
        return options.subscription(Collections.singleton("chat-messages"))
                .addAssignListener(partitions -> System.out.println("Assigned: " + partitions))
                .addRevokeListener(partitions -> System.out.println("Revoked: " + partitions));
    }

    @Bean
    public KafkaReceiver<String, String> kafkaReceiver(ReceiverOptions<String, String> options) {
        return KafkaReceiver.create(options);
    }

    @Bean
    public KafkaSender<String, String> kafkaSender() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);  // retry 3 times
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1); // wait 1ms to allow batching

        SenderOptions<String, String> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }



}
