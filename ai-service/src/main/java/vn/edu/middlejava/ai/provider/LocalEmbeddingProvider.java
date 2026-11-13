package vn.edu.middlejava.ai.provider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.edu.middlejava.ai.config.AiProperties;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "app.ai", name = "mode", havingValue = "remote", matchIfMissing = true)
public class LocalEmbeddingProvider implements IEmbeddingProvider {
    private final AiProperties properties;

    public LocalEmbeddingProvider(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<Double> embed(String text) {
        int dimensions = properties.getEmbeddingDimensions();
        double[] vector = new double[dimensions];
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N]+");
        for (String token : tokens) {
            if (!token.isBlank()) {
                vector[Math.floorMod(hash(token), dimensions)] += 1.0;
            }
        }
        double norm = Math.sqrt(Arrays.stream(vector).map(value -> value * value).sum());
        List<Double> result = new ArrayList<>(dimensions);
        for (double value : vector) {
            result.add(norm == 0.0 ? 0.0 : value / norm);
        }
        return result;
    }

    private int hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest).getInt();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
