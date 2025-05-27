package com.multi.matchon.chat.controller;

import com.multi.matchon.chat.dto.req.ReqChatDto;
import com.multi.matchon.chat.dto.res.ResChatDto;
import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ReqChatDto reqChatDto, Principal principal){
        String senderEmail = "none";
        if (principal instanceof UsernamePasswordAuthenticationToken authentication) {
            Object principalObj = authentication.getPrincipal();
            if (principalObj instanceof CustomUser customUser) {
                Member sender = customUser.getMember();
                senderEmail = sender.getMemberEmail(); // 예시
                // 이제 원하는대로 사용 가능
            }
        }

        log.info("message: {}",reqChatDto.getContent());
        ResChatDto resChatDto = ResChatDto.builder()
                .senderEmail(senderEmail)
                .content(reqChatDto.getContent())
                .build();

        messageTemplate.convertAndSend("/topic/"+roomId,resChatDto);
    }
}
