package org.Spurqlabs.Utils;

import java.util.ArrayList;
import java.util.List;

public class ScenarioResultCollector {
    public static class ScenarioResult {
        public final String scenarioName;
        public final boolean passed;
        public ScenarioResult(String scenarioName, boolean passed) {
            this.scenarioName = scenarioName;
            this.passed = passed;
        }
    }
    private static final List<ScenarioResult> results = new ArrayList<>();
    public static void addResult(String scenarioName, boolean passed) {
        results.add(new ScenarioResult(scenarioName, passed));
    }
    public static List<ScenarioResult> getResults() {
        return results;
    }
}

