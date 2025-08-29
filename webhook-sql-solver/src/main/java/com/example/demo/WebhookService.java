package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class WebhookService {
    
    @Value("${generate.webhook.url}")
    private String generateWebhookUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private SqlProblemSolver sqlProblemSolver;
    
    public void processWebhookAndSolve() {
        try {
            // Step 1: Generate webhook
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String requestBody = "{\"name\": \"John Doe\", \"regNo\": \"REG12347\", \"email\": \"john@example.com\"}";
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(
                generateWebhookUrl, request, WebhookResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                WebhookResponse webhookResponse = response.getBody();
                
                // Step 2: Solve the SQL problem
                String finalQuery = sqlProblemSolver.solveSqlProblem();
                
                // Step 3: Submit the solution
                submitSolution(webhookResponse.getWebhook(), 
                              webhookResponse.getAccessToken(), 
                              finalQuery);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        
        SolutionRequest solutionRequest = new SolutionRequest(finalQuery);
        HttpEntity<SolutionRequest> request = new HttpEntity<>(solutionRequest, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            webhookUrl, request, String.class);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Solution submitted successfully!");
        } else {
            System.out.println("Failed to submit solution: " + response.getStatusCode());
        }
    }
}