package org.Spurqlabs.Utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CustomHtmlReport {
    public static void generateHtmlReport(List<ScenarioResultCollector.ScenarioResult> results, String filePath) {
        int total = results.size();
        int passed = (int) results.stream().filter(r -> r.passed).count();
        int failed = total - passed;
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>API Test Report</title>");
        html.append("<style>table{border-collapse:collapse;}th,td{border:1px solid #ccc;padding:8px;}th{background:#eee;}</style>");
        html.append("</head><body>");
        html.append("<h2>API Test Execution Summary</h2>");
        html.append("<ul>");
        html.append("<li>Total Scenarios: ").append(total).append("</li>");
        html.append("<li>Passed: ").append(passed).append("</li>");
        html.append("<li>Failed: ").append(failed).append("</li>");
        html.append("</ul>");
        html.append("<h3>Scenario Details</h3>");
        html.append("<table><tr><th>Scenario Name</th><th>Status (Pass/Fail)</th></tr>");
        for (ScenarioResultCollector.ScenarioResult r : results) {
            html.append("<tr><td>").append(r.scenarioName).append("</td><td>")
                .append(r.passed ? "Pass" : "Fail").append("</td></tr>");
        }
        html.append("</table></body></html>");
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(html.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}