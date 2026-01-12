package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.PaginatedResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionHistoryResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionSearchFilter;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistoryEntity;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryRepository;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.specification.PredictionHistorySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para busca paginada e filtrada de histórico de predições.
 * Otimizado para grandes volumes de dados.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PredictionHistoryService {

    private final PredictionHistoryRepository repository;

    // Mapa de ações recomendadas por fator de risco
    private static final Map<String, String> RECOMMENDED_ACTIONS = createActionsMap();

    private static Map<String, String> createActionsMap() {
        Map<String, String> actions = new HashMap<>();
        actions.put("FREE_HIGH_ADS", "Oferecer teste Premium para aliviar interrupções de áudio.");
        actions.put("HIGH_SKIP_RATE", "Recalibrar algoritmo de recomendação para reduzir pulos.");
        actions.put("LOW_ENGAGEMENT", "Enviar recomendações personalizadas para aumentar engajamento.");
        actions.put("PREMIUM_NO_OFFLINE", "Sugerir uso de downloads para experiência completa.");
        actions.put("HIGH_FRUSTRATION", "Enviar pesquisa de satisfação com cupom de desconto.");
        actions.put("STUDENT_GRADUATING", "Oferecer desconto no plano Premium pós-formatura.");
        actions.put("DEFAULT", "Monitorar comportamento e manter contato proativo.");
        return actions;
    }

    /**
     * Busca paginada com filtros dinâmicos.
     *
     * @param filter Filtros de busca (todos opcionais)
     * @param page Número da página (0-indexed)
     * @param size Tamanho da página (max 100)
     * @param sortBy Campo para ordenação
     * @param sortDir Direção da ordenação (asc/desc)
     * @return Resposta paginada com estatísticas
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<PredictionHistoryResponse> search(
            PredictionSearchFilter filter,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        // Validação e limitação de tamanho
        size = Math.min(size, 100); // Máximo 100 registros por página
        page = Math.max(page, 0);

        // Configuração de ordenação
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Executa query com specification (usa índices)
        Specification<PredictionHistoryEntity> spec = PredictionHistorySpecification.withFilters(filter);
        Page<PredictionHistoryEntity> resultPage = repository.findAll(spec, pageable);

        // Converte para DTOs de resposta
        List<PredictionHistoryResponse> content = resultPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        // Calcula estatísticas da página
        PaginatedResponse.PageStats stats = calculatePageStats(resultPage.getContent());

        log.debug("Busca paginada: page={}, size={}, totalElements={}, filters={}",
                page, size, resultPage.getTotalElements(), filter);

        return PaginatedResponse.<PredictionHistoryResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .first(resultPage.isFirst())
                .last(resultPage.isLast())
                .hasNext(resultPage.hasNext())
                .hasPrevious(resultPage.hasPrevious())
                .stats(stats)
                .build();
    }

    /**
     * Retorna estatísticas globais para os filtros do frontend.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getFilterOptions() {
        Map<String, Object> options = new HashMap<>();

        // Contagem por gênero
        List<Object[]> genderCounts = repository.countByGender();
        Map<String, Long> genders = new HashMap<>();
        for (Object[] row : genderCounts) {
            genders.put((String) row[0], (Long) row[1]);
        }
        options.put("genders", genders);

        // Contagem por tipo de assinatura
        List<Object[]> subscriptionCounts = repository.countBySubscriptionType();
        Map<String, Long> subscriptions = new HashMap<>();
        for (Object[] row : subscriptionCounts) {
            subscriptions.put((String) row[0], (Long) row[1]);
        }
        options.put("subscriptionTypes", subscriptions);

        // Contagem total
        options.put("totalRecords", repository.count());

        // Contagem por status
        options.put("willChurnCount", repository.countByChurnStatus(ChurnStatus.WILL_CHURN));
        options.put("willStayCount", repository.countByChurnStatus(ChurnStatus.WILL_STAY));

        return options;
    }

    /**
     * Retorna estatísticas globais para o dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getGlobalStatistics() {
        long total = repository.count();
        long churners = repository.count(PredictionHistorySpecification.withFilters(
                PredictionSearchFilter.builder().churnStatus(ChurnStatus.WILL_CHURN).build()));

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_predictions", total);
        stats.put("total_churners", churners);
        stats.put("churn_rate", total > 0 ? (double) churners / total : 0.0);
        return stats;
    }

    /**
     * Busca User IDs para autocomplete.
     */
    @Transactional(readOnly = true)
    public List<String> searchUserIds(String prefix) {
        if (prefix == null || prefix.length() < 2) {
            return List.of();
        }
        return repository.findUserIdsByPrefix(prefix, PageRequest.of(0, 10));
    }

    /**
     * Agrupa métricas necessárias para o dashboard de forma otimizada (DB-side aggregations).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAggregates() {
        Map<String, Object> out = new HashMap<>();

        // Buckets de probabilidade
        Object[] buckets = repository.getProbabilityBuckets();
        if (buckets != null && buckets.length == 4) {
            out.put("probabilityBuckets", new long[]{
                    ((Number) buckets[0]).longValue(),
                    ((Number) buckets[1]).longValue(),
                    ((Number) buckets[2]).longValue(),
                    ((Number) buckets[3]).longValue()
            });
        }

        // Risk factor counts
        Object[] risks = repository.getRiskFactorCounts();
        if (risks != null && risks.length == 5) {
            Map<String, Long> riskMap = new HashMap<>();
            riskMap.put("FREE_HIGH_ADS", ((Number) risks[0]).longValue());
            riskMap.put("HIGH_SKIP_RATE", ((Number) risks[1]).longValue());
            riskMap.put("HIGH_FRUSTRATION", ((Number) risks[2]).longValue());
            riskMap.put("PREMIUM_NO_OFFLINE", ((Number) risks[3]).longValue());
            riskMap.put("LOW_ENGAGEMENT", ((Number) risks[4]).longValue());
            out.put("riskFactorCounts", riskMap);
        }

        // Global stats
        Object[] global = repository.getGlobalStats();
        if (global != null && global.length >= 4) {
            long total = ((Number) global[0]).longValue();
            double avg = global[1] != null ? ((Number) global[1]).doubleValue() : 0.0;
            long churners = ((Number) global[2]).longValue();
            long stayers = ((Number) global[3]).longValue();
            out.put("total", total);
            out.put("averageProbability", avg);
            out.put("totalChurners", churners);
            out.put("totalStayers", stayers);
            out.put("churnRate", total > 0 ? (double) churners / total : 0.0);
            // Estimate revenue at risk
            out.put("revenueAtRisk", churners * 12.90);
        }

        return out;
    }

    // === MÉTODOS PRIVADOS ===

    private Sort createSort(String sortBy, String sortDir) {
        // Campos válidos para ordenação (whitelist para segurança)
        List<String> validSortFields = List.of(
                "createdAt", "probability", "age", "gender",
                "country", "subscriptionType", "churnStatus"
        );

        if (sortBy == null || !validSortFields.contains(sortBy)) {
            sortBy = "createdAt"; // Default
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }

    private PredictionHistoryResponse toResponse(PredictionHistoryEntity entity) {
        String primaryRisk = identifyPrimaryRiskFactor(entity);

        return PredictionHistoryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .gender(entity.getGender())
                .age(entity.getAge())
                .country(entity.getCountry())
                .subscriptionType(entity.getSubscriptionType())
                .deviceType(entity.getDeviceType())
                .churnStatus(entity.getChurnStatus())
                .probability(entity.getProbability())
                .predictionLabel(entity.getChurnStatus() == ChurnStatus.WILL_CHURN
                        ? "Vai Cancelar" : "Vai Continuar")
                .frustrationIndex(entity.getFrustrationIndex())
                .isHeavyUser(entity.getIsHeavyUser())
                .recommendedAction(determineRecommendedAction(entity))
                .primaryRiskFactor(primaryRisk)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String identifyPrimaryRiskFactor(PredictionHistoryEntity entity) {
        if (entity.getChurnStatus() == ChurnStatus.WILL_STAY) {
            return "Nenhum (Stay)";
        }

        if ("Free".equalsIgnoreCase(entity.getSubscriptionType()) && entity.getAdsListenedPerWeek() > 15) {
            return "Anúncios por Semana";
        }
        if (entity.getSkipRate() > 0.4) return "Taxa de Pulos Elevada";
        if (entity.getFrustrationIndex() != null && entity.getFrustrationIndex() > 3.0) return "Índice de Frustração";
        if (Boolean.TRUE.equals(entity.getPremiumNoOffline())) return "Subutilização Premium";
        if (entity.getListeningTime() < 100) return "Baixo Engajamento";

        return "Perfil de Risco Moderado";
    }

    private String determineRecommendedAction(PredictionHistoryEntity entity) {
        // Lógica para determinar ação recomendada baseada no perfil
        if (entity.getChurnStatus() == ChurnStatus.WILL_STAY) {
            return "Manter relacionamento atual.";
        }

        // Análise do perfil para determinar ação
        if ("Free".equalsIgnoreCase(entity.getSubscriptionType()) &&
                entity.getAdsListenedPerWeek() > 15) {
            return RECOMMENDED_ACTIONS.get("FREE_HIGH_ADS");
        }

        if (entity.getSkipRate() > 0.4) {
            return RECOMMENDED_ACTIONS.get("HIGH_SKIP_RATE");
        }

        if (entity.getFrustrationIndex() != null && entity.getFrustrationIndex() > 3.0) {
            return RECOMMENDED_ACTIONS.get("HIGH_FRUSTRATION");
        }

        if (Boolean.TRUE.equals(entity.getPremiumNoOffline())) {
            return RECOMMENDED_ACTIONS.get("PREMIUM_NO_OFFLINE");
        }

        if (entity.getListeningTime() < 100) {
            return RECOMMENDED_ACTIONS.get("LOW_ENGAGEMENT");
        }

        return RECOMMENDED_ACTIONS.get("DEFAULT");
    }

    private PaginatedResponse.PageStats calculatePageStats(List<PredictionHistoryEntity> content) {
        if (content.isEmpty()) {
            return PaginatedResponse.PageStats.builder()
                    .willChurnCount(0)
                    .willStayCount(0)
                    .avgProbability(0.0)
                    .build();
        }

        long willChurn = content.stream()
                .filter(e -> e.getChurnStatus() == ChurnStatus.WILL_CHURN)
                .count();

        long willStay = content.size() - willChurn;

        double avgProb = content.stream()
                .mapToDouble(PredictionHistoryEntity::getProbability)
                .average()
                .orElse(0.0);

        return PaginatedResponse.PageStats.builder()
                .willChurnCount(willChurn)
                .willStayCount(willStay)
                .avgProbability(Math.round(avgProb * 1000.0) / 1000.0)
                .build();
    }
}

