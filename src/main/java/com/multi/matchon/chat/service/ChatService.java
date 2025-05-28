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

    public void addParticipantToRoom(ChatRoom chatRoom, Member member){
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();

        chatParticipantRepository.save(chatParticipant);
    }

    public List<ResMyChatListDto> findAllMyChatRoom(CustomUser user) {

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMemberId(user.getMember().getId());

        List<ResMyChatListDto> resMyChatListDtos = new ArrayList<>();

        for(ChatParticipant c: chatParticipants){
            //
            ResMyChatListDto resMyChatListDto = ResMyChatListDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getChatRoomName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
                    .unReadCount(0L)
                    .build();

            resMyChatListDtos.add(resMyChatListDto);
        }

        return resMyChatListDtos;

    }

    public void saveMessage(Long roomId, ResChatDto resChatDto) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(roomId).orElseThrow(()->new CustomException("Chat 해당 채팅방 번호를 가진 채팅방은 존재하지 않습니다."));

        Member sender =  memberRepository.findByMemberEmailAndIsDeletedFalse(resChatDto.getSenderEmail()).orElseThrow(()->new CustomException("Chat 해당 회원 번호를 가진 회원은 존재하지 않습니다."));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .member(sender)
                .content(resChatDto.getContent())
                .build();

        chatMessageRepository.save(chatMessage);

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);

        for(ChatParticipant c: chatParticipants){
            MessageReadLog messageReadLog = MessageReadLog.builder()
                    .chatRoom(chatRoom)
                    .member(c.getMember())
                    .chatMessage(chatMessage)
                    .isRead(c.getMember().equals(sender))
                    .build();
            messageReadLogRepository.save(messageReadLog);
        }



    }
}
