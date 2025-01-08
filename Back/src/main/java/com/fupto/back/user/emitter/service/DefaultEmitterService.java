package com.fupto.back.user.emitter.service;

import com.fupto.back.entity.Alert;
import com.fupto.back.entity.Favorite;
import com.fupto.back.entity.Product;
import com.fupto.back.repository.*;
import com.fupto.back.user.emitter.dto.AlertDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service("userEmitterService")
@Transactional
public class DefaultEmitterService implements EmitterService {
    private static final Long TIMEOUT = 5L * 60 * 1000;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> eventCache = new ConcurrentHashMap<>();
    private final AlertRepository alertRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public DefaultEmitterService(AlertRepository alertRepository,
                                 MemberRepository memberRepository,
                                 ProductRepository productRepository,
                                 FavoriteRepository favoriteRepository,
                                 PriceHistoryRepository priceHistoryRepository) {
        this.alertRepository = alertRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.favoriteRepository = favoriteRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Override
    public SseEmitter createEmitter(Long memberId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(memberId, emitter);

        // 최초 연결시 캐시된 이벤트 전송
        Map<String, Object> cachedEvents = eventCache.getOrDefault(memberId, new ConcurrentHashMap<>());
        cachedEvents.forEach((eventId, event) -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(eventId)
                        .name("alert")
                        .data(event));
            } catch (IOException e) {
                emitters.remove(memberId);
            }
        });

        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(() -> emitters.remove(memberId));

        return emitter;
    }

    @Override
    public void createAndSaveAlert(Long memberId, String alertType, String message, Long referenceId) {
        Alert alert = Alert.builder()
                .member(memberRepository.getReferenceById(memberId))
                .alertType(alertType)
                .message(message)
                .referenceId(referenceId)
                .isRead(false)
                .isDeleted(false)
                .createDate(Instant.now())
                .build();

        alertRepository.save(alert);
    }

    @Override
    public void notifyPriceAlert(Long memberId, Long productId, Integer newPrice, Integer alertPrice) {
        String message = createAlertMessage(productId, newPrice, alertPrice);
        createAndSaveAlert(memberId, "PRICE_ALERT", message, productId);
        sendToEmitter(memberId, "PRICE_ALERT", message);
    }

    private String createAlertMessage(Long productId, Integer newPrice, Integer alertPrice) {
        if (alertPrice != null) {
            return String.format("관심 상품(ID: %d)의 가격이 설정하신 %d원 이하로 떨어져 %d원으로 변경되었습니다.",
                    productId, alertPrice, newPrice);
        }
        return String.format("관심 상품(ID: %d)의 가격이 %d원으로 변경되었습니다.",
                productId, newPrice);
    }

    @Override
    public void sendToEmitter(Long memberId, String alertType, Object data) {
        // 이벤트 캐시에 저장
        String eventId = memberId + "_" + System.currentTimeMillis();
        eventCache.computeIfAbsent(memberId, k -> new ConcurrentHashMap<>())
                .put(eventId, data);

        // 실시간 전송
        SseEmitter emitter = emitters.get(memberId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .id(eventId)
                        .name(alertType)
                        .data(data));
            } catch (IOException e) {
                emitters.remove(memberId);
            }
        }
    }

    @Override
    public void checkAlertCondition(Favorite favorite, Integer oldAlertPrice){
        Integer lowestPrice = findLowestPriceByMappingId(favorite.getMappingId());
//        System.out.println("checkAlertCondition lowestPrice : "+ lowestPrice);

        if ((oldAlertPrice == null && favorite.getAlertPrice() != null)
                || (favorite.getAlertPrice() != null && lowestPrice <= favorite.getAlertPrice())){
            String message = createAlertMessage(favorite.getMappingId(), lowestPrice, favorite.getAlertPrice());
            System.out.println("checkAlertCondition message : "+message);
        }
    }

    @Transactional
    @Override
    public void checkerforfavPrice(Long productId, Integer newPrice){
        List<Favorite> favorites = favoriteRepository.findByMappingId(productId);
        Integer lowestPrice = findLowestPriceByMappingId(productId);
        String alertType = "PRICE_ALERT";
        for (Favorite favorite : favorites){
            Integer alertPrice = favorite.getAlertPrice();
            if (alertPrice == null || lowestPrice <= alertPrice){
                String message = createAlertMessage(productId, newPrice, alertPrice);
                sendToEmitter(favorite.getMember().getId(), alertType, message);

                createAndSaveAlert(favorite.getMember().getId(), alertType, message, productId);
            }
        }
    }

    // 캐시 정리 (주기적으로 호출)
    public void cleanEventCache(Long memberId) {
        eventCache.remove(memberId);
    }


    @Override
    public Page<AlertDto> getUnreadAlerts(Long memberId, Pageable pageable) {
        return alertRepository
                .findByMemberIdAndIsReadFalseAndIsDeletedFalseOrderByCreateDateDesc(memberId, pageable)
                .map(alert -> {
                    String productName = productRepository
                            .findById(alert.getReferenceId())
                            .map(Product::getProductName)
                            .orElse(""); // 상품이 없는 경우 빈 문자열 반환

                    return AlertDto.builder()
                            .id(alert.getId())
                            .message(alert.getMessage())
                            .createDate(alert.getCreateDate())
                            .alertType(alert.getAlertType())
                            .isRead(alert.getIsRead())
                            .isDeleted(alert.getIsDeleted())
                            .referName(productName)
                            .memberName(alert.getMember().getNickname())
                            .build();
                });
    }

    @Override
    public void markAlertAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId).orElseThrow( () ->
                new RuntimeException("Alert not found"));
        alert.setIsRead(true);
        alertRepository.save(alert);
    }

    @Override
    public void markAllAlertsAsRead(Long alertId) {
        List<Alert> alerts = alertRepository.findAllByMemberId(alertId);
        alerts.forEach(alert -> alert.setIsRead(true));
        alertRepository.saveAll(alerts);
    }

    @Override
    public void softDeleteAlert(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("알림을 찾을 수 없습니다"));
        alert.setIsDeleted(true);
        alertRepository.save(alert);
    }

    @Override
    public void removeEmitter(Long memberId) {
        emitters.remove(memberId);
    }

    //최저가 찾는 로직
    @Override
    public Integer findLowestPriceByMappingId(Long mappingId) {
        List<Product> products = productRepository.findAllByMappingId(mappingId);
        //mappingid가 변경 된 경우 찾아가는 로직
        if (products == null || products.isEmpty()){
            Product findProduct = productRepository.findById(mappingId).orElse(null);
            Long findProductId = findProduct.getMappingId().longValue();
            products = productRepository.findAllByMappingId(findProductId);
        }
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());

        return priceHistoryRepository.findLowestPriceByProductIdsAndLatestDate(productIds)
                .orElseThrow(() -> new EntityNotFoundException("No price found for mapping id: " + mappingId));
    }
}