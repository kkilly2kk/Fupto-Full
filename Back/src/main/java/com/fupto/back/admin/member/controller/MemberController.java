package com.fupto.back.admin.member.controller;

import com.fupto.back.admin.member.dto.MemberDetailDto;
import com.fupto.back.admin.member.dto.MemberListDto;
import com.fupto.back.admin.member.dto.MemberResponseDto;
import com.fupto.back.admin.member.dto.MemberSearchDto;
import com.fupto.back.admin.member.service.MemberService;
import com.fupto.back.entity.Member;
import com.fupto.back.repository.MemberRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController("adminMemberController")
@RequestMapping("admin/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원 목록 조회 (검색/필터링)
    @GetMapping("/search")
    public ResponseEntity<MemberResponseDto> getMembers(@ModelAttribute MemberSearchDto memberSearchDto) {
        return ResponseEntity.ok(memberService.getMemberList(memberSearchDto));
    }

    // 회원 상세 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<MemberDetailDto> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    // 회원 상태(active) 변경
    @PatchMapping("/{id}/active")
    public ResponseEntity<?> updateMemberActive(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        memberService.updateMemberActive(id, active);
        return ResponseEntity.ok().build();
    }

    // 회원 권한 변경
    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateMemberRole(
            @PathVariable Long id,
            @RequestBody String role) {
        memberService.updateMemberRole(id, role);
        return ResponseEntity.ok().build();
    }

    // 회원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok().build();
    }

    // 찜한 상품 이미지 조회
    @GetMapping("/fav/{id}/image")
    public ResponseEntity<Resource> getMembersFavImg(@PathVariable Long id) throws IOException {
        Resource resource = memberService.getProductImage(id);
        String contentType = Files.probeContentType(Paths.get(resource.getURI()));

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
