package vn.edu.middlejava.learning.quiz.client.impl;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import vn.edu.middlejava.learning.quiz.client.IAiQuizClient;

import java.util.List;
import java.util.Map;

@Component
public class AiQuizClientImpl implements IAiQuizClient {
    private final RestClient restClient;

    public AiQuizClientImpl(@org.springframework.cloud.client.loadbalancer.LoadBalanced RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://ai-service").build();
    }

    @Override
    public List<GeneratedQuestion> generate(Long unitId, int questionCount, String bearerToken) {
        Map<String, Object> request = Map.of("unitId", unitId, "questionCount", questionCount);
        DraftResponse response = restClient.post().uri("/api/ai/quiz-drafts")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(bearerToken))
                .body(request)
                .retrieve()
                .body(DraftResponse.class);
        return response == null || response.questions() == null ? List.of() : response.questions();
    }

    private record DraftResponse(List<GeneratedQuestion> questions) {
    }
}
