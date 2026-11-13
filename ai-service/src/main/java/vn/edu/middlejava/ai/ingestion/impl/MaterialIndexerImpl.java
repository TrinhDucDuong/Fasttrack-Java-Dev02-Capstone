package vn.edu.middlejava.ai.ingestion.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.ai.config.AiProperties;
import vn.edu.middlejava.ai.ingestion.IMaterialIndexer;
import vn.edu.middlejava.ai.ingestion.TextChunker;
import vn.edu.middlejava.ai.material.entity.Material;
import vn.edu.middlejava.ai.material.entity.MaterialStatus;
import vn.edu.middlejava.ai.material.repository.IMaterialRepository;
import vn.edu.middlejava.ai.provider.IEmbeddingProvider;

import java.util.List;

@Service
public class MaterialIndexerImpl implements IMaterialIndexer {
    private final IMaterialRepository materialRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TextChunker textChunker;
    private final IEmbeddingProvider embeddingProvider;
    private final AiProperties properties;

    public MaterialIndexerImpl(IMaterialRepository materialRepository, JdbcTemplate jdbcTemplate, TextChunker textChunker,
                               IEmbeddingProvider embeddingProvider, AiProperties properties) {
        this.materialRepository = materialRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.textChunker = textChunker;
        this.embeddingProvider = embeddingProvider;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void index(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));
        material.setStatus(MaterialStatus.PROCESSING);
        jdbcTemplate.update("DELETE FROM document_chunks WHERE material_id = ?", materialId);
        List<String> chunks = textChunker.split(material.getContent(), properties.getChunkSize(), properties.getChunkOverlap());
        for (int index = 0; index < chunks.size(); index++) {
            String content = chunks.get(index);
            List<Double> embedding = embeddingProvider.embed(content);
            jdbcTemplate.update("""
                    INSERT INTO document_chunks(material_id, course_id, unit_id, chunk_index, content, embedding)
                    VALUES (?, ?, ?, ?, ?, CAST(? AS vector))
                    """, materialId, material.getCourseId(), material.getUnitId(), index, content, toVectorLiteral(embedding));
        }
        material.setStatus(MaterialStatus.READY);
    }

    private String toVectorLiteral(List<Double> vector) {
        if (vector.size() != properties.getEmbeddingDimensions()) {
            throw new IllegalArgumentException("Embedding dimension does not match configured dimension");
        }
        return vector.toString().replace(" ", "");
    }
}
