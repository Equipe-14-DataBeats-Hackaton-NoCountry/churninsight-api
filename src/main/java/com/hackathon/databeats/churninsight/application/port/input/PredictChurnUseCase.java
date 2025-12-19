package com.hackathon.databeats.churninsight.application.port.input;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.domain.model.PredictionResult;

public interface PredictChurnUseCase {
    PredictionResult execute(CustomerProfile profile);
}