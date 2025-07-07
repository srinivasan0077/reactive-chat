package com.veprojects.reactivechat.repository;

import com.veprojects.reactivechat.entities.User;
import io.r2dbc.spi.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserRepository {

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Mono<User> createUser(User user){
        return loadUserByName(user.getUsername())
                .flatMap(user1 -> Mono.<User>error(new Exception("User already exist!")))
                .switchIfEmpty(insertUserWithTransaction(user));
    }

    private Mono<User> insertUserWithTransaction(User user) {
        return Mono.usingWhen(
                databaseClient.getConnectionFactory().create(),
                (connection)->{
                    Mono<Void> mono= (Mono<Void>) connection.beginTransaction();
                    return mono.then(insertUser(connection,user)).flatMap(u->{
                        Mono<Void> result= (Mono<Void>) connection.commitTransaction();
                        return result.thenReturn(u);
                    }).onErrorResume(e->{
                        Mono<Void> result= (Mono<Void>) connection.rollbackTransaction();
                        return result.then(Mono.error(e));
                    });
                },
                Connection::close
        );
    }

    public Mono<User> insertUser(Connection connection,User user) {
        return Mono.from(
                        connection.createStatement("INSERT INTO users(username,password,role) VALUES (?,?,?)")
                                .bind(0, user.getUsername())
                                .bind(1, passwordEncoder.encode(user.getPassword()))
                                .bind(2,user.getRole())
                                .returnGeneratedValues("id")
                                .execute()
                ).flatMapMany(result -> result.map((row, meta) -> {
                    user.setId(row.get("id",Long.class));
                    return user;
                }))
                .single();
    }

    public Mono<User> loadUserByName(String name){
        return databaseClient.sql("SELECT * FROM users where username=:name limit 1")
                .bind("name", name)
                .map((row,meta)->{
                    User user=new User();
                    user.setId(row.get("id",Long.class));
                    user.setUsername(row.get("username",String.class));
                    user.setPassword(row.get("password",String.class));
                    user.setRole(row.get("role",String.class));
                    return user;
                }).one();

    }


}
