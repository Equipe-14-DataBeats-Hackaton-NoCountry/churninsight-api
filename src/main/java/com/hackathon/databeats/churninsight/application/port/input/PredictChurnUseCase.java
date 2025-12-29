package com.hackathon.databeats.churninsight.application.port.input;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;

public interface PredictChurnUseCase {
    void predict(CustomerProfile profile, String requesterId, String requestIp);
}