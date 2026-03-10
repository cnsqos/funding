package com.funding.funding.domain.user.service.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/*
  이메일 발송 서비스
  - @Async: 이메일 발송이 HTTP 응답을 블로킹하지 않도록 별도 스레드에서 실행
  - 이메일 발송 실패해도 회원가입 흐름은 중단되지 않음
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ── 이메일 인증 코드 발송 ──────────────────────────
    @Async
    public void sendVerificationCode(String to, String code) {
        String subject = "[펀딩플랫폼] 이메일 인증 코드";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2 style="color: #333;">이메일 인증</h2>
                  <p>아래 인증 코드를 입력해 주세요. 코드는 <strong>5분</strong> 동안 유효합니다.</p>
                  <div style="background:#f5f5f5; padding:20px; text-align:center; border-radius:8px; margin:20px 0;">
                    <span style="font-size:32px; font-weight:bold; letter-spacing:8px; color:#2d3a4a;">%s</span>
                  </div>
                  <p style="color:#999; font-size:12px;">본인이 요청하지 않은 경우 이 이메일을 무시하세요.</p>
                </div>
                """.formatted(code);
        sendHtml(to, subject, body);
    }

    // ── 비밀번호 재설정 링크 발송 ──────────────────────
    @Async
    public void sendPasswordResetLink(String to, String token, String frontendUrl) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String subject = "[펀딩플랫폼] 비밀번호 재설정";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2 style="color: #333;">비밀번호 재설정</h2>
                  <p>아래 버튼을 클릭해 비밀번호를 재설정하세요. 링크는 <strong>30분</strong> 동안 유효합니다.</p>
                  <div style="text-align:center; margin:30px 0;">
                    <a href="%s"
                       style="background:#2d3a4a; color:#fff; padding:12px 32px; border-radius:6px;
                              text-decoration:none; font-size:16px;">
                      비밀번호 재설정
                    </a>
                  </div>
                  <p style="color:#999; font-size:12px;">링크가 작동하지 않으면 아래 URL을 브라우저에 직접 입력하세요.<br/>%s</p>
                  <p style="color:#999; font-size:12px;">본인이 요청하지 않은 경우 이 이메일을 무시하세요.</p>
                </div>
                """.formatted(resetLink, resetLink);
        sendHtml(to, subject, body);
    }

    // ── 아이디(이메일) 찾기 결과 발송 ─────────────────
    @Async
    public void sendFoundEmail(String to, String maskedEmail) {
        String subject = "[펀딩플랫폼] 아이디 찾기 결과";
        String body = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2 style="color: #333;">아이디 찾기</h2>
                  <p>회원님의 아이디(이메일)를 안내해 드립니다.</p>
                  <div style="background:#f5f5f5; padding:20px; text-align:center; border-radius:8px; margin:20px 0;">
                    <span style="font-size:20px; font-weight:bold; color:#2d3a4a;">%s</span>
                  </div>
                  <p style="color:#999; font-size:12px;">본인이 요청하지 않은 경우 이 이메일을 무시하세요.</p>
                </div>
                """.formatted(maskedEmail);
        sendHtml(to, subject, body);
    }

    // ── 내부 공통 ──────────────────────────────────────
    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML
            mailSender.send(message);
        } catch (Exception e) {
            // 발송 실패 로그만 남기고 예외 전파 안 함 (비동기이므로 HTTP 응답과 무관)
            System.err.println("[EmailService] 이메일 발송 실패: " + to + " / " + e.getMessage());
        }
    }
}