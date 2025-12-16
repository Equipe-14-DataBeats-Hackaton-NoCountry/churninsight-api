package developer.ezandro.churninsight.service;

import developer.ezandro.churninsight.infra.api.dto.ChurnRequest;
import developer.ezandro.churninsight.infra.api.dto.ChurnResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Service
public class MockChurnPredictionService implements ChurnPredictionService {

    private final StatsService statsService;

    @Override
    public ChurnResponse predict(ChurnRequest r) {

        boolean highRisk =
                r.skipRate() >= 2.0 ||
                        r.listeningTime() < 6.0 ||
                        r.songsPlayedPerDay() < 10;

        double probability;
        String prediction;

        if (highRisk) {
            probability =
                    0.70
                            + Math.min(0.20, r.skipRate() * 0.05)
                            + randomBetween(0.05);

            probability = Math.min(probability, 0.95);
            prediction = "Vai cancelar";

        } else {
            probability =
                    0.10
                            + randomBetween(0.30)
                            - Math.min(0.25, r.listeningTime() / 100.0);

            probability = Math.max(probability, 0.01);
            prediction = "Vai continuar";
        }

        probability = Math.round(probability * 100.0) / 100.0;

        boolean isChurn = prediction.equals("Vai cancelar");
        statsService.registerPrediction(isChurn);

        return new ChurnResponse(
                java.util.UUID.randomUUID(),
                prediction,
                probability
        );
    }

    private double randomBetween(double max) {
        return ThreadLocalRandom.current().nextDouble(max);
    }
}
