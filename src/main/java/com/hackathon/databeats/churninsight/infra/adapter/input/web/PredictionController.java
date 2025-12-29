package com.hackathon.databeats.churninsight.infra.adapter.input.web;

import com.hackathon.databeats.churninsight.application.port.input.PredictChurnUseCase;
import com.hackathon.databeats.churninsight.application.port.input.PredictionStatsUseCase;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.PredictionStatsResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PredictionController {
    private final PredictionStatsUseCase predictionStatsUseCase;
    private final PredictChurnUseCase predictChurnUseCase;

    public PredictionController(PredictionStatsUseCase predictionStatsUseCase,
                                PredictChurnUseCase predictChurnUseCase) {
        this.predictionStatsUseCase = predictionStatsUseCase;
        this.predictChurnUseCase = predictChurnUseCase;
    }

    @PostMapping(value = "/predict")
    public Map<String, Object> predict(@Valid @RequestBody CustomerProfile profile, HttpServletRequest request) {
        String requesterId = "hackathon-user";
        String requestIp = request.getRemoteAddr();

        this.predictChurnUseCase.predict(profile, requesterId, requestIp);

        PredictionStatsResponse stats = this.predictionStatsUseCase.predictWithStats(profile, requesterId, requestIp);

        Map<String, Float> classProbs = stats.getClassProbabilities();
        String label = classProbs.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");

        double probability = classProbs.getOrDefault(label, 0f);

        return Map.of("label", label, "probability", probability);
    }

    @PostMapping(value = "/stats")
    public PredictionStatsResponse stats(@RequestBody CustomerProfile profile, HttpServletRequest request) {
        String requesterId = "hackathon-user";
        String requestIp = request.getRemoteAddr();

        return this.predictionStatsUseCase.predictWithStats(profile, requesterId, requestIp);
    }
}