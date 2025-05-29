package com.multi.matchon.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResChatDto {

    private String senderEmail;
    private String senderName;
    private String content;
    private LocalDateTime createdDate;

}
