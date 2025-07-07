package com.veprojects.reactivechat.security;

import com.veprojects.reactivechat.services.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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

    private final JWTService jwtService;

    public JWTReactiveAuthenticationManager(JWTService jwtService){
        this.jwtService=jwtService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        String token = authentication.getCredentials().toString();

        try {
            Claims claims = jwtService.parseToken(token);
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

            return Mono.just(new UsernamePasswordAuthenticationToken(username, null, authorities));
        } catch (JwtException e) {
            return Mono.error(new BadCredentialsException("Invalid JWT token", e));
        }
    }
}
