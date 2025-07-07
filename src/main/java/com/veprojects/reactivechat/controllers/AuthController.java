package com.veprojects.reactivechat.controllers;

import com.veprojects.reactivechat.entities.UserDetails;
import com.veprojects.reactivechat.entities.User;
import com.veprojects.reactivechat.services.JWTService;
import com.veprojects.reactivechat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @PostMapping("/signup")
    public Mono<ResponseEntity<Response>> signUp(@RequestBody UserDetails userDetails){
        User user=new User();
        user.setUsername(userDetails.getUsername());
        user.setPassword(userDetails.getPassword());
        user.setRole("USER");

        return userRepository.createUser(user)
                .map(createdUser->ResponseEntity.ok(new Response("User created successfully!",null)))
                .onErrorResume(e->Mono.just(ResponseEntity.badRequest().body(new Response(e.getMessage(),null))));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Response>> login(@RequestBody UserDetails userDetails){

        return userRepository.loadUserByName(userDetails.getUsername())
                .flatMap(user -> {
                    if(passwordEncoder.matches(userDetails.getPassword(),user.getPassword())){
                        return Mono.just(ResponseEntity.ok(new Response("Login Successful", jwtService.generateToken(user))));
                    }else{
                        return Mono.just(ResponseEntity.badRequest().body(new Response("Authentication Failure!",null)));
                    }
                })
                .defaultIfEmpty(ResponseEntity.badRequest().body(new Response("User not exist!",null)));
    }
}
