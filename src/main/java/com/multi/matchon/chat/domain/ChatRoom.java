package com.multi.matchon.chat.domain;

import com.multi.matchon.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name="chat_room")
//@Setter: JPA entity에서 setter사용은 자제, test용
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chat_room_id")
    private Long id;

    @Column(name="is_group_chat")
    @Builder.Default
    private Boolean isGroupChat = true;

    @Column(name="chat_room_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String chatRoomName;

    @OneToMany(mappedBy = "chatRoom")
    @Builder.Default
    private List<ChatParticipant> chatParticipants = new ArrayList<>();

    @Column(name="is_deleted")
    @Builder.Default
    private Boolean isDeleted=false;

    @Column(name = "team_id")
    private Long teamId;






}
