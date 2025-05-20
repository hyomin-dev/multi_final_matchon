package com.multi.matchon.common.util;


import com.multi.matchon.common.auth.dto.CustomUser;
import com.multi.matchon.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoginUserAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();
        if(authentication ==null || !authentication.isAuthenticated())
            return null;
        return Optional.of(((CustomUser)SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication().getPrincipal()).getUsername());
    }
}
