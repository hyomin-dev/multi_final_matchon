package com.multi.matchon.common.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    @Value("${app.domain-url}")
    private String domainUrl;

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

    public void sendNotificationEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);


            ClassPathResource logo = new ClassPathResource("static/img/matchon_logo.png");
            helper.addInline("matchonLogo", logo);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("알림 메일 전송 실패: " + e.getMessage());
        }
    }

    // 이메일 본문 생성 메서드
    public String buildNotificationBody(String message, String targetUrl) {
        String redirectUrl = domainUrl + "/redirect?url=" + targetUrl;
        return """
        <div style="font-family: 'Noto Sans KR', sans-serif; color: #333;">
            <img src='cid:matchonLogo' style='width: 120px; margin-bottom: 20px;' alt='MatchOn Logo'/>
            <h2 style="color: #d77a8f;">📢 새로운 알림이 도착했습니다!</h2>

            <div style="padding: 14px 20px; background-color: #f2f2f2; border-left: 4px solid #d3365d; margin: 20px 0; font-size: 16px;">
                <strong>%s</strong>
            </div>

            <p>해당 알림은 아래 내용을 확인하시면 됩니다.</p>
            <p>
                <a href="%s" style="display:inline-block; margin-top:10px; background-color:#d77a8f; color:white; padding:10px 20px; text-decoration:none; border-radius:5px;">👉 바로 가기</a>
            </p>

            <hr style="margin-top: 30px;">
            <p style="font-size: 13px; color: #888;">본 메일은 알림 수신에 동의하신 회원에게 발송되었습니다.</p>
        </div>
        """.formatted(message, redirectUrl);
    }

    public void sendAdminNotificationEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[MatchOn 관리자 알림] " + subject);
            helper.setText(htmlContent, true);

            ClassPathResource logo = new ClassPathResource("static/img/matchon_logo.png");
            helper.addInline("matchonLogo", logo);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("관리자 메일 전송 실패: " + e.getMessage());
        }
    }

    public String buildAdminNotificationBody(String senderName, String message, String targetUrl) {
        String redirectUrl = domainUrl + "/redirect?url=" + targetUrl;
        return """
    <div style="font-family: 'Noto Sans KR', sans-serif; color: #333;">
        <img src='cid:matchonLogo' style='width: 120px; margin-bottom: 20px;' alt='MatchOn Logo'/>
        <h2 style="color: #005bac;">[🔔 관리자 알림]</h2>
        <p><strong>%s</strong>님의 활동에 대한 알림입니다:</p>

        <div style="padding: 14px 20px; background-color: #f0f0f0; border-left: 4px solid #005bac; margin: 20px 0; font-size: 16px;">
            <strong>%s</strong>
        </div>

        <p>관련 내용을 아래 버튼을 눌러 확인해 주세요.</p>
        <p>
            <a href="%s" style="display:inline-block; margin-top:10px; background-color:#005bac; color:white; padding:10px 20px; text-decoration:none; border-radius:5px;">🔍 바로 가기</a>
        </p>

        <hr style="margin-top: 30px;">
        <p style="font-size: 13px; color: #888;">본 메일은 관리자에게 발송된 자동 알림입니다.</p>
    </div>
    """.formatted(senderName, message, redirectUrl);
    }
}