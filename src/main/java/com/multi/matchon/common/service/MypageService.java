package com.multi.matchon.common.service;

import com.multi.matchon.event.domain.HostProfile;
import com.multi.matchon.event.repository.HostProfileRepository;
import com.multi.matchon.member.domain.Member;
import com.multi.matchon.member.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MypageService {

    private final HostProfileRepository hostProfileRepository;

    public Map<String, Object> getMypageInfo(Member member) {
        Map<String, Object> data = new HashMap<>();

        data.put("memberRole", member.getMemberRole() != null ? member.getMemberRole().name() : "UNKNOWN");
        data.put("memberName", member.getMemberName());

        if (member.getSportsType() != null && member.getSportsType().getSportsTypeName() != null) {
            data.put("sportsName", member.getSportsType().getSportsTypeName().name());
        } else {
            data.put("sportsName", "미지정");
        }

        data.put("myTemperature", member.getMyTemperature());
        data.put("teamName", member.getTeam() != null ? member.getTeam().getTeamName() : "팀이 없습니다");

        if (MemberRole.HOST.equals(member.getMemberRole())) {
            hostProfileRepository.findByMember(member).ifPresent(host -> {
                data.put("hostName", host.getHostName());
            });
        }

        // S3 프로필 이미지 경로 반영
        String imageUrl = "https://sample-s3-multi18.s3.us-west-2.amazonaws.com/attachments/profile/" + member.getId() + ".jpg";
        data.put("profileImageUrl", imageUrl);

        return data;
    }

    public void updateHostName(Member member, String newHostName) {
        // 없으면 생성해서 넣기
        HostProfile profile = hostProfileRepository.findByMember(member)
                .orElseGet(() -> {
                    HostProfile newProfile = HostProfile.builder()
                            .member(member)
                            .hostName(newHostName)
                            .build();
                    return hostProfileRepository.save(newProfile);
                });

        profile.setHostName(newHostName);

        hostProfileRepository.save(profile);
    }
}
