package com.multi.matchon.chat.repository;


import com.multi.matchon.chat.domain.ChatUserBlock;
import com.multi.matchon.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatUserBlockRepository extends JpaRepository<ChatUserBlock, Long> {


    @Query("""
            select t1
            from ChatUserBlock t1
            where t1.blocker =:blocker and t1.blocked=:blocked
            """)
    Optional<ChatUserBlock> findByBlockerAndBlocked(@Param("blocker") Member blocker, @Param("blocked") Member blocked);


    @Query("""
            select t1
            from ChatUserBlock t1
            where t1.blocker =:blocker
            """)
    List<ChatUserBlock> findAllByBlocker(@Param("blocker") Member blocker);



    @Query("""
            
            select case
                        when count(t1) >0 then true
                        else false
                    end
                from ChatUserBlock t1
                where (t1.blocker=:member1 and t1.blocked=:member2) or (t1.blocker=:member2 and t1.blocked=:member1)
            """)
    boolean isBlockByTwoMember(@Param("member1")Member member1,@Param("member2") Member member2);
}
