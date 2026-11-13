package vn.edu.middlejava.ai.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.edu.middlejava.ai.config.AiProperties;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "demo", matchIfMissing = true)
public class DemoAiProvider implements IEmbeddingProvider, IChatProvider {
    private final AiProperties properties;

    public DemoAiProvider(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<Double> embed(String text) {
        int dimensions = properties.getEmbeddingDimensions();
        double[] vector = new double[dimensions];
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            int index = Math.floorMod(hash(token), dimensions);
            vector[index] += 1.0;
        }
        double norm = Math.sqrt(java.util.Arrays.stream(vector).map(value -> value * value).sum());
        List<Double> result = new ArrayList<>(dimensions);
        for (double value : vector) {
            result.add(norm == 0.0 ? 0.0 : value / norm);
        }
        return result;
    }

    @Override
    public String answer(String question, List<String> context) {
        if (context.isEmpty()) {
            return "I could not find supporting course material for this question.";
        }
        String excerpt = context.get(0);
        return "Based on the retrieved course material, " + excerpt.substring(0, Math.min(excerpt.length(), 700));
    }

    @Override
    public List<QuizDraftQuestion> generateQuiz(List<String> context, int count) {
        if (context.isEmpty()) {
            return List.of();
        }
        List<QuizDraftQuestion> questions = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            String source = context.get(index % context.size());
            String excerpt = source.substring(0, Math.min(source.length(), 180));
            questions.add(new QuizDraftQuestion("Which statement is supported by the selected course material?",
                    "The correct choice is taken from the retrieved course material.", List.of(
                    new QuizDraftQuestion.Option(excerpt, true),
                    new QuizDraftQuestion.Option("The material says this topic is unrelated to the course.", false),
                    new QuizDraftQuestion.Option("The material recommends skipping this topic.", false))));
        }
        return List.copyOf(questions);
    }

    @Override
    public String modelName() {
        return "demo-extractive";
    }

    private int hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.nio.ByteBuffer.wrap(digest).getInt();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
