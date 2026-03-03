package com.student.iksu.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    /**
     * @param toEmail 수신자 이메일
     * @param emailType "JOIN"(가입), "RESET"(비번찾기), "UPDATE"(이메일변경)
     */
    public void sendVerificationCode(String toEmail, String emailType) {
        String code = String.valueOf((int) (Math.random() * 899999) + 100000);
        verificationCodes.put(toEmail, code);

        String subject = "";
        String title = "";
        String bodyHeader = "";
        String bodyDesc = "";

        // 메일 타입에 따라 멘트 분기 처리
        switch (emailType) {
            case "RESET":
                subject = "[KSAI] 비밀번호 재설정 인증 번호 안내";
                title = "비밀번호 재설정";
                bodyHeader = "비밀번호를 잊으셨나요?";
                bodyDesc = "비밀번호 재설정을 위해 아래의 인증 번호를<br/>입력창에 입력해 주세요.";
                break;
            case "UPDATE":
                subject = "[KSAI] 계정 이메일 변경 인증 안내";
                title = "이메일 변경";
                bodyHeader = "새로운 이메일로 변경하시나요?";
                bodyDesc = "안전한 계정 관리를 위해 아래의 6자리 인증 번호를<br/>마이페이지에 입력해 주세요.";
                break;
            default: // "JOIN" (기본값)
                subject = "[KSAI] 회원가입 이메일 인증 번호 안내";
                title = "이메일 인증 번호 안내";
                bodyHeader = "안녕하세요! KSAI 가입을 환영합니다.";
                bodyDesc = "계속 진행하시려면 아래의 6자리 인증 번호를<br/>회원가입 화면에 입력해 주세요.";
                break;
        }

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);

            String htmlContent = """
                    <div style="font-family: 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; background-color: #f8fafc; padding: 60px 20px; margin: 0;">
                        <div style="max-width: 500px; margin: 0 auto; background-color: #ffffff; padding: 50px 40px; border-radius: 24px; box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05); text-align: center;">
                            
                            <h1 style="color: #0f172a; margin: 0 0 10px 0; font-size: 32px; font-weight: 900; letter-spacing: -1px;">KSAI</h1>
                            <h2 style="color: #475569; margin: 0 0 30px 0; font-size: 18px; font-weight: 600;">%s</h2>
                            
                            <p style="color: #64748b; font-size: 15px; line-height: 1.6; margin: 0 0 40px 0; word-break: keep-all;">
                                %s<br/>%s
                            </p>
                            
                            <div style="background-color: #f1f5f9; border-radius: 16px; padding: 24px; margin: 0 0 40px 0;">
                                <span style="font-size: 36px; font-weight: 800; color: #4f46e5; letter-spacing: 12px; display: inline-block; margin-left: 12px;">
                                    %s
                                </span>
                            </div>
                            
                            <div style="border-top: 1px solid #e2e8f0; padding-top: 24px;">
                                <p style="color: #94a3b8; font-size: 12px; line-height: 1.5; margin: 0;">
                                    본 메일은 발신 전용이며, 인증 번호는 5분간 유효합니다.<br/>
                                    만약 본인이 요청하지 않으셨다면 이 메일을 무시해 주세요.
                                </p>
                            </div>
                        </div>
                    </div>
                    """.formatted(title, bodyHeader, bodyDesc, code);

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송 중 오류가 발생했습니다.", e);
        }
    }

    public boolean verifyCode(String email, String code) {
        String savedCode = verificationCodes.get(email);

        if (savedCode != null && savedCode.equals(code)) {
            verificationCodes.remove(email);
            return true;
        }
        return false;
    }
}