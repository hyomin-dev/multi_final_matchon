package com.multi.matchon.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResMyChatListDto {

    private Long roomId;
    private String roomName;
    private Boolean isGroupChat;
    private Long unReadCount;


}
