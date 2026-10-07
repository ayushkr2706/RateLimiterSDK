package com.ayush.rateLimiter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RateLimiterClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final String backendUrl;
    private final String apiKey;
    private final RateLimiterConfig rateLimiterConfig;

    public RateLimiterClient(String backendUrl, String apiKey){

        this(backendUrl, apiKey, new RateLimiterConfig());
    }

    public RateLimiterClient(String backendUrl, String apiKey,
                             RateLimiterConfig rateLimiterConfig)

    {

        this.backendUrl = backendUrl;
        this.apiKey = apiKey;
        this.rateLimiterConfig = rateLimiterConfig;

        //It is the object that sends HTTP requests and brings the HTTP response
        //It is the messenger that is responsible for performing HTTP communications.
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(rateLimiterConfig.getConnectTimeout())
                .build();

        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    //It is the front desk of the SDK that will be called by the tenant application.
    public Response invokeRateLimit(String policyId, String userId){

        try{
            return callBackend(policyId, userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return fallBackResponse();      //return the fail-open or closed strategy
        }
        catch(Exception ex){
            return fallBackResponse();      //return the fail-open or closed strategy
        }
    }

    private Response callBackend(String policyId, String userIp)
            throws IOException, InterruptedException {

        Request tenantRequest = new Request();
        tenantRequest.setPolicyId(policyId);
        tenantRequest.setUserIp(userIp);

        //Converting the tenantRequest object to JSON string
        String requestBody;
        requestBody = objectMapper.writeValueAsString(tenantRequest);

        String url = backendUrl.endsWith("/")
                ? backendUrl + "api/rate-limit"
                : backendUrl + "/api/rate-limit";

        //Building the POST Request
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(rateLimiterConfig.getRequestTimeout())
                .header("Authorization", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        //Sending the request to our backend application
        //and receiving the response sent by our backend inside the httpResponse object
        HttpResponse<String> httpResponse = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());

        if(httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300){

            throw new IOException("Rate-limit service returned HTTP " +
                    httpResponse.statusCode());
        }

        //Converts the Json response body into Java Object.
        return objectMapper.readValue(httpResponse.body(), Response.class);
    }

    private Response fallBackResponse(){
        Response response = new Response();
        response.setAllowed(rateLimiterConfig.isFailOpen());
        return response;
    }
}
