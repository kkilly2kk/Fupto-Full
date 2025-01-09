package com.fupto.back.user.emitter.controller;

import com.fupto.back.auth.entity.FuptoUserDetails;
import com.fupto.back.user.emitter.dto.AlertDto;
import com.fupto.back.user.emitter.service.EmitterService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController("userEmitterController")
@RequestMapping("/user/member")
public class EmitterController {
    private final EmitterService emitterService;
    public EmitterController(EmitterService emitterService) {
        this.emitterService = emitterService;
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal FuptoUserDetails userDetails) {
        System.out.println("-------------------subscribe----------------");
        
        SseEmitter emitter = emitterService.createEmitter(userDetails.getId());

        try {
            // 초기 연결 확인을 위한 더미 데이터 전송
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @GetMapping("/alerts")
    public ResponseEntity<Page<AlertDto>> getAlerts(
            @AuthenticationPrincipal FuptoUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(emitterService.getAlerts(
                userDetails.getId(),
                PageRequest.of(page, size)
        ));
    }
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PatchMapping("/alerts/{alertId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long alertId) {
        emitterService.markAlertAsRead(alertId);
        System.out.println("-------작동확인-------"+alertId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PatchMapping("/readAll")
    public ResponseEntity<?> markAsReadAll(@AuthenticationPrincipal FuptoUserDetails userDetails) {
        emitterService.markAllAlertsAsRead(userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/alerts/{alertId}/delete")
    public ResponseEntity<?> softDeleteAlert(@PathVariable Long alertId) {
        emitterService.softDeleteAlert(alertId);
        return ResponseEntity.ok().build();
    }
}
