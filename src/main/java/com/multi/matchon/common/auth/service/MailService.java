package com.multi.matchon.common.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendTemporaryPassword(String toEmail, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[MatchOn] 임시 비밀번호 안내");

            String htmlContent = """
                <div style="font-family: 'Noto Sans KR', sans-serif; color: #333;">
                    <img src='cid:matchonLogo' style='width: 120px; margin-bottom: 20px;' alt='MatchOn Logo'/>
                    <h2 style="color: #d77a8f;">MatchOn 임시 비밀번호 안내</h2>
                    <p>안녕하세요, MatchOn 회원님.</p>
                    <p>요청하신 <strong>임시 비밀번호</strong>는 아래와 같습니다.</p>

                    <div style="padding: 12px 20px; background-color: #f8f8f8; border-left: 4px solid #d77a8f; margin: 20px 0; font-size: 18px;">
                        <strong style="color: #d3365d;">%s</strong>
                    </div>

                    <p>위 임시 비밀번호로 로그인하신 후, 반드시 비밀번호를 변경해주세요.</p>

                    <p style="margin-top: 30px; font-size: 13px; color: #999;">본 메일은 자동 발송되었습니다.</p>
                </div>
                """.formatted(tempPassword);

            helper.setText(htmlContent, true);

            // 로고 CID 등록
            ClassPathResource logo = new ClassPathResource("static/img/matchon_logo.png");
            helper.addInline("matchonLogo", logo);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("메일 전송 실패: " + e.getMessage());
        }
    }


}
