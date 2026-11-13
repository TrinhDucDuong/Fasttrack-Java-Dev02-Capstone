package vn.edu.middlejava.ai.chat.dto;

public record SourceResponse(Long materialId, String title, int chunkIndex, String excerpt, double score) {
}
