package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.port.input.PredictChurnUseCase;
import com.hackathon.databeats.churninsight.application.port.input.PredictionStatsUseCase;
import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.domain.exception.PredictionException;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.PredictionStatsResponse;
import com.hackathon.databeats.churninsight.infra.adapter.output.inference.OnnxRuntimeAdapter;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryEntity;

import java.util.Map;
import java.util.UUID;

public class ChurnPredictionService implements PredictChurnUseCase, PredictionStatsUseCase {
    private final SaveHistoryPort saveHistoryPort;
    private final OnnxRuntimeAdapter onnxAdapter;

    public ChurnPredictionService(SaveHistoryPort saveHistoryPort, OnnxRuntimeAdapter onnxAdapter) {
        this.saveHistoryPort = saveHistoryPort;
        this.onnxAdapter = onnxAdapter;
    }

    @Override
    public void predict(CustomerProfile profile, String requesterId, String requestIp) {
        try {
            float[] prediction = this.onnxAdapter.predict(profile);

            double churnProbability = prediction[0];
            boolean willChurn = churnProbability > 0.5;

            PredictionHistoryEntity history = new PredictionHistoryEntity();
            history.setId(UUID.randomUUID().toString());
            history.setGender(profile.getGender());
            history.setAge(profile.getAge());
            history.setCountry(profile.getCountry());
            history.setSubscriptionType(profile.getSubscriptionType());
            history.setListeningTime(profile.getListeningTime());
            history.setSongsPlayedPerDay(profile.getSongsPlayedPerDay() != null ? profile.getSongsPlayedPerDay() : 0);
            history.setAdsListenedPerWeek(profile.getAdsListenedPerWeek() != null ? profile.getAdsListenedPerWeek() : 0);
            history.setSkipRate(profile.getSkipRate());
            history.setDeviceType(profile.getDeviceType());
            history.setOfflineListening(profile.getOfflineListening() != null && profile.getOfflineListening());
            history.setUserId(profile.getUserId());

            history.setProbability(churnProbability);
            history.setChurnStatus(willChurn ? ChurnStatus.WILL_CHURN : ChurnStatus.WILL_STAY);

            history.setRequesterId(requesterId);
            history.setRequestIp(requestIp);

            this.saveHistoryPort.save(history);

        } catch (Exception e) {
            throw new PredictionException("Erro ao executar predição com modelo ONNX.");
        }
    }

    @Override
    public PredictionStatsResponse predictWithStats(CustomerProfile profile, String requesterId, String requestIp) {
        try {
            float[] prediction = this.onnxAdapter.predict(profile);

            double churnProbability = prediction[0];
            boolean willChurn = churnProbability > 0.5;

            Map<String, Float> classProbs = Map.of(
                    ChurnStatus.WILL_CHURN.name(), prediction[0],
                    ChurnStatus.WILL_STAY.name(), prediction.length > 1 ? prediction[1] : 1 - prediction[0]
            );

            return PredictionStatsResponse.builder()
                    .label(willChurn ? ChurnStatus.WILL_CHURN : ChurnStatus.WILL_STAY)
                    .probability(churnProbability)
                    .probabilities(prediction)
                    .classProbabilities(classProbs)
                    .build();

        } catch (Exception e) {
            throw new PredictionException("Erro ao executar predição com modelo ONNX.");
        }
    }
}