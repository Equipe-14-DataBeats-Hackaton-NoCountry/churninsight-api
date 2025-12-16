package developer.ezandro.churninsight.controller;

import developer.ezandro.churninsight.infra.api.dto.ChurnRequest;
import developer.ezandro.churninsight.infra.api.dto.ChurnResponse;
import developer.ezandro.churninsight.service.ChurnPredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PredictionController {
    private final ChurnPredictionService service;

    @PostMapping(value = "/predict")
    public ResponseEntity<ChurnResponse> predict(@Valid @RequestBody ChurnRequest request
    ) {
        ChurnResponse response = service.predict(request);
        return ResponseEntity.ok(response);
    }
}