package org.Spurqlabs.Utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.util.Map;

public class APIUtility {
    public static Response sendRequest(String method, String url, Map<String, String> headers, Map<String, String> queryParams, Object body) {
        RequestSpecification request = RestAssured.given();
        if (headers != null && !headers.isEmpty()) {
            request.headers(headers);
        }
        if (queryParams != null && !queryParams.isEmpty()) {
            request.queryParams(queryParams);
        }
        if (body != null && !method.equalsIgnoreCase("GET")) {
            if (headers == null || !headers.containsKey("Content-Type")) {
                request.header("Content-Type", "application/json");
            }
            request.body(body);
        }
        switch (method.trim().toUpperCase()) {
            case "GET":
                return request.get(url);
            case "POST":
                return request.post(url);
            case "PUT":
                return request.put(url);
            case "PATCH":
                return request.patch(url);
            case "DELETE":
                return request.delete(url);
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    public static Response sendMultipartRequest(String url, Map<String, String> headers, Map<String, Object> multipartParams) {
        RequestSpecification request = RestAssured.given();
        if (headers != null && !headers.isEmpty()) {
            request.headers(headers);
        }
        // Attach the file
        if (multipartParams.containsKey("fileBody")) {
            File file = (File) multipartParams.get("fileBody");
            request.multiPart("fileBody", file);
        }
        // Attach other form fields
        for (Map.Entry<String, Object> entry : multipartParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!"fileBody".equals(key)) {
                request.multiPart(key, value.toString());
            }
        }
        // Ensure multipart content type
        request.contentType(ContentType.MULTIPART);
        return request.post(url);
    }
}
