package vn.edu.middlejava.ai.chat.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.edu.middlejava.ai.chat.dto.SourceResponse;

import java.util.List;

@Repository
public class VectorSearchRepository {
    private final JdbcTemplate jdbcTemplate;

    public VectorSearchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<SourceResponse> findRelevantChunks(Long unitId, String vectorLiteral, int limit) {
        return jdbcTemplate.query("""
                SELECT c.material_id, m.title, c.chunk_index, c.content,
                       1 - (c.embedding <=> CAST(? AS vector)) AS score
                FROM document_chunks c
                JOIN materials m ON m.id = c.material_id
                WHERE c.unit_id = ? AND m.status = 'READY'
                ORDER BY c.embedding <=> CAST(? AS vector)
                LIMIT ?
                """, (rs, rowNumber) -> new SourceResponse(rs.getLong("material_id"), rs.getString("title"),
                rs.getInt("chunk_index"), rs.getString("content"), rs.getDouble("score")),
                vectorLiteral, unitId, vectorLiteral, limit);
    }
}
