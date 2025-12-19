package com.hackathon.databeats.churninsight.application.port.input;

import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.StatsResponse;

public interface GetStatsUseCase {
    StatsResponse execute();
}