package com.example.sparta_3rd_newsfeed.member.controller;

import com.example.sparta_3rd_newsfeed.member.dto.requestDto.DeleteMemberRequestDto;
import com.example.sparta_3rd_newsfeed.member.dto.MemberUpdateRequestDto;
import com.example.sparta_3rd_newsfeed.member.dto.requestDto.LoginRequestDto;
import com.example.sparta_3rd_newsfeed.member.dto.requestDto.SignUpRequestDto;
import com.example.sparta_3rd_newsfeed.member.dto.responseDto.LoginResponseDto;
import com.example.sparta_3rd_newsfeed.member.dto.responseDto.SignUpResponseDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.sparta_3rd_newsfeed.member.entity.Member;
import com.example.sparta_3rd_newsfeed.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 내 정보 수정
    @PutMapping("/me")
    public ResponseEntity<Member> updateProfile(
            @AuthenticationPrincipal Long loginUserId,  // 로그인된 사용자의 ID
            @RequestBody @Valid MemberUpdateRequestDto updateRequestDto,  // @Valid 추가
            BindingResult bindingResult) {  // 유효성 검사 결과
        //유효성 검사 오류 발생 확인
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(null); //잘못된 요청시 400상태코드 반환
        }

        Member updatedMember = memberService.updateMember(loginUserId, updateRequestDto);
        return ResponseEntity.ok(updatedMember);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignUpRequestDto signUpRequestDto,
            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(error -> error.getField() + " : " + error.getDefaultMessage()) // 필드명과 메시지 함께 반환
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }
        SignUpResponseDto signUpResponseDto = memberService.signUp(signUpRequestDto);
        return new ResponseEntity<>(signUpResponseDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto loginRequestDto,
            HttpServletRequest request) {

        Member member = memberService.login(loginRequestDto);

        HttpSession session = request.getSession();
        session.setAttribute("member", member);
        return new ResponseEntity<>(new LoginResponseDto("로그인 성공"), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMember(
            @RequestBody DeleteMemberRequestDto requestDto
    ) {

        memberService.deleteMember(requestDto);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}

