package com.multi.matchon.chat.service;

import com.multi.matchon.chat.domain.ChatMessage;
import com.multi.matchon.chat.domain.ChatParticipant;
import com.multi.matchon.chat.domain.ChatRoom;
import com.multi.matchon.chat.domain.MessageReadLog;
import com.multi.matchon.chat.dto.res.ResChatDto;
import com.multi.matchon.chat.dto.res.ResMyChatListDto;
import com.multi.matchon.chat.repository.ChatMessageRepository;
import com.multi.matchon.chat.repository.ChatParticipantRepository;
import com.multi.matchon.chat.repository.ChatRoomRepository;
import com.multi.matchon.chat.repository.MessageReadLogRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j

public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageReadLogRepository messageReadLogRepository;
    private final MemberRepository memberRepository;


    // 등록
    @Transactional
    public Long findPrivateChatRoom(Long receiverId, Long senderId) {

        Member receiver = memberRepository.findByIdAndIsDeletedFalse(receiverId).orElseThrow(()->new CustomException("Chat 해당 회원 번호를 가진 회원은 존재하지 않습니다."));

        Member sender = memberRepository.findByIdAndIsDeletedFalse(senderId).orElseThrow(()->new CustomException("Chat 해당 회원 번호를 가진 회원은 존재하지 않습니다."));


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
                .chatRoom(chatRoom)
                .member(member)
                .build();

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
    public List<ResMyChatListDto> findAllMyChatRoom(CustomUser user) {

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMemberId(user.getMember().getId());

        List<ResMyChatListDto> resMyChatListDtos = new ArrayList<>();

        for(ChatParticipant c: chatParticipants){
            Long count = messageReadLogRepository.countByChatRoomAndMemberAndIsReadFalse(c.getChatRoom(), user.getMember());
            ResMyChatListDto resMyChatListDto = ResMyChatListDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getChatRoomName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
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

    // 삭제



}
