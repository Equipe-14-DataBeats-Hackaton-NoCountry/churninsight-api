package com.hackathon.databeats.churninsight.infra.adapter.input.web.mapper;

import java.util.Map;

public final class JsonFieldNameMapper {
    private JsonFieldNameMapper() {}

    // Mapa estático de campos JSON para mensagens amigáveis em PT-BR
    public static final Map<String, String> FIELD_MAP = Map.of(
            "contractDurationMonths", "tempo_contrato_meses",
            "paymentDelays", "atrasos_pagamento",
            "monthlyUsage", "uso_mensal",
            "plan", "plano",
            "skipRate", "taxa_pulos",
            "listeningTime", "tempo_audicao"
    );

    public static String getJsonFieldName(String javaField) {
        return FIELD_MAP.getOrDefault(javaField, javaField);
    }
}