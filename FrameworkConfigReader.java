package org.Spurqlabs.Utils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FrameworkConfigReader {
    public static String getFrameworkConfig(String parameter) {
        String assemblyLocation = System.getProperty("user.dir");
        String path = Paths.get(assemblyLocation + "/FrameworkConfig.json").toString();
        String fullPath = new File(path).getAbsolutePath();
        try {
            String json = Files.readString(Path.of(fullPath));
            JSONObject jObject = new JSONObject(json);
            return jObject.optString(parameter, "");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
