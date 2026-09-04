package com.student.iksu.service;

import com.student.iksu.dto.request.VoteSubmitDto;
import com.student.iksu.dto.response.CandidateResponseDto;
import com.student.iksu.entity.Candidate;
import com.student.iksu.entity.VoteVerificationCode;
import com.student.iksu.entity.VoterParticipation;
import com.student.iksu.repository.CandidateRepository;
import com.student.iksu.repository.VoteVerificationCodeRepository;
import com.student.iksu.repository.VoterParticipationRepository;
import jakarta.annotation.PostConstruct;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    private static final Pattern HUJI_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@mail\\.huji\\.ac\\.il$", Pattern.CASE_INSENSITIVE);

    // ★ [편의 기능] 관리자 등록 없이도 서버 구동 시 DB에 후보가 없으면 자동 생성
    @PostConstruct
    public void initSingleCandidateIfEmpty() {
        if (candidateRepository.count() == 0) {
            Candidate defaultCandidate = new Candidate(
                    1,
                    "DB에 후보가 없습니다",
                    "히브리대학교 짱",
                    "학생 여러분의 목소리로 더 가깝고 든든한 한인회를 만들겠습니다",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS2rchFPrbHvScd0pR4phXHN2zWcQcK68maCxYSpvlQqIQbxKA_UMt0ThgW&s=10",
                    Arrays.asList(
                            "후보가 등록되었는지 확인해주세요"
                    )
            );
            candidateRepository.save(defaultCandidate);
            log.info("단일 후보자가 DB에 자동 등록되었습니다.");
        }
    }

    // 1. 후보자 목록 조회
    @Transactional(readOnly = true)
    public List<CandidateResponseDto> getCandidates() {
        return candidateRepository.findAllByOrderByCandidateNumberAsc().stream()
                .map(CandidateResponseDto::from)
                .collect(Collectors.toList());
    }

    // ★ 2. 실시간 찬/반 집계 통계 조회
    @Transactional(readOnly = true)
    public Map<String, Integer> getVoteStats() {
        List<Candidate> candidates = candidateRepository.findAllByOrderByCandidateNumberAsc();
        if (candidates.isEmpty()) {
            return Map.of("approve", 0, "reject", 0);
        }
        Candidate candidate = candidates.get(0);
        return Map.of(
                "approve", candidate.getVotes() != null ? candidate.getVotes() : 0,
                "reject", candidate.getRejectVotes() != null ? candidate.getRejectVotes() : 0
        );
    }

    // 3. 투표용 이메일 인증코드 발송
    public void sendVoteEmailCode(String email) {
        String cleanEmail = email.trim();

        if (!HUJI_EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new IllegalArgumentException("히브리대학교 웹메일(@mail.huji.ac.il)만 유효합니다.");
        }

        if (voterParticipationRepository.existsByEmail(cleanEmail)) {
            throw new IllegalStateException("이미 이번 선거에 투표를 완료한 이메일입니다.");
        }

        voteVerificationCodeRepository.deleteByEmail(cleanEmail);
        String code = generate6DigitCode();

        VoteVerificationCode verificationCode = new VoteVerificationCode(cleanEmail, code, 5);
        voteVerificationCodeRepository.save(verificationCode);

        sendHtmlEmail(cleanEmail, code);
    }

    // 4. 인증번호 확인
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

    // 5. 최종 찬/반 투표 제출
    @Transactional
    public void submitVote(VoteSubmitDto dto) {
        String email = dto.getEmail().trim();

        if (!HUJI_EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }

        if (voterParticipationRepository.existsByEmail(email)) {
            throw new IllegalStateException("이미 투표에 참여하셨습니다.");
        }

        VoteVerificationCode verifyCode = voteVerificationCodeRepository.findTopByEmailOrderByExpireAtDesc(email)
                .orElseThrow(() -> new IllegalStateException("이메일 인증이 완료되지 않았습니다."));

        if (!verifyCode.isVerified() || !verifyCode.getCode().equals(dto.getCode().trim())) {
            throw new IllegalStateException("인증 정보가 유효하지 않습니다.");
        }

        // 단일 후보 식별 (전달된 candidateId가 없거나 0이면 첫 번째 후보 지정)
        Long targetCandidateId = dto.getCandidateId();
        Candidate candidate;
        if (targetCandidateId != null && targetCandidateId > 0) {
            candidate = candidateRepository.findById(targetCandidateId)
                    .orElseGet(() -> candidateRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new IllegalStateException("등록된 후보자가 없습니다.")));
        } else {
            candidate = candidateRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("등록된 후보자가 없습니다."));
        }

        // 1. 참여자 이메일 저장 (1인 1표 보장용 - 선택 내용은 저장하지 않아 비밀투표 보장)
        voterParticipationRepository.save(new VoterParticipation(email));

        // 2. ★ 찬성/반대에 따른 득표수 1 증가 (동시성 안전 쿼리)
        boolean isReject = "REJECT".equalsIgnoreCase(dto.getChoice()) || Long.valueOf(0).equals(dto.getCandidateId());
        if (isReject) {
            candidateRepository.incrementRejectVoteCount(candidate.getId());
        } else {
            candidateRepository.incrementVoteCount(candidate.getId());
        }

        // 3. 인증코드 삭제 (재사용 방지)
        voteVerificationCodeRepository.deleteByEmail(email);
    }

    // 6. 투표 여부 확인
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