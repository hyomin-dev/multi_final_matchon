package com.multi.matchon.common.service;

import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.common.domain.Notification;
import com.multi.matchon.common.dto.res.ResNotificationDto;
import com.multi.matchon.common.exception.custom.ApiCustomException;
import com.multi.matchon.common.repository.NotificationRepository;
import com.multi.matchon.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /*
    * 읽지 않은 정보 가져오기
    * */
    @Transactional(readOnly = true)
    public List<ResNotificationDto> findAllByMemberAndUnreadFalse(CustomUser user) {

        // 읽지 않은 알림 가져오기

       return notificationRepository.findAllByMemberAndUnreadFalse(user.getMember());


    }

    @Transactional
    public String updateUnreadNotification(Long notificationId, CustomUser user) {

         Notification notification = notificationRepository.findTargetUrlByNotificationIdAndMember(notificationId, user.getMember()).orElseThrow(()->new ApiCustomException("Notification "));

         notification.updateIsRead(true);

         return notification.getTargetUrl();

    }
}
