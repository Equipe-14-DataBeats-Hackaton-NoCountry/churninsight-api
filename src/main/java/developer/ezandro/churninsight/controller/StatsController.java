package developer.ezandro.churninsight.controller;

import developer.ezandro.churninsight.infra.api.dto.StatsResponse;
import developer.ezandro.churninsight.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/stats")
public class StatsController {
    private final StatsService statsService;

    @GetMapping
    public StatsResponse getStats() {
        return this.statsService.getStats();
    }
}