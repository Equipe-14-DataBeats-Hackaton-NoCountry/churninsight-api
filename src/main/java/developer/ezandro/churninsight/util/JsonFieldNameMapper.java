package developer.ezandro.churninsigh.util;

import java.util.Map;

public final class JsonFieldNameMapper {
    private JsonFieldNameMapper() {}

    public static final Map<String, String> FIELD_MAP = Map.of(
            "contractDurationMonths", "tempo_contrato_meses",
            "paymentDelays", "atrasos_pagamento",
            "monthlyUsage", "uso_mensal",
            "plan", "plano"
    );

    public static String getJsonFieldName(String javaField) {
        return FIELD_MAP.getOrDefault(javaField, javaField);
    }
}