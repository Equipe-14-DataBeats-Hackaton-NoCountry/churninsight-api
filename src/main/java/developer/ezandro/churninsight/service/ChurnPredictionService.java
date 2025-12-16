package developer.ezandro.churninsight.service;

import developer.ezandro.churninsight.infra.api.dto.ChurnRequest;
import developer.ezandro.churninsight.infra.api.dto.ChurnResponse;

public interface ChurnPredictionService {

    ChurnResponse predict(ChurnRequest request);

}
