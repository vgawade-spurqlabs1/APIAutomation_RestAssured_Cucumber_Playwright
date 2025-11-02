package org.Spurqlabs.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DealDetailsManager {
    private static final String filePath = org.Spurqlabs.Utils.FrameworkConfigReader.getFrameworkConfig("DealDetails");
    public static JSONObject dealDetails;

    static {
        try {
            String content = Files.readString(Paths.get(filePath));
            dealDetails = new JSONObject(content);
        } catch (Exception e) {
            dealDetails = new JSONObject();
        }
    }

    public static synchronized void put(String key, Object value) {
        dealDetails.put(key, value);
        save();
    }

    public static synchronized Object get(String key) {
        return dealDetails.opt(key);
    }

    public static synchronized JSONObject getAll() {
        return dealDetails;
    }

    private static synchronized void save() {
        try {
            Files.write(Paths.get(filePath), dealDetails.toString(4).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper for placeholder replacement
    public static String replacePlaceholders(String value) throws IOException {
        JSONObject getDealDetails = new JSONObject(Files.readString(Paths.get(FrameworkConfigReader.getFrameworkConfig("DealDetails")), StandardCharsets.UTF_8));

        for (String key : getDealDetails.keySet()) {
            value = value.replace("{" + key + "}", getDealDetails.getString(key));
        }
        return value;
    }
}

