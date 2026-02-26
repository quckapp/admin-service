package com.quckapp.admin.domain.repository;

import com.quckapp.admin.domain.entity.PromotionRecord;
import com.quckapp.admin.domain.entity.PromotionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PromotionRecordRepository extends JpaRepository<PromotionRecord, UUID> {
    List<PromotionRecord> findByServiceKeyAndApiVersionOrderByCreatedAtDesc(String serviceKey, String apiVersion);
    List<PromotionRecord> findByToEnvironmentOrderByCreatedAtDesc(String toEnvironment);
    List<PromotionRecord> findByPromotionTypeOrderByCreatedAtDesc(PromotionType promotionType);
}
