package com.fupto.back.repository;

import com.fupto.back.entity.Alert;
import com.fupto.back.user.emitter.dto.AlertDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository  extends JpaRepository<Alert, Long> {
    Optional<Alert> findByMemberIdAndAlertType(Long memberId, String alertType);
    Optional<Alert> findByMemberId(Long memberId);
    List<Alert> findAllByMemberId(Long longs);
    Page<Alert> findByMemberIdAndIsReadFalseAndIsDeletedFalseOrderByCreateDateDesc(Long memberId, Pageable pageable);
}
