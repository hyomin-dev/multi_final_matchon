package com.multi.matchon.chat.repository;


import com.multi.matchon.chat.domain.MessageReadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageReadLogRepository extends JpaRepository<MessageReadLog, Long> {
}
