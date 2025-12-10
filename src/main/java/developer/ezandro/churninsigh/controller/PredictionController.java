package developer.ezandro.churninsigh.controller;

import developer.ezandro.churninsigh.dto.PredictionRequest;
import developer.ezandro.churninsigh.dto.PredictionResponse;
import developer.ezandro.churninsigh.service.ChurnPredictionService;
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
    public ResponseEntity<PredictionResponse> predict(@Valid @RequestBody PredictionRequest request) {
        PredictionResponse response = this.service.predict(request);
        return ResponseEntity.ok(response);
    }
}