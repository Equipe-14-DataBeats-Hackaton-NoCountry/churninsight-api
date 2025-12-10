package developer.ezandro.churninsigh.service;

import developer.ezandro.churninsigh.dto.PredictionRequest;
import developer.ezandro.churninsigh.dto.PredictionResponse;

public interface ChurnPredictionService {
    PredictionResponse predict(PredictionRequest request);
}