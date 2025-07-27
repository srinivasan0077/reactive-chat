package com.veprojects.reactivechat.security;

import com.veprojects.reactivechat.entities.User;
import com.veprojects.reactivechat.services.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Component
public class JWTReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    private JWTService jwtService;
    
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        String token = authentication.getCredentials().toString();

        try {
            Claims claims = jwtService.parseToken(token);
            User user=new User().setId(claims.get("id",Long.class)).setUsername(claims.getSubject());
            String role = claims.get("role", String.class);

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

            return Mono.just(new UsernamePasswordAuthenticationToken(user, null, authorities));
        } catch (JwtException e) {
            return Mono.error(new BadCredentialsException("Invalid JWT token", e));
        }
    }
}
