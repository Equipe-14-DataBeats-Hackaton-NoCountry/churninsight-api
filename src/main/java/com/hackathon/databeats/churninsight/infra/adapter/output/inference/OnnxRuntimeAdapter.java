package com.hackathon.databeats.churninsight.infra.adapter.output.inference;

import ai.onnxruntime.*;
import com.hackathon.databeats.churninsight.application.port.output.LoadModelPort;
import com.hackathon.databeats.churninsight.infra.exception.ModelInferenceException;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.infra.util.ModelMetadata;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OnnxRuntimeAdapter implements LoadModelPort {
    private final OrtEnvironment env;
    private final OrtSession session;
    private final ModelMetadata metadata;

    public OnnxRuntimeAdapter(InputStream modelStream, ModelMetadata metadata) throws OrtException, IOException {
        this.env = OrtEnvironment.getEnvironment();
        this.metadata = metadata;

        byte[] modelBytes = modelStream.readAllBytes();
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        this.session = env.createSession(modelBytes, options);
    }

    public float[] predict(CustomerProfile profile) throws OrtException {
        Map<String, OnnxTensor> inputs = new HashMap<>();

        for (String feature : this.metadata.getNumericFeatures()) {
            float value = switch (feature) {
                case "age" -> safeFloat(profile.getAge());
                case "listening_time" -> (float) safeDouble(profile.getListeningTime());
                case "songs_played_per_day" -> safeInt(profile.getSongsPlayedPerDay());
                case "skip_rate" -> (float) safeDouble(profile.getSkipRate());
                case "ads_listened_per_week" -> safeInt(profile.getAdsListenedPerWeek());
                default -> 0f;
            };
            inputs.put(feature, OnnxTensor.createTensor(env, new float[][]{{value}}));
        }

        for (String feature : this.metadata.getCategoricalFeatures()) {
            String value = switch (feature) {
                case "gender" -> profile.getGender();
                case "country" -> profile.getCountry();
                case "subscription_type" -> profile.getSubscriptionType();
                case "device_type" -> profile.getDeviceType();
                default -> "";
            };
            inputs.put(feature, OnnxTensor.createTensor(env, new String[][]{{value}}));
        }

        try (OrtSession.Result result = session.run(inputs)) {
            OnnxValue probOutput = result.get("output_probability")
                    .orElseThrow(() -> new ModelInferenceException("Saída 'output_probability' não encontrada"));
            Object probValue = probOutput.getValue();

            return switch (probValue) {
                case float[] floatArray -> floatArray;
                case float[][] floatArrayArray -> floatArrayArray[0];
                case List<?> list -> listToFloatArray(list);
                case OnnxMap onnxMap -> {
                    Object rawMap = onnxMap.getValue();
                    if (rawMap instanceof Map<?, ?> map) {
                        yield mapToFloatArray(map);
                    }
                    throw new ModelInferenceException("OnnxMap retornou tipo inesperado: " + "null");
                }
                case OnnxSequence seq -> {
                    Object rawList = seq.getValue();
                    if (rawList instanceof List<?> list) {
                        yield listToFloatArray(list);
                    }
                    throw new ModelInferenceException("OnnxSequence retornou tipo inesperado: " + "null");
                }
                default -> throw new ModelInferenceException("Formato inesperado: " + probValue.getClass().getName());
            };
        }
    }

    private float[] listToFloatArray(List<?> list) throws OrtException {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = toFloat(list.get(i));
        }
        return arr;
    }

    private float[] mapToFloatArray(Map<?, ?> map) throws OrtException {
        float[] arr = new float[map.size()];
        int i = 0;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            arr[i++] = toFloat(e.getValue());
        }
        return arr;
    }

    private float toFloat(Object val) throws OrtException {
        if (val == null) {
            throw new ModelInferenceException("Valor inesperado: null");
        }

        return switch (val) {
            case Number num -> num.floatValue();
            case Map<?, ?> map -> {
                if (!map.isEmpty()) {
                    Object firstVal = map.values().iterator().next();
                    yield toFloat(firstVal);
                }
                throw new ModelInferenceException("Mapa vazio retornado pelo modelo.");
            }
            case OnnxValue ov -> toFloat(ov.getValue());
            case List<?> list -> {
                if (!list.isEmpty()) {
                    yield toFloat(list.getFirst());
                }
                throw new ModelInferenceException("Lista vazia retornada pelo modelo.");
            }
            case float[] fa -> {
                if (fa.length > 0) {
                    yield fa[0];
                }
                throw new ModelInferenceException("Array vazio retornado pelo modelo.");
            }
            default -> throw new ModelInferenceException("Valor inesperado: " + val.getClass().getName());
        };
    }

    private float safeFloat(Integer value) { return value == null ? 0f : value.floatValue(); }
    private double safeDouble(Double value) { return value == null ? 0d : value; }
    private float safeInt(Integer value) { return value == null ? 0f : value.floatValue(); }

    @Override
    public boolean isModelLoaded() {
        return session != null;
    }
}
