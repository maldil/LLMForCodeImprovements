package org.mal;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIRequestHandler {

    private static String API_KEY = "";
    private static final String GPT_ENDPOINT = "https://api.openai.com/v1/chat/completions";

    public OpenAIRequestHandler() {
        API_KEY = FileIO.readStringFromFile(Configurations.OPEN_AI_KEY).strip();
        System.out.println(API_KEY);
    }

    public JSONArray getGPT4Response(String prompt) {
        HttpClient client = HttpClient.newHttpClient();
        JSONObject jsonPayload = new JSONObject();
        JSONArray messages = new JSONArray();
        JSONObject messageObject = new JSONObject();
        messageObject.put("role", "user"); // Add role property
        messageObject.put("content", prompt);
        messages.put(messageObject);
        jsonPayload.put("model", "gpt-3.5-turbo");
        jsonPayload.put("messages", messages); // Add messages array

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GPT_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(BodyPublishers.ofString(jsonPayload.toString()))
                .build();
        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            System.out.println("Response status code: " + response.statusCode()); // Log the status code
            JSONObject jsonResponse = new JSONObject(response.body());


            if (response.statusCode() == 200) {
                JSONArray choices = jsonResponse.getJSONArray("choices");
                JSONArray allIm = new JSONArray();
                for (int i = 0; i < choices.length(); i++) {
                    JSONObject choice = choices.getJSONObject(i);
                    JSONObject message = choice.getJSONObject("message");

                    String contentString = message.getString("content");
                    JSONObject contentJson = new JSONObject(contentString);
                    allIm.put(contentJson);
                }
                    return allIm;
            } else {
                    JSONObject errorResponse = new JSONObject();

                    errorResponse.put("error", "Received non-OK response from the API: " + response.statusCode());
                    errorResponse.put("details", jsonResponse);
                    JSONArray allIm = new JSONArray();
                    allIm.put(errorResponse);
                    return allIm;
            }

        } catch (IOException e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "IOException occurred");
            errorResponse.put("details", e.getMessage());
            JSONArray allIm = new JSONArray();
            allIm.put(errorResponse);
            return allIm;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Request was interrupted");
            errorResponse.put("details", e.getMessage());
            JSONArray allIm = new JSONArray();
            allIm.put(errorResponse);
            return allIm;
        } catch (Exception e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "An unexpected error occurred");
            errorResponse.put("details", e.getMessage());
            JSONArray allIm = new JSONArray();
            allIm.put(errorResponse);
            return allIm;
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        OpenAIRequestHandler handler = new OpenAIRequestHandler();
        String prompt = "Translate the following English text to French: 'Hello, how are you?'";
        JSONArray response = handler.getGPT4Response(prompt);
        System.out.println("Final response: " + response);
    }
}

