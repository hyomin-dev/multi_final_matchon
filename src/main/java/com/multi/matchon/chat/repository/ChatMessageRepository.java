package com.multi.matchon.chat.repository;


import com.multi.matchon.chat.domain.ChatMessage;
import com.multi.matchon.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {


    @Query("""
            select t1
            from ChatMessage t1
            join fetch t1.member
            where t1.chatRoom = :chatRoom
            
            """)
    List<ChatMessage> findByChatRoomOrderByCreatedTimeAscWithMember(@Param("chatRoom") ChatRoom chatRoom);
}
