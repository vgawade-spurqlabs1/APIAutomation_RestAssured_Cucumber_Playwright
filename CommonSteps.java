package org.Spurqlabs.Steps; 
 
import io.cucumber.java.en.Then; 
import io.cucumber.java.en.When; 
import io.restassured.response.Response; 
import org.Spurqlabs.Core.TestContext; 
import org.Spurqlabs.Utils.*; 
import org.json.JSONArray; 
import org.json.JSONObject; 
 
import java.io.File; 
import java.io.IOException; 
import java.nio.charset.StandardCharsets; 
import java.nio.file.Files; 
import java.nio.file.Paths; 
import java.util.HashMap; 
import java.util.Map; 
 
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath; 
import static org.Spurqlabs.Utils.DealDetailsManager.replacePlaceholders; 
import static org.hamcrest.Matchers.equalTo; 
public class CommonSteps extends TestContext { 
    private Response response; 
 
    @When("User sends {string} request to {string} with headers {string} and query file {string} and requestDataFile {string}") 
    public void user_sends_request_to_with_query_file_and_requestDataFile (String method, String url, String headers, String queryFile, String bodyFile) throws IOException { 
        String jsonString = Files.readString(Paths.get(FrameworkConfigReader.getFrameworkConfig("DealDetails")), StandardCharsets.UTF_8); 
        JSONObject storedValues = new JSONObject(jsonString); 
 
        String fullUrl = FrameworkConfigReader.getFrameworkConfig("BaseUrl") + replacePlaceholders(url); 
 
        Map<String, String> header = new HashMap<>(); 
        if (!"NA".equalsIgnoreCase(headers)) { 
            header = JsonFileReader.getHeadersFromJson(FrameworkConfigReader.getFrameworkConfig("headers") + headers + ".json"); 
        } else { 
            header.put("cookie", TokenManager.getToken()); 
        } 
 
        Map<String, String> queryParams = new HashMap<>(); 
        if (!"NA".equalsIgnoreCase(queryFile)) { 
            queryParams = JsonFileReader.getQueryParamsFromJson(FrameworkConfigReader.getFrameworkConfig("Query_Parameters") + queryFile + ".json"); 
            for (String key : queryParams.keySet()) { 
                String value = queryParams.get(key); 
                for (String storedKey : storedValues.keySet()) { 
                    value = value.replace("{" + storedKey + "}", storedValues.getString(storedKey)); 
                } 
                queryParams.put(key, value); 
            } 
        } 
 
        Object requestBody = null; 
        if (!"NA".equalsIgnoreCase(bodyFile)) { 
            String bodyTemplate = JsonFileReader.getJsonAsString( 
                    FrameworkConfigReader.getFrameworkConfig("Request_Bodies") + bodyFile + ".json"); 
 
            for (String key : storedValues.keySet()) { 
                String placeholder = "{" + key + "}"; 
                if (bodyTemplate.contains(placeholder)) { 
                    bodyTemplate = bodyTemplate.replace(placeholder, storedValues.getString(key)); 
                } 
            } 
 
            requestBody = bodyTemplate; 
        } 
 
 
        response = APIUtility.sendRequest(method, fullUrl, header, queryParams, requestBody); 
        response.prettyPrint(); 
        TestContextLogger.scenarioLog("API", "Request sent: " + method + " " + fullUrl); 
 
        if (scenarioName.contains("GET Notes") && response.getStatusCode() == 200) { 
            DealDetailsManager.put("noteId", response.path("[0].id")); 
        } 
         
    } 
 
    @Then("User verifies the response status code is {int}") 
    public void userVerifiesTheResponseStatusCodeIsStatusCode(int statusCode) { 
        response.then().statusCode(statusCode); 
        TestContextLogger.scenarioLog("API", "Response status code: " + statusCode); 
    } 
 
    @Then("User verifies the response body matches JSON schema {string}") 
    public void userVerifiesTheResponseBodyMatchesJSONSchema(String schemaFile) { 
        if (!"NA".equalsIgnoreCase(schemaFile)) { 
            String schemaPath = "Schema/" + schemaFile + ".json"; 
            response.then().assertThat().body(matchesJsonSchemaInClasspath(schemaPath)); 
            TestContextLogger.scenarioLog("API", "Response body matches schema"); 
        } else { 
            TestContextLogger.scenarioLog("API", "Response body does not have schema to validate"); 
        } 
    } 
 
    @Then("User verifies field {string} has value {string}") 
    public void userVerifiesFieldHasValue(String jsonPath, String expectedValue) { 
        response.then().body(jsonPath, equalTo(expectedValue)); 
        TestContextLogger.scenarioLog("API", "Field " + jsonPath + " has value: " + expectedValue); 
    } 
 
    @Then("User verifies fields in response: {string} with content type {string}") 
    public void userVerifiesFieldsInResponseWithContentType(String contentType, String fields) throws IOException { 
        // If NA, skip verification 
        if ("NA".equalsIgnoreCase(contentType) || "NA".equalsIgnoreCase(fields)) { 
            return; 
        } 
        String responseStr = response.getBody().asString().trim(); 
 
        try { 
            if ("text".equalsIgnoreCase(contentType)) { 
                // For text, verify each expected value is present in response 
                for (String expected : fields.split(";")) { 
                    expected = replacePlaceholders(expected.trim()); 
                    if (!responseStr.contains(expected)) { 
                        throw new AssertionError("Expected text not found: " + expected); 
                    } 
                    TestContextLogger.scenarioLog("API", "Text found: " + expected); 
                } 
            } else if ("json".equalsIgnoreCase(contentType)) { 
                // For json, verify key=value pairs 
                JSONObject jsonResponse; 
                if (responseStr.startsWith("[")) { 
                    JSONArray arr = new JSONArray(responseStr); 
                    jsonResponse = !arr.isEmpty() ? arr.getJSONObject(0) : new JSONObject(); 
                } else { 
                    jsonResponse = new JSONObject(responseStr); 
                } 
                for (String pair : fields.split(";")) { 
                    if (pair.trim().isEmpty()) continue; 
                    String[] kv = pair.split("=", 2); 
                    if (kv.length < 2) continue; 
                    String keyPath = kv[0].trim(); 
                    String expected = replacePlaceholders(kv[1].trim()); 
                    Object actual = JsonFileReader.getJsonValueByPath(jsonResponse, keyPath); 
                    if (actual == null) { 
                        throw new AssertionError("Key not found in JSON: " + keyPath); 
                    } 
                    if (!String.valueOf(actual).equals(String.valueOf(expected))) { 
                        throw new AssertionError("Mismatch for " + keyPath + ": expected '" + expected + "', got '" + actual + "'"); 
                    } 
                    TestContextLogger.scenarioLog("API", "Validated: " + keyPath + " = " + expected); 
                } 
            } else { 
                throw new AssertionError("Unsupported content type: " + contentType); 
            } 
        } catch (AssertionError | Exception e) { 
            TestContextLogger.scenarioLog("API", "Validation failed: " + e.getMessage()); 
            throw e; 
        } 
    } 
 
} 

 