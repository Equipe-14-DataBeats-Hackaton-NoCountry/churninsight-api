package com.hackathon.databeats.churninsight.infra.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsConfig {

    private final MeterRegistry registry;

    private final AtomicLong activeRequests = new AtomicLong(0);

    private final Counter predictionsCounter;
    private final Counter batchItemsCounter;
    private final Counter errorsCounter;

    private final Timer predictionTimer;

    public MetricsConfig(MeterRegistry registry) {
        this.registry = registry;

        this.predictionsCounter = Counter.builder("houseprice.predictions.total")
                .description("Total acumulado de previsoes realizadas com sucesso")
                .tag("type", "individual")
                .register(registry);

        this.batchItemsCounter = Counter.builder("houseprice.batch.items")
                .description("Total de registros individuais processados via arquivos batch")
                .register(registry);

        this.errorsCounter = Counter.builder("houseprice.errors.total")
                .description("Total acumulado de falhas de negocio ou excecoes nao tratadas")
                .register(registry);

        this.predictionTimer = Timer.builder("houseprice.prediction.latency")
                .description("Distribuicao do tempo de execucao da inferencia (Model Inference)")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        Gauge.builder("houseprice.requests.active", activeRequests, AtomicLong::get)
                .description("Numero instantaneo de requisicoes HTTP sendo processadas")
                .register(registry);
    }

    public void incrementActiveRequests() {
        activeRequests.incrementAndGet();
    }

    public void decrementActiveRequests() {
        activeRequests.decrementAndGet();
    }

    public void recordPrediction() {
        predictionsCounter.increment();
    }

    public void recordBatchItems(long count) {
        batchItemsCounter.increment(count);
    }

    public void recordError() {
        errorsCounter.increment();
    }

    public void recordLatency(long nanos) {
        predictionTimer.record(nanos, TimeUnit.NANOSECONDS);
    }
}