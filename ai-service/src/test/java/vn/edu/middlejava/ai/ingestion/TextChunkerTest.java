package vn.edu.middlejava.ai.ingestion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class TextChunkerTest {
    private final TextChunker chunker = new TextChunker();

    @Test
    void returnsNoChunksForBlankText() {
        assertThat(chunker.split("  ", 20, 2)).isEmpty();
    }

    @Test
    void splitsTextWithOverlap() {
        List<String> chunks = chunker.split("one two three four five six seven", 15, 4);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks.get(0)).contains("one");
        assertThat(chunks.get(1)).isNotBlank();
    }

    @Test
    void rejectsInvalidChunkConfiguration() {
        assertThatIllegalArgumentException().isThrownBy(() -> chunker.split("text", 10, 10));
    }
}
