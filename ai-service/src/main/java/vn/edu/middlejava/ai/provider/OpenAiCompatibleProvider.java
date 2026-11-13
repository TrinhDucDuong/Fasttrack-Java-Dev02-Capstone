package vn.edu.middlejava.ai.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.edu.middlejava.ai.config.AiProperties;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "remote")
public class OpenAiCompatibleProvider implements IChatProvider {
    private final AiProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleProvider(AiProperties properties, RestClient.Builder builder, ObjectMapper objectMapper) {
        this.properties = properties;
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("Set the GROQ_API_KEY environment variable before starting ai-service");
        }
        this.restClient = builder.baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String answer(String question, List<String> context) {
        String groundedContext = String.join("\n\n", context);
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", "Answer only from the provided course context. If it does not support an answer, say so. Cite source labels included in the context."),
                Map.of("role", "user", "content", "Context:\n" + groundedContext + "\n\nQuestion:\n" + question));
        Map<String, Object> response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("model", properties.getChatModel(), "messages", messages))
                .retrieve()
                .body(Map.class);
        Map<String, Object> choice = (Map<String, Object>) ((List<?>) response.get("choices")).get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        return String.valueOf(message.get("content"));
    }

    @Override
    public List<QuizDraftQuestion> generateQuiz(List<String> context, int count) {
        String instruction = "Create exactly " + count + " multiple-choice questions from the course context. " +
                "Return only JSON matching {\\\"questions\\\":[{\\\"prompt\\\":string,\\\"explanation\\\":string,\\\"options\\\":[{\\\"text\\\":string,\\\"correct\\\":boolean}]}]}. " +
                "Each question must have exactly one correct option and three options total. Context: " + String.join("\\n\\n", context);
        String content = complete(instruction);
        try {
            String json = content.replaceAll("^```(?:json)?\\s*|\\s*```$", "").trim();
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json).path("questions");
            List<QuizDraftQuestion> questions = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode question : root) {
                List<QuizDraftQuestion.Option> options = new java.util.ArrayList<>();
                for (com.fasterxml.jackson.databind.JsonNode option : question.path("options")) {
                    options.add(new QuizDraftQuestion.Option(option.path("text").asText(), option.path("correct").asBoolean()));
                }
                questions.add(new QuizDraftQuestion(question.path("prompt").asText(), question.path("explanation").asText(), options));
            }
            return List.copyOf(questions);
        } catch (Exception exception) {
            throw new IllegalStateException("AI provider returned an invalid quiz payload", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private String complete(String prompt) {
        List<Map<String, Object>> messages = List.of(Map.of("role", "user", "content", prompt));
        Map<String, Object> response = restClient.post().uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("model", properties.getChatModel(), "messages", messages,
                        "response_format", Map.of("type", "json_object")))
                .retrieve().body(Map.class);
        Map<String, Object> choice = (Map<String, Object>) ((List<?>) response.get("choices")).get(0);
        Map<String, Object> message = (Map<String, Object>) choice.get("message");
        return String.valueOf(message.get("content"));
    }

    @Override
    public String modelName() {
        return properties.getChatModel();
    }
}
