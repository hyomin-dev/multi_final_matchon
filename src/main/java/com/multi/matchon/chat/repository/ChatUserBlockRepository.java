package com.multi.matchon.chat.repository;


import com.multi.matchon.chat.domain.ChatUserBlock;
import com.multi.matchon.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatUserBlockRepository extends JpaRepository<ChatUserBlock, Long> {


    Optional<ChatUserBlock> findByBlockerAndBlocked(Member blocker, Member blocked);


    @Query("""
            select t1
            from ChatUserBlock t1
            where t1.blocker =:blocker
            """)
    List<ChatUserBlock> findAllByBlocker(@Param("blocker") Member blocker);
}
