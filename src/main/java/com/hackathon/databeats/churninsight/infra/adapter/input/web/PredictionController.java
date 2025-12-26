package com.hackathon.databeats.churninsight.infra.adapter.input.web;

import com.hackathon.databeats.churninsight.application.port.input.PredictChurnUseCase;
import com.hackathon.databeats.churninsight.application.service.ChurnPredictionService;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.ChurnRequest;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.ChurnResponse;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.mapper.WebMapper;
import com.hackathon.databeats.churninsight.infra.config.MetricsConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PredictionController {

    private final PredictChurnUseCase useCase;
    @SuppressWarnings("unused") // Mantido para futura integração com o fluxo real
    private final ChurnPredictionService churnPredictionService;
    private final WebMapper mapper;
    private final MetricsConfig metrics;

    @PostMapping("/predict")
    public ResponseEntity<ChurnResponse> predict(@Valid @RequestBody ChurnRequest request) {
        long start = System.nanoTime();
        metrics.incrementActiveRequests();

        try {
            var profile = mapper.toDomain(request);

            var result = useCase.execute(profile);
            metrics.recordPrediction();

            return ResponseEntity.ok(mapper.toResponse(result));

        } catch (Exception e) {
            metrics.recordError();
            throw e;
        } finally {
            metrics.decrementActiveRequests();
            metrics.recordLatency(System.nanoTime() - start);
        }
    }
}