package com.multi.matchon.chat.controller;

import com.multi.matchon.chat.dto.res.ResChatDto;
import com.multi.matchon.chat.dto.res.ResMyChatListDto;
import com.multi.matchon.chat.service.ChatService;
import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.dto.res.ApiResponse;
import com.multi.matchon.common.exception.custom.CustomException;
import com.multi.matchon.team.domain.TeamMember;
import com.multi.matchon.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
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

    private final TeamMemberRepository teamMemberRepository;

    private final ChatService chatService;

    // 등록

    @GetMapping("/my/room")
    public ModelAndView showPrivateChatPageByRoomId(@RequestParam("roomId") Long roomId, ModelAndView mv){
        mv.setViewName("chat/private-chat");
        mv.addObject("roomId",roomId);
        return mv;
    }

    // private-chat page로 이동
    @GetMapping("/private")
    public ModelAndView showPrivateChatPageByReceiverId(@RequestParam("receiverId") Long receiverId, ModelAndView mv){
        mv.setViewName("chat/private-chat");
        mv.addObject("receiverId",receiverId);
        return mv;
    }

    // private-chat room 생성 또는 조회
    @ResponseBody
    @GetMapping("/room/private")
    public ResponseEntity<ApiResponse<Long>> getPrivateChatRoom(@RequestParam Long receiverId, @AuthenticationPrincipal CustomUser user){

        Long senderId = user.getMember().getId();
        System.out.println("🔍 Creating or fetching private chat room between sender " + senderId + " and receiver " + receiverId);

        Long roomId = chatService.findPrivateChatRoom(receiverId, user.getMember().getId());


        System.out.println("✅ Found or created roomId: " + roomId);

        return ResponseEntity.ok().body(ApiResponse.ok(roomId));
    }

    // 조회

    /*
    * private chat room 페이지 이동
    * */
    @GetMapping("/my/rooms")
    public String showMyChatRooms(){
        return "chat/my-chat-list";
    }

    /*
    * 내가 속한 채팅방 목록 전달하는 메서드
    * */
    //팀 채팅 목록에서 팀 채팅 만 보이도록 수정
    @PostMapping("/my/rooms")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ResMyChatListDto>>> getMyChatRooms(@AuthenticationPrincipal CustomUser user) {
        List<ResMyChatListDto> chatRooms;


        TeamMember teamMember = teamMemberRepository.findByMemberId(user.getMember().getId())
                .orElse(null);

        boolean isLeader = teamMember != null && teamMember.getTeamLeaderStatus();
        boolean hasTeam = user.getMember().getTeam() != null;

        if (isLeader && hasTeam) {
            Long leaderId = user.getMember().getId();
            Long teamId = user.getMember().getTeam().getId();
            chatRooms = chatService.findRelevantRoomsForLeader(leaderId, teamId); // ✅ filtered
        } else {
            chatRooms = chatService.findAllRoomsForUser(user.getMember().getId()); // fallback
        }

        return ResponseEntity.ok().body(ApiResponse.ok(chatRooms));
    }

    /*
    * 내가 속한 채팅방에서 대화 기록 전달하는 메서드
    * */
    @GetMapping("/history{roomId}")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<ResChatDto>>> getChatHistory(@RequestParam("roomId") Long roomId, @AuthenticationPrincipal CustomUser user){
        List<ResChatDto> resChatDtos = chatService.findAllChatHistory(roomId, user);
        return ResponseEntity.ok().body(ApiResponse.ok(resChatDtos));
    }


    /*
    * 로그인한 유저가 지정한 roomId에 채팅 참여자 인지 체크
    * */
    @GetMapping("/check/my/rooms")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> checkRoomParticipant(@RequestParam("roomId") Long roomId, @AuthenticationPrincipal CustomUser user){

        chatService.checkRoomParticipant(user,roomId);

        return ResponseEntity.ok().build();
    }


    /*
    * 특정 group chat room 페이지 이동
    * */
    @GetMapping("/group/room")
    public ModelAndView showGroupChatPageByRoomId(@RequestParam("roomId") Long roomId, ModelAndView mv){
        mv.setViewName("chat/group-chat");
        mv.addObject("roomId",roomId);
        return mv;
    }

    //수정

    @PostMapping("/room/read")
    @ResponseBody
    public ResponseEntity<?> readAllMessage(@RequestParam("roomId") Long roomId, @AuthenticationPrincipal CustomUser user){
        chatService.readAllMessage(roomId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/room/private/block")
    public String blockUser(@RequestParam("roomId") Long roomId, @AuthenticationPrincipal CustomUser user){
        chatService.blockUser(roomId, user);
        return "redirect:/chat/my/rooms";
    }

    @GetMapping("/room/private/unblock")
    public String unblockUser(@RequestParam("roomId") Long roomId, @AuthenticationPrincipal CustomUser user){
        chatService.unblockUser(roomId, user);
        return "redirect:/chat/my/rooms";
    }


    @GetMapping("/group/room-id")
    @ResponseBody
    public ResponseEntity<ApiResponse<Long>> getTeamChatRoomId(@RequestParam Long teamId) {
        Long roomId = chatService.findTeamChatRoomByTeamId(teamId);
        return ResponseEntity.ok(ApiResponse.ok(roomId));
    }
    // 삭제

}
