package com.veprojects.reactivechat.repository;

import com.veprojects.reactivechat.entities.Room;
import com.veprojects.reactivechat.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public class RoomRepository {

    @Autowired
    private DatabaseClient databaseClient;

    public Mono<Room> createRoom(Room room){
        return databaseClient.sql("insert into rooms values(?,?)")
                .bind(0,room.getName())
                .bind(1,room.getCreatedBy().getId())
                .filter(statement -> statement.returnGeneratedValues("id"))
                .map((row,meta)->{
                    room.setId(row.get("id",Long.class));
                    return room;
                }).one();
    }

    public Flux<Room> getRoomsOfUser(User user){
        return databaseClient
                .sql("select r.id,r.name from rooms as r inner join users as u on r.created_by=u.id where r.created_by=?")
                .bind(0,user.getId())
                .map((row,meta)->{
                    Room room=new Room();
                    room.setId(row.get("id",Long.class));
                    room.setName(row.get("name",String.class));
                    return room;
                })
                .all();
    }
}
