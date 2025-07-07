package com.veprojects.reactivechat.broadcast;

import com.veprojects.reactivechat.entities.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MessageBroadCaster {

    private static class RoomSink {
        final Sinks.Many<Message> sink;
        final AtomicInteger subscriberCount = new AtomicInteger(0);

        RoomSink(Sinks.Many<Message> sink) {
            this.sink = sink;
        }
    }

    private static Map<Long, RoomSink> rooms=new ConcurrentHashMap<>();

    public void publishToRoom(Message message){
        RoomSink roomSinkWrapper = rooms.computeIfAbsent(
                message.getRoom().getId(),
                id -> new RoomSink(Sinks.many().multicast().onBackpressureBuffer())
        );

        roomSinkWrapper.sink.tryEmitNext(message);
    }

    public Flux<Message> subscribeToRoom(Long roomId){
        RoomSink roomSink = rooms.compute(roomId, (id, existing) -> {
            if (existing == null) {
                existing = new RoomSink(Sinks.many().multicast().onBackpressureBuffer());
            }
            existing.subscriberCount.incrementAndGet();
            return existing;
        });

        return roomSink.sink.asFlux()
                .doFinally(signal -> handleUnsubscribe(roomId));
    }

    private void handleUnsubscribe(Long roomId) {
        rooms.compute(roomId, (id, existing) -> {
            if (existing == null) return null;

            int remaining = existing.subscriberCount.decrementAndGet();
            if (remaining <= 0) {
                System.out.println("Removed Sink for roomId: " + roomId);
                return null;
            }
            return existing;
        });
    }
}
