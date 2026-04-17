package com.research.assistant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class ResearchService {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String processContent(ResearchRequest request) {

        try {
            //  Build prompt
            String prompt = buildPrompt(request);
            System.out.println(" Prompt: " + prompt);

            //  Request body
            Map<String, Object> requestBody = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{
                                    Map.of("text", prompt)
                            })
                    }
            );

            //  Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            //  Call Gemini API
            String response = restTemplate.postForObject(
                    geminiApiUrl + "?key=" + geminiApiKey,
                    entity,
                    String.class
            );

            System.out.println(" Gemini Response: " + response);

            return extractTextFromResponse(response);

        } catch (Exception e) {
            e.printStackTrace();
            return " Backend Error: " + e.getMessage();
        }
    }

    private String extractTextFromResponse(String response) {
        try {
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);

            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {

                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);

                if (firstCandidate.getContent() != null &&
                        firstCandidate.getContent().getParts() != null &&
                        !firstCandidate.getContent().getParts().isEmpty()) {

                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }

            return " No content found";

        } catch (Exception e) {
            return " Error parsing: " + e.getMessage();
        }
    }

    private String buildPrompt(ResearchRequest request) {

        StringBuilder prompt = new StringBuilder();

        switch (request.getOperation()) {
            case "summarize":
                prompt.append("Provide a clear and concise summary:\n\n");
                break;

            case "suggest":
                prompt.append("Suggest related topics with bullet points:\n\n");
                break;

            default:
                throw new IllegalArgumentException("Unknown Operation: " + request.getOperation());
        }

        prompt.append(request.getContent());

        return prompt.toString();
    }
}