package com.multi.matchon.chat.repository;


import com.multi.matchon.chat.domain.ChatParticipant;
import com.multi.matchon.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    @Query("""
            select
                t1.chatRoom
            from ChatParticipant t1
            join ChatParticipant t2
            on t1.chatRoom.id = t2.chatRoom.id
            where t1.member.id =:receiverId and t2.member.id=:senderId and t1.chatRoom.isGroupChat = false
            
            """)
    Optional<ChatRoom> findPrivateChatRoomByReceiverIdAndSenderId(Long receiverId, Long senderId);

    @Query("""
            select t1
            from ChatParticipant t1
            join fetch t1.chatRoom t2
            where t1.member.id =:memberId
            
            """)
    List<ChatParticipant> findAllByMemberId(@Param("memberId") Long memberId);

    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);
}
