package com.hackathon.databeats.churninsight.infra.config;

import com.hackathon.databeats.churninsight.application.port.input.GetStatsUseCase;
import com.hackathon.databeats.churninsight.application.port.output.LoadModelPort;
import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.application.service.ChurnPredictionService;
import com.hackathon.databeats.churninsight.application.service.StatsService;
import com.hackathon.databeats.churninsight.domain.rules.RiskEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RiskEvaluator riskEvaluator(MetadataConfig config) {
        return new RiskEvaluator(config);
    }

    @Bean
    public ChurnPredictionService churnPredictionService(
            LoadModelPort modelPort,
            SaveHistoryPort historyPort,
            RiskEvaluator riskEvaluator
    ) {
        return new ChurnPredictionService(modelPort, historyPort, riskEvaluator);
    }

    @Bean
    public GetStatsUseCase getStatsUseCase(SaveHistoryPort historyPort) {
        return new StatsService(historyPort);
    }
}