package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.port.input.PredictChurnUseCase;
import com.hackathon.databeats.churninsight.application.port.output.LoadModelPort;
import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.domain.model.PredictionResult;
import com.hackathon.databeats.churninsight.domain.rules.RiskEvaluator;

import java.util.UUID;

public class ChurnPredictionService implements PredictChurnUseCase {

    private final LoadModelPort modelPort;
    private final SaveHistoryPort historyPort;
    private final RiskEvaluator riskEvaluator;

    public ChurnPredictionService(LoadModelPort modelPort,
                                  SaveHistoryPort historyPort,
                                  RiskEvaluator riskEvaluator) {
        this.modelPort = modelPort;
        this.historyPort = historyPort;
        this.riskEvaluator = riskEvaluator;
    }

    @Override
    public PredictionResult execute(CustomerProfile profile) {
        boolean highRisk = riskEvaluator.isHighRisk(profile);

        double probability = modelPort.calculateProbability(profile, highRisk);

        String status = highRisk ? "Vai cancelar" : "Vai continuar"; // Simplificado da lógica original

        probability = Math.round(probability * 100.0) / 100.0;

        var result = new PredictionResult(UUID.randomUUID(), status, probability);

        historyPort.savePrediction(profile, result);

        return result;
    }
}