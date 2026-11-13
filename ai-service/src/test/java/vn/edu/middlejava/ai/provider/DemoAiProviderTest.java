package vn.edu.middlejava.ai.provider;

import org.junit.jupiter.api.Test;
import vn.edu.middlejava.ai.config.AiProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemoAiProviderTest {
    @Test
    void producesStableNormalizedEmbeddings() {
        AiProperties properties = new AiProperties();
        properties.setEmbeddingDimensions(64);
        DemoAiProvider provider = new DemoAiProvider(properties);

        List<Double> first = provider.embed("Java streams and optional");
        List<Double> second = provider.embed("Java streams and optional");

        assertThat(first).hasSize(64).isEqualTo(second);
        assertThat(first.stream().mapToDouble(value -> value * value).sum()).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.000001));
    }

    @Test
    void doesNotInventAnAnswerWithoutRetrievedContext() {
        DemoAiProvider provider = new DemoAiProvider(new AiProperties());

        assertThat(provider.answer("What is the subject?", List.of())).contains("could not find supporting");
    }
}
