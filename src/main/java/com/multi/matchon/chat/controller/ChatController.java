package com.multi.matchon.chat.controller;

import com.multi.matchon.chat.dto.res.ResChatDto;
import com.multi.matchon.chat.dto.res.ResMyChatListDto;
import com.multi.matchon.chat.service.ChatService;
import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.dto.res.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;



    @GetMapping("/my/room")
    public ModelAndView showPrivateChatPageByRoomId(@RequestParam("roomId") Long roomId, ModelAndView mv){
        mv.setViewName("chat/private-chat");
        mv.addObject("roomId",roomId);
        return mv;
    }

    // private-chat page
    @GetMapping("/private")
    public ModelAndView showPrivateChatPageByReceiverId(@RequestParam("receiverId") Long receiverId, ModelAndView mv){
        mv.setViewName("chat/private-chat");
        mv.addObject("receiverId",receiverId);
        return mv;
    }

    @ResponseBody
    @GetMapping("/room/private")
    public ResponseEntity<ApiResponse<Long>> getPrivateChatRoom(@RequestParam Long receiverId, @AuthenticationPrincipal CustomUser user){

        Long roomId = chatService.findPrivateChatRoom(receiverId, user.getMember().getId());

        return ResponseEntity.ok().body(ApiResponse.ok(roomId));
    }


    @GetMapping("/my/rooms")
    public String showMyChatRooms(){
        return "chat/my-chat-list";
    }


    @PostMapping("/my/rooms")
    public ResponseEntity<ApiResponse<List<ResMyChatListDto>>> getMyChatRooms(@AuthenticationPrincipal CustomUser user){
        List<ResMyChatListDto> resMyChatListDtos = chatService.findAllMyChatRoom(user);

        return ResponseEntity.ok().body(ApiResponse.ok(resMyChatListDtos));

    }

    @GetMapping("/history{roomId}")
    public ResponseEntity<ApiResponse<List<ResChatDto>>> getChatHistory(@RequestParam("roomId") Long roomId, @AuthenticationPrincipal CustomUser user){
        List<ResChatDto> resChatDtos = chatService.findAllChatHistory(roomId, user);
        return ResponseEntity.ok().body(ApiResponse.ok(resChatDtos));
    }

    @PostMapping("/room/read")
    public ResponseEntity<?> readAllMessage(@RequestParam("roomId") Long roomId, @AuthenticationPrincipal CustomUser user){
        chatService.readAllMessage(roomId, user);
        return ResponseEntity.ok().build();
    }
}
