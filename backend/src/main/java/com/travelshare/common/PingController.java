package com.travelshare.common;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Smoke-test endpoint for the /api proxy chain.
 *
 * <p>Deliberately free of service/mapper layers: it proves the HTTP + proxy path only.
 */
@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("service", "travel-share-backend");
        data.put("time", OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
        return Result.ok(data);
    }
}
