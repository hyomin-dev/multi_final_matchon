package com.multi.matchon.chat.service;

import com.multi.matchon.chat.domain.*;
import com.multi.matchon.chat.dto.res.ResChatDto;
import com.multi.matchon.chat.dto.res.ResMyChatListDto;
import com.multi.matchon.chat.exception.custom.ChatBlockException;

import com.multi.matchon.chat.exception.custom.NotChatParticipantException;

import com.multi.matchon.chat.repository.*;
import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.exception.custom.ApiCustomException;
import com.multi.matchon.common.exception.custom.CustomException;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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


    @Transactional
    public Long findPrivateChatRoom(Long receiverId, Long senderId) {
        System.out.println("🟡 [findPrivateChatRoom] Called with receiverId = " + receiverId + ", senderId = " + senderId);

        Member receiver = memberRepository.findByIdAndIsDeletedFalse(receiverId)
                .orElseThrow(() -> {
                    System.out.println("❌ Receiver not found: " + receiverId);
                    return new ApiCustomException("Chat 해당 회원 번호를 가진 회원은 존재하지 않습니다.");
                });

        Member sender = memberRepository.findByIdAndIsDeletedFalse(senderId)
                .orElseThrow(() -> {
                    System.out.println("❌ Sender not found: " + senderId);
                    return new ApiCustomException("Chat 해당 회원 번호를 가진 회원은 존재하지 않습니다.");
                });

        System.out.println("✅ Receiver: " + receiver.getMemberName() + ", Sender: " + sender.getMemberName());

        Optional<ChatRoom> chatRoom = chatParticipantRepository.findPrivateChatRoomByReceiverIdAndSenderId(receiverId, senderId);
        if (chatRoom.isPresent()) {
            System.out.println("📎 Existing chat room found with ID: " + chatRoom.get().getId());
            return chatRoom.get().getId();
        }

        String identifierChatRoomName = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        ChatRoom newChatRoom = ChatRoom.builder()
                .isGroupChat(false)
                .chatRoomName("private chat " + receiver.getMemberName() + "---" + sender.getMemberName() + "---" + identifierChatRoomName)
                .build();

        chatRoomRepository.save(newChatRoom);
        addParticipantToRoom(newChatRoom, receiver);
        addParticipantToRoom(newChatRoom, sender);

        System.out.println("🆕 New chat room created with ID: " + newChatRoom.getId());

        return newChatRoom.getId();
    }

    /*
    * 생성된 채팅방에 참여자를 추가할 때 사용
    * */
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

    /*
    * Matchup board 작성할 때, group chat room을 생성하는 메서드
    * */
    @Transactional
    public ChatRoom registerGroupChatRoom(Member matchupWriter){
        String identifierChatRoomName = UUID.randomUUID().toString().replace("-","").substring(0,8);

        ChatRoom newChatRoom = ChatRoom.builder()
                .isGroupChat(true)
                .chatRoomName("Matchup Group Chat "+matchupWriter.getMemberName()+"---" +identifierChatRoomName)
                .build();

        chatRoomRepository.save(newChatRoom);

        addParticipantToRoom(newChatRoom, matchupWriter);

        return newChatRoom;

    }

    /*
    * Matchup board에 대응 되는 group chat room에 사용자를 추가
    * */
    @Transactional
    public void addParticipantToGroupChat(ChatRoom groupChatRoom, Member applicant){
        addParticipantToRoom(groupChatRoom, applicant);
    }

    /*
     * Matchup board에 대응 되는 group chat room에 사용자를 제거
     * */
    @Transactional
    public void removeParticipantToGroupChat(ChatRoom groupChatRoom, Member applicant){
        ChatParticipant removeChatParticipant = chatParticipantRepository.findByChatRoomAndMember(groupChatRoom, applicant).orElseThrow(()-> new CustomException("Matchup 해당 참여자는 그룹 채팅에 참여자가 아니에요."));

        removeChatParticipant.deleteParticipant(true);
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
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(roomId).orElseThrow(()->new ApiCustomException("Chat 해당 채팅방 번호를 가진 채팅방은 존재하지 않습니다."));

        Member sender =  memberRepository.findByMemberEmailAndIsDeletedFalse(user.getMember().getMemberEmail()).orElseThrow(()->new ApiCustomException("Chat 해당 회원은 존재하지 않습니다."));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomWithMember(chatRoom);

        Boolean check = false;

        for(ChatParticipant c : chatParticipants){
            if(c.getMember().equals(sender)){
                check = true;
                break;
            }
        }

        if(!check)
            throw new ApiCustomException("Chat 본인이 속하지 않은 채팅방입니다.");

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAscWithMember(chatRoom);
        List<ResChatDto> resChatDtos = new ArrayList<>();

        for(ChatMessage c: chatMessages){
            ResChatDto resChatDto = ResChatDto.builder()
                    .content(c.getContent())
                    .senderEmail(c.getMember().getMemberEmail())
                    .senderName(c.getMember().getMemberName())
                    .createdDate(c.getCreatedDate())
                    .build();

            resChatDtos.add(resChatDto);
        }
        return resChatDtos;
    }

    @Transactional(readOnly = true)
    public void checkRoomParticipant(CustomUser user, Long roomId) {

        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(roomId).orElseThrow(()->new ApiCustomException("Chat 해당 채팅방 번호를 가진 채팅방은 존재하지 않습니다."));

        Member sender =  memberRepository.findByIdAndIsDeletedFalse(user.getMember().getId()).orElseThrow(()->new ApiCustomException("Chat 해당 회원은 존재하지 않습니다."));

        if(!chatParticipantRepository.isRoomParticipantByChatRoomAndMember(chatRoom, sender)){
            throw new NotChatParticipantException("Chat 해당 채팅방에 참여자가 아닙니다.");

        }
//        return false;
    }


    /*
     * 1대1 채팅방 == roomId에 대응되는 참여자들이 서로서로 차단했는지 체크
     * 한 사람이라도 차단했으면 예외 발생 → @MessageExceptionHandler에서 처리
     * 두 사람 모두 차단안한경우 예외 발생 안함 → 메시지 저장 → room을 subscribe한 유저들에게 메시지 전달
     * */
    @Transactional(readOnly = true)
    public void checkBlock(Long roomId) {
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoomIdWithMember(roomId);

        if(chatParticipants.isEmpty())
            throw new CustomException("Chat 해당 채팅방은 존재하지 않습니다.");


//        if(chatParticipants.size()!=2)
//            throw new CustomException("Chat 해당 채팅방은 1대1 채팅방이 아닙니다.");


        // 그룹 채팅이라면 더 이상 진행하지 않음
        if(chatRoomRepository.isGroupChat(roomId))
            return;

        // 한 사람이라도 차단했는지 체크
        if (chatUserBlockRepository.isBlockByTwoMember(chatParticipants.get(0).getMember(),chatParticipants.get(1).getMember())){

            throw new ChatBlockException("ChatBlockException 발생");
        }
    }

    // 수정

    @Transactional
    public void readAllMessage(Long roomId, CustomUser user) {
        ChatRoom chatRoom = chatRoomRepository.findByIdAndIsDeletedFalse(roomId).orElseThrow(()->new ApiCustomException("Chat 해당 채팅방 번호를 가진 채팅방은 존재하지 않습니다."));

        Member sender =  memberRepository.findByMemberEmailAndIsDeletedFalse(user.getMember().getMemberEmail()).orElseThrow(()->new ApiCustomException("Chat 해당 회원은 존재하지 않습니다."));

        int count = messageReadLogRepository.updateMessagesRead(chatRoom, sender);
        log.info("읽음 처리 메시지: {}",count);

    }


    /*
     * 1대1 채팅에서 상대 유저를 차단하는 메서드
     * */
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


    /*
     * 1대1 채팅에서 상대 유저를 차단해제 하는 메서드
     * */
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


    @Transactional(readOnly = true)
    public Long findTeamChatRoomByTeamId(Long teamId) {
        return chatRoomRepository.findByTeamIdAndIsGroupChatTrue(teamId)
                .orElseThrow(() -> new CustomException("팀 채팅방이 존재하지 않습니다."))
                .getId();
    }

    public List<ResMyChatListDto> findRelevantRoomsForLeader(Long leaderId, Long teamId) {
        List<ChatRoom> privateChats = chatParticipantRepository.findAllPrivateChatsForLeader(leaderId);
        Optional<ChatRoom> groupChatOpt = chatParticipantRepository.findGroupChatByTeamId(teamId);

        groupChatOpt.ifPresent(privateChats::add); // ✅ 그룹 채팅방 추가

        return privateChats.stream()
                .map(room -> {
                    boolean isBlocked = chatUserBlockRepository.isBlocked(room.getId(), leaderId);
                    long unreadCount = messageReadLogRepository.countByChatRoomAndMemberAndIsReadFalse(room, memberRepository.findById(leaderId).orElseThrow());
                    return ResMyChatListDto.from(room, leaderId, isBlocked, unreadCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ResMyChatListDto> findAllRoomsForUser(Long userId) {
        Member member = memberRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new CustomException("해당 회원이 존재하지 않습니다."));

        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByMemberIdAndIsDeletedFalse(userId);

        Set<Long> blockedIds = chatUserBlockRepository.findAllByBlocker(member).stream()
                .map(chatUserBlock -> chatUserBlock.getBlocked().getId())
                .collect(Collectors.toSet());

        List<ResMyChatListDto> resMyChatListDtos = new ArrayList<>();

        for (ChatParticipant c : chatParticipants) {
            Long count = messageReadLogRepository.countByChatRoomAndMemberAndIsReadFalse(c.getChatRoom(), member);
            boolean isBlock = false;

            if (!c.getChatRoom().getIsGroupChat()) {
                Member opponent = c.getChatRoom().getChatParticipants().stream()
                        .filter(p -> !p.getMember().getId().equals(member.getId()) && !p.getIsDeleted())
                        .findFirst()
                        .map(ChatParticipant::getMember)
                        .orElse(null);
                if (opponent != null) isBlock = blockedIds.contains(opponent.getId());
            }

            ResMyChatListDto dto = ResMyChatListDto.builder()
                    .roomId(c.getChatRoom().getId())
                    .roomName(c.getChatRoom().getChatRoomName())
                    .isGroupChat(c.getChatRoom().getIsGroupChat())
                    .isBlock(isBlock)
                    .unReadCount(count)
                    .build();

            resMyChatListDtos.add(dto);
        }

        return resMyChatListDtos;
    }
}
