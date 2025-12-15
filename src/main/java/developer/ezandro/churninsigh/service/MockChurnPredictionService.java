package developer.ezandro.churninsigh.service;

import developer.ezandro.churninsigh.dto.PredictionRequest;
import developer.ezandro.churninsigh.dto.PredictionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Service
public class MockChurnPredictionService implements ChurnPredictionService {
    private final StatsService statsService;

    @Override
    public PredictionResponse predict(PredictionRequest r) {
        boolean highRisk = r.paymentDelays() >= 2 || r.contractDurationMonths() < 6 || r.monthlyUsage() < 10.0;

        double probability;
        String prediction;

        if (highRisk) {
            probability = 0.70 + Math.min(0.20, r.paymentDelays() * 0.05) + randomBetween(0.05);
            probability = Math.min(probability, 0.95);
            prediction = "Vai cancelar";
        } else {
            probability = 0.10 + this.randomBetween(0.30) - Math.min(0.25, r.contractDurationMonths() / 100.0);
            probability = Math.max(probability, 0.01);
            prediction = "Vai continuar";
        }

        probability = Math.round(probability * 100.0) / 100.0;

        boolean isChurn = prediction.equals("Vai cancelar");
        this.statsService.registerPrediction(isChurn);

        return new PredictionResponse(prediction, probability);
    }

    private double randomBetween(double max) {
        return ThreadLocalRandom.current().nextDouble(max);
    }
}