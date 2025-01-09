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
                .build();

        alertRepository.save(alert);
    }

    private String createAlertMessage(Long productId, Integer newPrice, Integer alertPrice) {
        if (alertPrice == null) {
            return null;
        }
        
        return String.format(
                "찜 상품의 최저 가격이 설정하신 가격(%,d원) 이하로 떨어져 %,d원으로 변경 되었습니다.",
                alertPrice,
                newPrice
        );
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

    @Transactional
    @Override
    public void checkerforfavPrice(Long productId, Integer newPrice){
        List<Favorite> favorites = favoriteRepository.findByMappingId(productId);
        Integer lowestPrice = findLowestPriceByMappingId(productId);
        String alertType = "PRICE_ALERT";
        for (Favorite favorite : favorites){
            Integer alertPrice = favorite.getAlertPrice();
            if (alertPrice != null && lowestPrice <= alertPrice){
                String message = createAlertMessage(productId, newPrice, alertPrice);
                sendToEmitter(favorite.getMember().getId(), alertType, message);
                createAndSaveAlert(favorite.getMember().getId(), alertType, message, productId);  // DB에도 저장
            }
        }
    }

    // 캐시 정리 (주기적으로 호출)
    public void cleanEventCache(Long memberId) {
        eventCache.remove(memberId);
    }


    @Override
    public Page<AlertDto> getAlerts(Long memberId, Pageable pageable) {
        return alertRepository
                .findByMemberIdAndIsDeletedFalseOrderByCreateDateDesc(memberId, pageable)
                .map(alert -> {
                    Product product = productRepository
                            .findById(alert.getReferenceId())
                            .orElse(null);

                    String productName = (product != null) ? product.getProductName() : "";
                    String brandEngName = (product != null && product.getBrand() != null)
                            ? product.getBrand().getEngName() : "";

                    return AlertDto.builder()
                            .id(alert.getId())
                            .message(alert.getMessage())
                            .createDate(alert.getCreateDate())
                            .alertType(alert.getAlertType())
                            .isRead(alert.getIsRead())
                            .isDeleted(alert.getIsDeleted())
                            .referName(productName)
                            .referBrandName(brandEngName)
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