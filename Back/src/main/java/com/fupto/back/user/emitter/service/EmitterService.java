package com.fupto.back.user.emitter.service;

import com.fupto.back.entity.Alert;
import com.fupto.back.entity.Favorite;
import com.fupto.back.user.emitter.dto.AlertDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface EmitterService {
    SseEmitter createEmitter(Long memberId);
    void createAndSaveAlert(Long memberId, String alertType, String message, Long referenceId);
    void checkerforfavPrice(Long productId, Integer newPrice);
    void softDeleteAlert(Long alertId);
    void sendToEmitter(Long memberId, String alertType, Object data);
    Page<AlertDto> getAlerts(Long memberId, Pageable pageable);
    void markAlertAsRead(Long alertId);
    void markAllAlertsAsRead(Long alertId);
    Integer findLowestPriceByMappingId(Long mappingId);

}
