package com.hackathon.databeats.churninsight.infra.adapter.input.web.controller;

import com.hackathon.databeats.churninsight.application.dto.PredictionResult;
import com.hackathon.databeats.churninsight.application.port.input.BatchProcessingUseCase;
import com.hackathon.databeats.churninsight.application.port.input.PredictionStatsUseCase;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.domain.rules.ChurnDiagnosisService;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.CustomerProfileRequest;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.PredictionStatsResponse;
import com.hackathon.databeats.churninsight.infra.exception.ApiErrorResponse;
import com.hackathon.databeats.churninsight.infra.util.ModelMetadata;
import com.hackathon.databeats.churninsight.infra.util.NetworkUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller REST para endpoints de predição de churn.
 *
 * <p>Expõe os endpoints da API para:</p>
 * <ul>
 *   <li>Predição individual com diagnóstico de IA</li>
 *   <li>Retorno de estatísticas completas (probabilidades por classe)</li>
 *   <li>Processamento assíncrono em lote (CSV/XLSX)</li>
 *   <li>Consulta de status de jobs em lote</li>
 *   <li>Health check e gestão de cache</li>
 * </ul>
 *
 * <p><b>Implementa cache em camada HTTP:</b> Predições são cacheadas para otimizar
 * performance em requisições repetidas (mesmo perfil de cliente).</p>
 *
 * <p><b>Segurança:</b> Endpoints de cache requerem role ADMIN. Todas as requisições
 * são rastreadas com IP do cliente para auditoria.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Predição de Churn", description = "Endpoints para predição de cancelamento de clientes")
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class PredictionController {

    private final PredictionStatsUseCase predictionStatsUseCase;
    private final BatchProcessingUseCase batchProcessingUseCase;
    private final ModelMetadata modelMetadata;

    /** Tamanho máximo permitido para upload de arquivos (200MB). */
    private static final long TAMANHO_MAXIMO_ARQUIVO = 200 * 1024 * 1024L;

    /** Estimativa de tempo de processamento por MB em minutos. */
    private static final double TEMPO_POR_MB = 0.5;

    /**
     * Realiza predição de churn para um único cliente.
     *
     * <p>O resultado inclui a previsão (Vai Cancelar/Vai Continuar), probabilidade,
     * threshold de decisão e diagnóstico de IA com fatores de risco e retenção.</p>
     *
     * @param request dados do cliente para predição
     * @param httpRequest requisição HTTP para obtenção do IP
     * @return mapa com resultado da predição e diagnóstico
     */
    @PostMapping(value = "/predict")
    @Operation(
            summary = "Predição individual de churn",
            description = "Analisa o perfil de um cliente e retorna a probabilidade de cancelamento com diagnóstico detalhado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Predição realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @Cacheable(value = "predictions", key = "#request.toString()", unless = "#result.containsKey('error')")
    public Map<String, Object> predict(
            @Valid @RequestBody CustomerProfileRequest request,
            HttpServletRequest httpRequest) {

        long inicio = System.currentTimeMillis();
        String requesterId = "hackathon-user";
        String requestIp = NetworkUtils.getClientIp(httpRequest);

        CustomerProfile profile = request.toDomain();
        log.debug("Iniciando predição - UserId: {}, IP: {}", profile.userId(), requestIp);

        PredictionResult resultado = predictionStatsUseCase.predictWithStats(profile, requesterId, requestIp);
        Map<String, Object> resposta = construirResposta(resultado, profile);

        long duracao = System.currentTimeMillis() - inicio;
        log.info("Predição concluída em {}ms - UserId: {}, Status: {}",
                duracao, profile.userId(), resultado.label());

        return resposta;
    }

    /**
     * Retorna estatísticas detalhadas da predição.
     *
     * @param request dados do cliente para predição
     * @param httpRequest requisição HTTP
     * @return estatísticas completas incluindo probabilidades por classe
     */
    @PostMapping(value = "/stats")
    @Operation(
            summary = "Estatísticas completas de predição",
            description = "Retorna predição com probabilidades detalhadas para cada classe"
    )
    @Cacheable(value = "predictionStats", key = "#request.toString() + '_' + #httpRequest.remoteAddr")
    public PredictionStatsResponse stats(
            @Valid @RequestBody CustomerProfileRequest request,
            HttpServletRequest httpRequest) {

        String requesterId = "hackathon-user";
        String requestIp = NetworkUtils.getClientIp(httpRequest);
        CustomerProfile profile = request.toDomain();

        log.debug("Solicitação de estatísticas - UserId: {}, IP: {}", profile.userId(), requestIp);
        PredictionResult resultado = predictionStatsUseCase.predictWithStats(profile, requesterId, requestIp);

        return PredictionStatsResponse.builder()
                .label(resultado.label())
                .probability(resultado.probability())
                .probabilities(resultado.probabilities())
                .classProbabilities(resultado.classProbabilities())
                .build();
    }

    /**
     * Inicia processamento em lote de predições a partir de arquivo CSV ou XLSX.
     *
     * <p>O processamento é executado de forma assíncrona. Use o endpoint de status
     * para acompanhar o progresso do job.</p>
     *
     * @param file arquivo CSV ou XLSX com perfis de clientes
     * @param httpRequest requisição HTTP
     * @return informações do job iniciado incluindo ID para consulta de status
     */
    @PostMapping(value = "/predict/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Processamento em lote",
            description = "Processa arquivo CSV/XLSX com múltiplos perfis. Suporta até 1M de registros."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Processamento iniciado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou muito grande"),
            @ApiResponse(responseCode = "500", description = "Erro ao iniciar processamento")
    })
    public ResponseEntity<Map<String, Object>> predictBatch(
            @Parameter(description = "Arquivo CSV ou XLSX com perfis de clientes")
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {

        String requestIp = NetworkUtils.getClientIp(httpRequest);
        String nomeArquivo = file.getOriginalFilename();

        log.info("Iniciando processamento em lote - Arquivo: {}, Tamanho: {} bytes, IP: {}",
                nomeArquivo, file.getSize(), requestIp);

        ResponseEntity<Map<String, Object>> validacao = validarArquivo(file, nomeArquivo);
        if (validacao != null) {
            return validacao;
        }

        try {
            String jobId = batchProcessingUseCase.startBatchProcessing(file, requestIp);

            return ResponseEntity.accepted().body(Map.of(
                    "message", "Processamento iniciado com sucesso",
                    "job_id", jobId,
                    "filename", nomeArquivo != null ? nomeArquivo : "arquivo_sem_nome",
                    "size_mb", Math.round(file.getSize() / (1024.0 * 1024.0) * 100) / 100.0,
                    "estimated_time_minutes", estimarTempoProcessamento(file.getSize()),
                    "status_url", "/predict/batch/status/" + jobId,
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("Erro ao iniciar processamento em lote - IP: {}, Erro: {}", requestIp, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erro ao processar arquivo",
                    "message", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * Consulta o status de um job de processamento em lote.
     *
     * @param jobId identificador único do job
     * @return status atual do processamento incluindo progresso e métricas
     */
    @GetMapping(value = "/predict/batch/status/{jobId}")
    @Operation(
            summary = "Status do processamento em lote",
            description = "Retorna o progresso e métricas de um job de processamento"
    )
    public ResponseEntity<Map<String, Object>> getBatchStatus(
            @Parameter(description = "ID do job retornado pelo endpoint de batch")
            @PathVariable String jobId) {

        try {
            Map<String, Object> status = batchProcessingUseCase.getJobStatus(jobId);

            if (status.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Erro ao consultar status do job {} - Erro: {}", jobId, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erro ao verificar status",
                    "job_id", jobId,
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * Verifica a saúde da API e do modelo de ML.
     *
     * @return status de saúde incluindo informações do modelo e cache
     */
    @GetMapping(value = "/health")
    @Operation(summary = "Health check", description = "Verifica disponibilidade da API e modelo")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            boolean modeloSaudavel = batchProcessingUseCase.isModelHealthy();

            Map<String, Object> health = Map.of(
                    "status", modeloSaudavel ? "UP" : "DEGRADED",
                    "timestamp", System.currentTimeMillis(),
                    "version", "1.0.0",
                    "model_status", modeloSaudavel ? "LOADED" : "ERROR",
                    "cache_stats", obterEstatisticasCache()
            );

            return modeloSaudavel
                    ? ResponseEntity.ok(health)
                    : ResponseEntity.status(503).body(health);

        } catch (Exception e) {
            log.error("Erro no health check: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                    "status", "DOWN",
                    "error", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * Limpa o cache de predições.
     *
     * <p>Requer role ADMIN. Útil para forçar recálculo de predições após
     * atualização do modelo.</p>
     *
     * @return confirmação da limpeza do cache
     */
    @PostMapping(value = "/cache/clear")
    @Operation(
            summary = "Limpar cache",
            description = "Remove todas as predições em cache. Requer permissão de administrador."
    )
    public ResponseEntity<Map<String, String>> clearCache() {
        try {
            batchProcessingUseCase.clearPredictionCache();
            log.info("Cache de predições limpo com sucesso");

            return ResponseEntity.ok(Map.of(
                    "message", "Cache limpo com sucesso",
                    "timestamp", String.valueOf(System.currentTimeMillis())
            ));

        } catch (Exception e) {
            log.error("Erro ao limpar cache: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Erro ao limpar cache",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Constrói a resposta da predição conforme contrato da API.
     */
    private Map<String, Object> construirResposta(PredictionResult resultado, CustomerProfile profile) {
        String previsao = resultado.label() == ChurnStatus.WILL_CHURN
                ? "Vai Cancelar"
                : "Vai Continuar";

        Map<String, String> diagnostico = ChurnDiagnosisService.gerarDiagnostico(
                profile, resultado.probability(), modelMetadata.getThresholdOtimo());

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("prediction", previsao);
        resposta.put("probability", Math.round(resultado.probability() * 10000) / 10000.0);
        resposta.put("decision_threshold", modelMetadata.getThresholdOtimo());
        resposta.put("ai_diagnosis", diagnostico);

        return resposta;
    }

    /**
     * Valida arquivo de upload verificando se está vazio, formato e tamanho.
     */
    private ResponseEntity<Map<String, Object>> validarArquivo(MultipartFile file, String nomeArquivo) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Arquivo não pode estar vazio",
                    "timestamp", System.currentTimeMillis()
            ));
        }

        if (nomeArquivo == null ||
                (!nomeArquivo.toLowerCase().endsWith(".csv") &&
                        !nomeArquivo.toLowerCase().endsWith(".xlsx"))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Apenas arquivos CSV ou XLSX são suportados",
                    "supported_formats", "CSV, XLSX",
                    "timestamp", System.currentTimeMillis()
            ));
        }

        if (file.getSize() > TAMANHO_MAXIMO_ARQUIVO) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Arquivo muito grande",
                    "max_size_mb", TAMANHO_MAXIMO_ARQUIVO / (1024 * 1024),
                    "current_size_mb", file.getSize() / (1024 * 1024),
                    "timestamp", System.currentTimeMillis()
            ));
        }

        return null;
    }

    /**
     * Estima o tempo de processamento baseado no tamanho do arquivo.
     */
    private int estimarTempoProcessamento(long tamanhoBytesArquivo) {
        long tamanhoMB = tamanhoBytesArquivo / (1024 * 1024);
        return Math.max(1, (int) (tamanhoMB * TEMPO_POR_MB));
    }

    /**
     * Obtém estatísticas do cache para monitoramento.
     */
    private Map<String, Object> obterEstatisticasCache() {
        try {
            return batchProcessingUseCase.getCacheStatistics();
        } catch (Exception e) {
            log.warn("Erro ao obter estatísticas do cache: {}", e.getMessage());
            return Map.of("error", "Estatísticas indisponíveis");
        }
    }

    /**
     * Handler para requisições com Content-Type não suportado.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        log.warn("Content-Type não suportado: {}", ex.getContentType());

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "Tipo de conteúdo não suportado. Para upload, utilize 'multipart/form-data'.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }
}