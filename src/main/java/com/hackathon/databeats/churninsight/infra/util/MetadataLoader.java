package com.hackathon.databeats.churninsight.infra.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.databeats.churninsight.infra.exception.MetadataLoadException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class MetadataLoader {
    private MetadataLoader() {
    }

    public static ModelMetadata load(String resourcePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource(resourcePath).getInputStream();
            return mapper.readValue(inputStream, ModelMetadata.class);
        } catch (IOException e) {
            throw new MetadataLoadException("Erro ao carregar metadata do recurso: " + resourcePath);
        }
    }
}