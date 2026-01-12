package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.adapter;

import com.hackathon.databeats.churninsight.application.port.output.BatchSavePort;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistoryEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Adapter JDBC otimizado para inserção em massa.
 * Usa multi-row INSERT com paralelismo agressivo.
 */
@Slf4j
@Repository("jdbcBatchPersistenceAdapter")
@Primary
public class JdbcBatchPersistenceAdapter implements BatchSavePort {

    private final JdbcTemplate jdbcTemplate;
    private final ExecutorService dbExecutor;
    private final int chunkSize;

    // SQL base - Single row insert for batch update
    private static final String INSERT_SQL =
        "INSERT INTO churn_history (" +
        "id, user_id, gender, age, country, subscription_type, " +
        "listening_time, songs_played_per_day, skip_rate, " +
        "ads_listened_per_week, device_type, offline_listening, " +
        "churn_status, probability, created_at, requester_id, request_ip, " +
        "frustration_index, ad_intensity, songs_per_minute, is_heavy_user, premium_no_offline" +
        ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public JdbcBatchPersistenceAdapter(
            JdbcTemplate jdbcTemplate,
            @Value("${app.db.insert-threads:16}") int insertThreads,
            @Value("${app.db.chunk-size:2000}") int chunkSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.chunkSize = chunkSize;
        this.dbExecutor = Executors.newFixedThreadPool(insertThreads);
        log.info("🚀 JdbcBatchPersistenceAdapter: {} threads, chunk size {}", insertThreads, chunkSize);
    }

    @Override
    public void saveAll(List<PredictionHistoryEntity> entities) {
        if (entities.isEmpty()) return;

        // Divide em chunks e executa em paralelo máximo
        List<CompletableFuture<Void>> futures = new ArrayList<>((entities.size() / chunkSize) + 1);

        for (int i = 0; i < entities.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, entities.size());
            List<PredictionHistoryEntity> chunk = entities.subList(i, end);

            futures.add(CompletableFuture.runAsync(() -> insertMultiRow(chunk), dbExecutor));
        }

        // Aguarda todos em paralelo
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * MULTI-ROW INSERT otimizado usando JDBC Batch + RewriteBatchedStatements.
     *
     * Muda estratégia de construir String gigante (que sobrecarrega parser)
     * para usar batching padrão do driver JDBC, que é muito mais eficiente
     * quando rewriteBatchedStatements=true.
     */
    private void insertMultiRow(List<PredictionHistoryEntity> entities) {
        if (entities.isEmpty()) return;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        try {
             jdbcTemplate.batchUpdate(INSERT_SQL, entities, entities.size(),
                (ps, e) -> {
                    int col = 1;
                    ps.setString(col++, e.getId());
                    ps.setString(col++, e.getUserId());
                    ps.setString(col++, e.getGender());
                    ps.setInt(col++, e.getAge());
                    ps.setString(col++, e.getCountry());
                    ps.setString(col++, e.getSubscriptionType());
                    ps.setDouble(col++, e.getListeningTime());
                    ps.setInt(col++, e.getSongsPlayedPerDay());
                    ps.setDouble(col++, e.getSkipRate());
                    ps.setInt(col++, e.getAdsListenedPerWeek());
                    ps.setString(col++, e.getDeviceType());
                    ps.setBoolean(col++, e.isOfflineListening());
                    ps.setString(col++, e.getChurnStatus().name());
                    ps.setDouble(col++, e.getProbability());
                    ps.setTimestamp(col++, e.getCreatedAt() != null ? Timestamp.valueOf(e.getCreatedAt()) : now);
                    ps.setString(col++, e.getRequesterId());
                    ps.setString(col++, e.getRequestIp());
                    ps.setDouble(col++, e.getFrustrationIndex());
                    ps.setDouble(col++, e.getAdIntensity());
                    ps.setDouble(col++, e.getSongsPerMinute());
                    ps.setBoolean(col++, e.getIsHeavyUser());
                    ps.setBoolean(col++, e.getPremiumNoOffline());
                });
        } catch (Exception e) {
             log.error("Erro ao salvar batch de {} registros: {}", entities.size(), e.getMessage());
             throw e;
        }
    }

    @Override
    public void saveBatch(List<PredictionHistoryEntity> entities, int batchSize) {
        saveAll(entities);
    }

    @Override
    public long countTotalPredictions() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM churn_history", Long.class);
        return count != null ? count : 0L;
    }
}