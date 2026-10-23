package com.ticketrun.domain.member.api;

import com.ticketrun.common.model.BaseResponse;
import com.ticketrun.domain.member.model.dto.response.MemberResponse;
import com.ticketrun.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<MemberResponse> getMyInfo(@AuthenticationPrincipal Long memberId) {
        return new BaseResponse<>(memberService.getMember(memberId), "회원 정보를 조회했습니다.");
    }
}
