package org.Spurqlabs.Utils;

import org.Spurqlabs.Core.TestContext;

public class TestContextLogger extends TestContext {

    public static void setStringContext(String key, String value) {
        TestContextLogger.stringContext.put(key, value);
    }

    private static String removeDoubleEmptySpaces(String value) {
        return value.replace("  ", " ");
    }

    private static String removeSpecialCharsFromString(String data) {
        return data.replaceAll("[^a-zA-Z0-9\\s]", "").replace("  ", " ");
    }

    public static void scenarioLog(String data, String value) {
        TestContextLogger.setStringContext(data, value);
        data = TestContextLogger.removeSpecialCharsFromString(data);
        value = TestContextLogger.removeDoubleEmptySpaces(value);
        scenarioLogger.log(data + ":" + value);
    }
}
