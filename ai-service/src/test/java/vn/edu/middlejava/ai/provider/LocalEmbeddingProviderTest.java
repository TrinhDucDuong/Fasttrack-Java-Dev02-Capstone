package vn.edu.middlejava.ai.provider;

import org.junit.jupiter.api.Test;
import vn.edu.middlejava.ai.config.AiProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalEmbeddingProviderTest {
    @Test
    void createsStableNormalizedEmbeddingsForUnicodeText() {
        AiProperties properties = new AiProperties();
        properties.setEmbeddingDimensions(128);
        LocalEmbeddingProvider provider = new LocalEmbeddingProvider(properties);

        List<Double> first = provider.embed("Đóng gói Java: fields và methods");
        List<Double> second = provider.embed("Đóng gói Java: fields và methods");

        assertThat(first).hasSize(128).isEqualTo(second);
        assertThat(first.stream().mapToDouble(value -> value * value).sum())
                .isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.000001));
    }

    @Test
    void returnsZeroVectorForTextWithoutTokens() {
        AiProperties properties = new AiProperties();
        properties.setEmbeddingDimensions(8);
        LocalEmbeddingProvider provider = new LocalEmbeddingProvider(properties);

        assertThat(provider.embed("--- ... !!!"))
                .hasSize(8)
                .containsOnly(0.0);
    }
}
