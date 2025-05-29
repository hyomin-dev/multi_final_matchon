package com.multi.matchon.chat.service;

import com.multi.matchon.chat.domain.*;
import com.multi.matchon.chat.dto.res.ResChatDto;
import com.multi.matchon.chat.dto.res.ResMyChatListDto;
import com.multi.matchon.chat.repository.*;
import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.exception.custom.CustomException;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageReadLogRepository messageReadLogRepository;
    private final ChatUserBlockRepository chatUserBlockRepository;
    private final MemberRepository memberRepository;


    // 등록
    @Transactional
    public Long findPrivateChatRoom(Long receiverId, Long senderId) {
        // 차단 검사

        Member receiver = memberRepository.findByIdAndIsDeletedFalse(receiverId).orElseThrow(()->new CustomException("Chat 해당 회원 번호를 가진 회원은 존재하지 않습니다."));

        Member sender = memberRepository.findByIdAndIsDeletedFalse(senderId).orElseThrow(()->new CustomException("Chat 해당 회원 번호를 가진 회원은 존재하지 않습니다."));

        // 서로서로 차단했는지 확인

        //Boolean isBlock = chatUserBlockRepository.isBlockByReceiver(receiver);

        // 여기까지 왔다는 것은 receiverId와 senderId가 유효
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findPrivateChatRoomByReceiverIdAndSenderId(receiverId, senderId);
        if(chatRoom.isPresent()){
            return chatRoom.get().getId();
        }

        ChatRoom newChatRoom = ChatRoom.builder()
                .isGroupChat(false)
                .chatRoomName("private chat "+receiver.getMemberName()+"---" +sender.getMemberName())
                .build();

        chatRoomRepository.save(newChatRoom);

        addParticipantToRoom(newChatRoom, receiver);
        addParticipantToRoom(newChatRoom, sender);

        return newChatRoom.getId();
    }

    @Transactional
    public void addParticipantToRoom(ChatRoom chatRoom, Member member){

        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom) // 양방향 쓸것이기 때문에 없앰
                .member(member)
                .build();

        chatParticipant.changeChatRoom(chatRoom);

        chatParticipantRepository.save(chatParticipant);
    }

    @Transactional
    public void saveMessage(Long roomId, ResChatDto resChatDto) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(roomId).orElseThrow(()->new CustomException("Chat 해당 채팅방 번호를 가진 채팅방은 존재하지 않습니다."));

        Member sender =  memberRepository.findByMemberEmailAndIsDeletedFalse(resChatDto.getSenderEmail()).orElseThrow(()->new CustomException("Chat 해당 회원 번호를 가진 회원은 존재하지 않습니다."));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(resChatDto.getContent())
                .build();

        chatMessageRepository.save(chatMessage);

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomWithMember(chatRoom);

        for(ChatParticipant c : chatParticipants){
            MessageReadLog messageReadLog = MessageReadLog.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatMessage)
                    .isRead(c.getMember().equals(sender))
                    .build();

            messageReadLogRepository.save(messageReadLog);
        }

    }

    // 조회

    @Transactional(readOnly = true)
    public List<ResMyChatListDto> findAllMyChatRoom(CustomUser user) { // 차단 검사

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMemberIdAndIsDeletedFalse(user.getMember().getId());

//        List<ChatUserBlock> chatUserBlocks = chatUserBlockRepository.findAllByBlocker(user.getMember());
//        Set<Long> blockedIds = chatUserBlocks.stream()
//                .map(chatUserBlock -> chatUserBlock.getBlocked().getId())
//                .collect(Collectors.toSet());
        Set<Long> blockedIds = chatUserBlockRepository.findAllByBlocker(user.getMember()).stream()
                .map(chatUserBlock -> chatUserBlock.getBlocked().getId())
                .collect(Collectors.toSet());

        List<ResMyChatListDto> resMyChatListDtos = new ArrayList<>();

        for(ChatParticipant c: chatParticipants){
            Long count = messageReadLogRepository.countByChatRoomAndMemberAndIsReadFalse(c.getChatRoom(), user.getMember());
            Boolean isBlock = false;
            if(!c.getChatRoom().getIsGroupChat()){
                Member opponent = c.getChatRoom().getChatParticipants().stream()
                        .filter(chatParticipant -> !chatParticipant.getMember().getId().equals(user.getMember().getId()) && !chatParticipant.getIsDeleted())
                        .findFirst()
                        .map(ChatParticipant::getMember)
                        .orElse(null);
                if(opponent!=null)
                    isBlock = blockedIds.contains(opponent.getId());
            }


            ResMyChatListDto resMyChatListDto = ResMyChatListDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getChatRoomName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
                    .isBlock(isBlock)
                    .unReadCount(count)
                    .build();

            resMyChatListDtos.add(resMyChatListDto);
        }

        return resMyChatListDtos;

    }

    @Transactional(readOnly = true)
    public List<ResChatDto> findAllChatHistory(Long roomId, CustomUser user) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(roomId).orElseThrow(()->new CustomException("Chat 해당 채팅방 번호를 가진 채팅방은 존재하지 않습니다."));

        Member sender =  memberRepository.findByMemberEmailAndIsDeletedFalse(user.getMember().getMemberEmail()).orElseThrow(()->new CustomException("Chat 해당 회원은 존재하지 않습니다."));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomWithMember(chatRoom);

        Boolean check = false;

        for(ChatParticipant c : chatParticipants){
            if(c.getMember().equals(sender)){
                check = true;
                break;
            }
        }

        if(!check)
            throw new CustomException("Chat 본인이 속하지 않은 채팅방입니다.");

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAscWithMember(chatRoom);
        List<ResChatDto> resChatDtos = new ArrayList<>();

        for(ChatMessage c: chatMessages){
            ResChatDto resChatDto = ResChatDto.builder()
                    .content(c.getContent())
                    .senderEmail(c.getMember().getMemberEmail())
                    .senderName(c.getMember().getMemberName())
                    .build();

            resChatDtos.add(resChatDto);
        }
        return resChatDtos;
    }

    @Transactional(readOnly = true)
    public Boolean isRoomParticipant(String email, Long roomId) {

        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(roomId).orElseThrow(()->new CustomException("Chat 해당 채팅방 번호를 가진 채팅방은 존재하지 않습니다."));

        Member sender =  memberRepository.findByMemberEmailAndIsDeletedFalse(email).orElseThrow(()->new CustomException("Chat 해당 회원은 존재하지 않습니다."));

        return chatParticipantRepository.isRoomParticipantByChatRoomAndMember(chatRoom, sender);
//        return false;

    }

    // 수정

    @Transactional
    public void readAllMessage(Long roomId, CustomUser user) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(roomId).orElseThrow(()->new CustomException("Chat 해당 채팅방 번호를 가진 채팅방은 존재하지 않습니다."));

        Member sender =  memberRepository.findByMemberEmailAndIsDeletedFalse(user.getMember().getMemberEmail()).orElseThrow(()->new CustomException("Chat 해당 회원은 존재하지 않습니다."));

        int count = messageReadLogRepository.updateMessagesRead(chatRoom, sender);
        log.info("읽음 처리 메시지: {}",count);

    }

    @Transactional
    public void blockUser(Long roomId, CustomUser user) {

        Member blocked = chatParticipantRepository.findByRoomIdAndMemberAndRoleMember(roomId, user.getMember()).stream().map(ChatParticipant::getMember).findFirst().orElseThrow(()->new CustomException("blockUser 차단할 대상이 없습니다."));

        // chatUserBlock에서 자신과 상대방이 있는지 조회
        Optional<ChatUserBlock> chatUserBlock = chatUserBlockRepository.findByBlockerAndBlocked(user.getMember(),blocked);

        if(chatUserBlock.isPresent()){
            throw new CustomException("blockUser 이미 차단한 유저입니다.");
        }

        // 상대방을 차단
        ChatUserBlock newChatUserBlock = ChatUserBlock.builder()
                .blocker(user.getMember())
                .blocked(blocked)
                .build();
        chatUserBlockRepository.save(newChatUserBlock);

        log.info("blockUser: {} → {}", user.getMember().getId(), blocked.getId());

    }

    @Transactional
    public void unblockUser(Long roomId, CustomUser user){
        Member unblocked = chatParticipantRepository.findByRoomIdAndMemberAndRoleMember(roomId, user.getMember()).stream().map(ChatParticipant::getMember).findFirst().orElseThrow(()->new CustomException("blockUser 차단할 대상이 없습니다."));

        // chatUserBlock에서 자신과 상대방이 있는지 조회
        Optional<ChatUserBlock> chatUserBlock = chatUserBlockRepository.findByBlockerAndBlocked(user.getMember(),unblocked);

        if(chatUserBlock.isPresent()){
            chatUserBlockRepository.delete(chatUserBlock.get());
        }else{
            throw new CustomException("blockUser 차단된 유저가 없습니다.");
        }

    }

    // 삭제



}
