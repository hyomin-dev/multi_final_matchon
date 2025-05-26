package com.multi.matchon.chat.config;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT == accessor.getCommand()){

            log.info("connect 토큰 검증 시작");

            String accessToken = accessor.getFirstNativeHeader("Authorization");
            String token = accessToken.substring(7);

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            log.info("connect 토큰 검증 완료");

        }else if(StompCommand.SUBSCRIBE == accessor.getCommand()){

            log.info("subscribe 토큰 검증 시작");

            String accessToken = accessor.getFirstNativeHeader("Authorization");
            String token = accessToken.substring(7);

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

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
