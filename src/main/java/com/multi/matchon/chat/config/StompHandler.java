package com.multi.matchon.chat.config;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Principal;

@Component
@Slf4j

public class StompHandler implements ChannelInterceptor {


    private final SecretKey secretKey;
    private final CustomUserDetailsService customUserDetailsService;

    public StompHandler(@Value("${jwt.secret}") String secret, CustomUserDetailsService customUserDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT == accessor.getCommand()){

            log.info("connect 토큰 검증 시작");

            String accessToken = accessor.getFirstNativeHeader("Authorization");
            String token = accessToken.substring(7);

            String email = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            CustomUser userDetails = (CustomUser) customUserDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authentication);

            log.info("connect 토큰 검증 완료");


        }else if(StompCommand.SUBSCRIBE == accessor.getCommand()){

            log.info("subscribe 토큰 검증 시작");

            String accessToken = accessor.getFirstNativeHeader("Authorization");
            String token = accessToken.substring(7);

            String email = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            CustomUser userDetails = (CustomUser) customUserDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authentication);

            log.info("subscribe 토큰 검증 완료");



        }else if(StompCommand.SEND == accessor.getCommand()){
            log.info("SEND Stage");
        }else if(StompCommand.UNSUBSCRIBE == accessor.getCommand()){
            log.info("UNSUBSCRIBE Stage");
        }else if(StompCommand.DISCONNECT == accessor.getCommand()){
            log.info("DISCONNECT Stage");
        }
        return message;
    }
}
