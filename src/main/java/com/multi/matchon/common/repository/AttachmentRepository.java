package com.multi.matchon.common.repository;


import com.multi.matchon.common.domain.Attachment;
import com.multi.matchon.common.domain.BoardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    @Query("SELECT a FROM Attachment a WHERE a.boardType = :boardType AND a.boardNumber = :boardNumber ORDER BY a.id DESC LIMIT 1")
    Optional<Attachment> findLatestAttachment(@Param("boardType") BoardType boardType, @Param("boardNumber") Long boardNumber);


    @Query("select t1 from Attachment t1 where t1.boardType=:boardType and t1.boardNumber=:matchupBoardId")
    List<Attachment> findAllByBoardTypeAndBoardNumber(@Param("boardType") BoardType boardType,@Param("matchupBoardId") Long matchupBoardId);

}
