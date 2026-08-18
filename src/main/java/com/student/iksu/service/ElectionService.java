package com.student.iksu.service;

import com.student.iksu.dto.request.VoteSubmitDto;
import com.student.iksu.dto.response.CandidateResponseDto;
import com.student.iksu.entity.Candidate;
import com.student.iksu.entity.VoteVerificationCode;
import com.student.iksu.entity.VoterParticipation;
import com.student.iksu.repository.CandidateRepository;
import com.student.iksu.repository.VoteVerificationCodeRepository;
import com.student.iksu.repository.VoterParticipationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ElectionService {

    private final CandidateRepository candidateRepository;
    private final VoterParticipationRepository voterParticipationRepository;
    private final VoteVerificationCodeRepository voteVerificationCodeRepository;
    private final JavaMailSender mailSender;

    // 히브리대학교 이메일 정규식 검증
    private static final Pattern HUJI_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@mail\\.huji\\.ac\\.il$", Pattern.CASE_INSENSITIVE);

    // 1. 후보자 목록 조회
    @Transactional(readOnly = true)
    public List<CandidateResponseDto> getCandidates() {
        return candidateRepository.findAllByOrderByCandidateNumberAsc().stream()
                .map(CandidateResponseDto::from)
                .collect(Collectors.toList());
    }

    // 2. 투표용 히브리대 이메일 인증코드 발송
    public void sendVoteEmailCode(String email) {
        String cleanEmail = email.trim();

        // 도메인 검증
        if (!HUJI_EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException("히브리대학교 웹메일(@mail.huji.ac.il)만 유효합니다.");
        }

        // 이미 투표한 이메일인지 체크
        if (voterParticipationRepository.existsByEmail(cleanEmail)) {
            throw new IllegalStateException("이미 이번 선거에 투표를 완료한 이메일입니다.");
        }

        // 기존 잔여 코드 정리 후 6자리 난수 생성
        voteVerificationCodeRepository.deleteByEmail(cleanEmail);
        String code = generate6DigitCode();

        // 5분 유효시간으로 저장
        VoteVerificationCode verificationCode = new VoteVerificationCode(cleanEmail, code, 5);
        voteVerificationCodeRepository.save(verificationCode);

        // Gmail 메일 발송
        sendHtmlEmail(cleanEmail, code);
    }

    // 3. 인증번호 확인
    public boolean verifyVoteCode(String email, String code) {
        String cleanEmail = email.trim();
        VoteVerificationCode verifyCode = voteVerificationCodeRepository.findTopByEmailOrderByExpireAtDesc(cleanEmail)
                .orElseThrow(() -> new IllegalStateException("인증번호 요청 기록이 없습니다."));

        if (verifyCode.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증번호 유효시간(5분)이 만료되었습니다. 다시 요청해주세요.");
        }

        if (!verifyCode.getCode().equals(code.trim())) {
            return false;
        }

        verifyCode.setVerified(true);
        voteVerificationCodeRepository.save(verifyCode);
        return true;
    }

    // 4. 최종 투표 제출 (트랜잭션)
    @Transactional
    public void submitVote(VoteSubmitDto dto) {
        String email = dto.getEmail().trim();

        // 이메일 검증
        if (!HUJI_EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }

        // 중복 투표 방지 (DB 유니크 제약 포함)
        if (voterParticipationRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 투표에 참여하셨습니다.");
        }

        // 인증 여부 재검증 (보안)
        VoteVerificationCode verifyCode = voteVerificationCodeRepository.findTopByEmailOrderByExpireAtDesc(email)
                .orElseThrow(() -> new IllegalStateException("이메일 인증이 완료되지 않았습니다."));

        if (!verifyCode.isVerified() || !verifyCode.getCode().equals(dto.getCode().trim())) {
            throw new IllegalStateException("인증 정보가 유효하지 않습니다.");
        }

        // 후보자 존재 확인
        Candidate candidate = candidateRepository.findById(dto.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("선택하신 후보자가 존재하지 않습니다."));

        // 1. 참여 기록 저장 (1인 1표용 - 선택 내용은 저장 안 함: 비밀투표 보장)
        voterParticipationRepository.save(new VoterParticipation(email));

        // 2. 후보자 득표수 1 증가 (동시성 안전 쿼리)
        candidateRepository.incrementVoteCount(candidate.getId());

        // 3. 인증코드 삭제 (재사용 방지)
        voteVerificationCodeRepository.deleteByEmail(email);
    }

    // 5. 투표 여부 확인
    @Transactional(readOnly = true)
    public boolean checkHasVoted(String email) {
        return voterParticipationRepository.existsByEmail(email.trim());
    }

    // --- 내부 헬퍼 메소드 ---

    private String generate6DigitCode() {
        SecureRandom random = new SecureRandom();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }

    private void sendHtmlEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[SKAI] 히브리대 한인학생회장 선거 투표 본인 인증번호");

            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 25px; border: 1px solid #e2e8f0; border-radius: 20px; text-align: center;'>"
                    + "<h2 style='color: #0f172a; margin-bottom: 10px;'>히브리대 한인학생회장 선거 투표</h2>"
                    + "<p style='color: #64748b; font-size: 14px; margin-bottom: 25px;'>본인 확인을 위한 인증번호입니다. 화면에 아래 6자리 번호를 입력해주세요.</p>"
                    + "<div style='background-color: #f1f5f9; padding: 18px; border-radius: 12px; font-size: 30px; font-weight: bold; letter-spacing: 6px; color: #4f46e5; margin-bottom: 20px;'>"
                    + code
                    + "</div>"
                    + "<p style='color: #94a3b8; font-size: 12px;'>* 인증번호는 5분 동안만 유효합니다.<br/>* 본인이 요청하지 않은 경우 이 메일을 무시하셔도 됩니다.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("투표 인증 메일 전송 실패 to: {}", toEmail, e);
            throw new IllegalStateException("인증 메일 발송에 실패했습니다. 이메일 주소를 다시 확인해주세요.");
        }
    }
}