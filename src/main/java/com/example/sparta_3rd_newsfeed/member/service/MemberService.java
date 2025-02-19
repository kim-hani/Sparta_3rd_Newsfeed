package com.example.sparta_3rd_newsfeed.member.service;

import com.example.sparta_3rd_newsfeed.common.encoder.PasswordEncoder;
import com.example.sparta_3rd_newsfeed.member.dto.requestDto.DeleteMemberRequestDto;
import com.example.sparta_3rd_newsfeed.member.dto.MemberUpdateRequestDto;
import com.example.sparta_3rd_newsfeed.member.dto.requestDto.LoginRequestDto;
import com.example.sparta_3rd_newsfeed.member.dto.requestDto.SignUpRequestDto;
import com.example.sparta_3rd_newsfeed.member.dto.responseDto.SignUpResponseDto;
import com.example.sparta_3rd_newsfeed.member.entity.Member;
import com.example.sparta_3rd_newsfeed.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public Member updateMember(Long memberId, MemberUpdateRequestDto updateRequestDto) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 2. 현재 비밀번호 확인
        if (!passwordEncoder.matches(updateRequestDto.getOldPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 3. 새 비밀번호 형식 검증 및 변경
        // 3. 새 비밀번호 형식 검증 및 변경
        if (updateRequestDto.isPasswordcheck()) {  // passwordcheck가 true일 때 새 비밀번호 확인
            if (!updateRequestDto.getNewPassword().equals(updateRequestDto.getNewPassword())) {
                throw new IllegalArgumentException("새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.");
            }


            // 비밀번호 변경
            String encodedPassword = passwordEncoder.encode(updateRequestDto.getNewPassword());
            member.setPassword(encodedPassword);
        }

        // 4. 소개글 수정
        if (updateRequestDto.getProfileBio() != null) {
            if (updateRequestDto.getProfileBio().length() > 30) {
                throw new IllegalArgumentException("소개글은 30자 이하로 작성해주세요.");
            }
            member.setProfileBio(updateRequestDto.getProfileBio());
        }

        // 5. 프로필 이미지 수정
        if (updateRequestDto.getProfileImg() != null) {
            member.setProfileImg(updateRequestDto.getProfileImg());
        }

        // 6. 수정된 회원 저장
        return memberRepository.save(member);
    }


    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto) {

        // 활성화된 계정이 있는지 확인
        Optional<Member> existingMember = memberRepository.findByEmail(signUpRequestDto.getEmail());

        if (existingMember.isPresent()) {
            Member member = existingMember.get();

            // 탈퇴한 계정이지만 탈퇴한 지 1달이 지나지 않은 계정일 경우
            if(member.isDeleted() && member.getDeletedAt() != null){
                LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
                if(member.getDeletedAt().isAfter(oneMonthAgo)){
                    member.setDeleted(false);
                    member.setDeletedAt(null);
                    member.setUsername(signUpRequestDto.getUsername());

                    if(signUpRequestDto.getProfileImg() != null){
                        member.setProfileImg(signUpRequestDto.getProfileImg());
                    }

                    if(signUpRequestDto.getProfileBio() != null){
                        member.setProfileBio(signUpRequestDto.getProfileBio());
                    }

                    member.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));

                    memberRepository.save(member);
                    return new SignUpResponseDto(member.getUsername(),member.getEmail());
                }else{

                    // 1달이 지났으면 계정 완전히 삭제 후 신규가입
                    memberRepository.delete(member);
                }
            }else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 사용중인 이메일 입니다.");
            }
        }

        // 비밀번호 유효성 검사
        if (!signUpRequestDto.getPassword().matches("(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 8~16자 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.");
        }

        // 신규 회원가입 처리
        Member member = new Member();
        member.setUsername(signUpRequestDto.getUsername());
        member.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        member.setEmail(signUpRequestDto.getEmail());
        member.setProfileImg(signUpRequestDto.getProfileImg());
        member.setProfileBio(signUpRequestDto.getProfileBio());

        memberRepository.save(member);

        return new SignUpResponseDto(member.getUsername(), member.getEmail());
    }


    public Member login(LoginRequestDto loginRequestDto) {
        Member member = memberRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일이나 비밀번호가 일치하지 않습니다."));

        // 탈퇴한 계정인지 확인
        if (member.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이미 탈퇴한 계정입니다. 다시 가입해주세요.");
        }

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일이나 비밀번호가 일치하지 않습니다.");
        }

        return member;
    }


    public void deleteMember(DeleteMemberRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"이메일이나 비밀번호가 일치하지 않습니다."));

        if(member.isDeleted()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"이메일이나 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일이나 비밀번호가 일치하지 않습니다.");
        }

        member.setDeleted(true);
        member.setDeletedAt(LocalDateTime.now());
        memberRepository.save(member);
    }
}
