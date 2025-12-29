package com.hackathon.databeats.churninsight.infra.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.application.service.ChurnPredictionService;
import com.hackathon.databeats.churninsight.infra.adapter.output.inference.OnnxRuntimeAdapter;
import com.hackathon.databeats.churninsight.infra.util.ModelMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class BeanConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(BeanConfiguration.class);

    @Bean
    public ModelMetadata modelMetadata() throws IOException {
        try (InputStream is = new ClassPathResource("metadata.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, ModelMetadata.class);
        }
    }

    @Bean
    public OnnxRuntimeAdapter onnxRuntimeAdapter(ModelMetadata metadata) throws Exception {
        try (InputStream modelStream = new ClassPathResource("modelo_hackathon.onnx").getInputStream()) {
            return new OnnxRuntimeAdapter(modelStream, metadata);
        }
    }

    @Bean
    public ChurnPredictionService churnPredictionService(SaveHistoryPort saveHistoryPort,
                                                         OnnxRuntimeAdapter onnxAdapter) {
        return new ChurnPredictionService(saveHistoryPort, onnxAdapter);
    }
}