package developer.ezandro.churninsight.service;

import developer.ezandro.churninsight.infra.api.dto.StatsResponse;
import org.springframework.stereotype.Service;

@Service
public class StatsService {
    private long totalEvaluated = 0;
    private long totalChurn = 0;

    public void registerPrediction(boolean isChurn) {
        this.totalEvaluated++;
        if (isChurn) {
            this.totalChurn++;
        }
    }

    public StatsResponse getStats() {
        double churnRate = this.totalEvaluated == 0
                ? 0.0
                : (double) this.totalChurn / this.totalEvaluated;

        churnRate = Math.round(churnRate * 100.0) / 100.0;

        return new StatsResponse(this.totalEvaluated, churnRate);
    }
}