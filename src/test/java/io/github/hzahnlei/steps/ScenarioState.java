package io.github.hzahnlei.steps;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Holds per-scenario state shared between step definition classes.
 */
@Component
@ScenarioScope
public class ScenarioState {

    public ResponseEntity<String> lastResponse;
    public String lastToolResponse;
    public List<Map<String, String>> pendingBatch;
    public String idempotencyKey;
}
