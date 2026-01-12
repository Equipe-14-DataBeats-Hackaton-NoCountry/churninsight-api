package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository;

import com.hackathon.databeats.churninsight.application.port.output.BatchSavePort;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistoryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class JdbcBatchRepository implements BatchSavePort {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = """
        INSERT INTO churn_history (
            id, user_id, gender, age, country, subscription_type, 
            listening_time, songs_played_per_day, skip_rate, 
            ads_listened_per_week, device_type, offline_listening, 
            churn_status, probability, created_at, requester_id, request_ip,
            frustration_index, ad_intensity, songs_per_minute, 
            is_heavy_user, premium_no_offline
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    @Override
    @Transactional
    public void saveAll(List<PredictionHistoryEntity> entities) {
        // OTIMIZAÇÃO: Batch de 10k para melhor throughput em arquivos grandes
        saveBatch(entities, 10000);
    }

    @Override
    @Transactional
    public void saveBatch(List<PredictionHistoryEntity> entities, int batchSize) {
        if (entities.isEmpty()) return;

        try {
            jdbcTemplate.batchUpdate(INSERT_SQL, entities, batchSize,
                    (PreparedStatement ps, PredictionHistoryEntity entity) -> {
                        ps.setString(1, entity.getId());
                        ps.setString(2, entity.getUserId());
                        ps.setString(3, entity.getGender());

                        // Campos primitivos (int/double/boolean) não precisam de check != null
                        ps.setInt(4, entity.getAge());
                        ps.setString(5, entity.getCountry());
                        ps.setString(6, entity.getSubscriptionType());
                        ps.setDouble(7, entity.getListeningTime());
                        ps.setInt(8, entity.getSongsPlayedPerDay());
                        ps.setDouble(9, entity.getSkipRate());
                        ps.setInt(10, entity.getAdsListenedPerWeek());
                        ps.setString(11, entity.getDeviceType());

                        // CORREÇÃO: Getter de boolean no Lombok é 'is'
                        ps.setBoolean(12, entity.isOfflineListening());

                        ps.setString(13, entity.getChurnStatus() != null ? entity.getChurnStatus().name() : "UNKNOWN");
                        ps.setDouble(14, entity.getProbability());

                        ps.setTimestamp(15, entity.getCreatedAt() != null ? Timestamp.valueOf(entity.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
                        ps.setString(16, entity.getRequesterId());
                        ps.setString(17, entity.getRequestIp());

                        // Campos Double/Boolean (Wrappers) ainda precisam do check != null
                        ps.setDouble(18, entity.getFrustrationIndex() != null ? entity.getFrustrationIndex() : 0.0);
                        ps.setDouble(19, entity.getAdIntensity() != null ? entity.getAdIntensity() : 0.0);
                        ps.setDouble(20, entity.getSongsPerMinute() != null ? entity.getSongsPerMinute() : 0.0);
                        ps.setBoolean(21, entity.getIsHeavyUser() != null ? entity.getIsHeavyUser() : false);
                        ps.setBoolean(22, entity.getPremiumNoOffline() != null ? entity.getPremiumNoOffline() : false);
                    });
        } catch (Exception e) {
            log.error("Erro ao salvar no banco via JDBC: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public long countTotalPredictions() {
        // CORREÇÃO: Mudei para churn_history para bater com sua @Table
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM churn_history", Long.class);
        return count != null ? count : 0L;
    }
}