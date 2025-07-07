package com.veprojects.reactivechat.repository;

import com.veprojects.reactivechat.entities.Message;
import com.veprojects.reactivechat.entities.Room;
import com.veprojects.reactivechat.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
public class MessageRepository {

    @Autowired
    private DatabaseClient databaseClient;

    public Mono<Message> createMessage(Message message){
        return databaseClient.sql("insert into messages(message,room_id,sender_id,timestamp) values(?,?,?,?)")
                .bind(0,message.getMessage())
                .bind(1,message.getRoom().getId())
                .bind(2,message.getSender().getId())
                .bind(3,new Timestamp(System.currentTimeMillis()))
                .filter(statement -> statement.returnGeneratedValues("id"))
                .map((row,meta)->{
                    message.setId(row.get("id",Long.class));
                    return message;
                }).one();
    }

    public Flux<Message> getMessagesOfRoom(Long roomId,Long offset,int limit){
        return databaseClient
                .sql("""
                SELECT m.id, m.message, m.timestamp, u.id AS sender_id, u.username, r.name AS room_name
                FROM messages AS m
                INNER JOIN rooms AS r ON m.room_id = r.id
                INNER JOIN users AS u ON m.sender_id = u.id
                WHERE m.room_id = ? AND m.id < ?
                ORDER BY m.id DESC
                LIMIT ?
                """)
                .bind(0, roomId)
                .bind(1, offset)
                .bind(2, limit)
                .map((row, meta) -> {
                    Message message = new Message();
                    message.setId(row.get("m.id", Long.class));
                    message.setMessage(row.get("m.message", String.class));
                    message.setCreatedAt(row.get("m.timestamp", Instant.class));

                    Room room = new Room();
                    room.setId(roomId);
                    room.setName(row.get("room_name", String.class));

                    User user = new User();
                    user.setId(row.get("sender_id", Long.class));
                    user.setUsername(row.get("u.username", String.class));

                    message.setRoom(room);
                    message.setSender(user);

                    return message;
                })
                .all();

    }

}
