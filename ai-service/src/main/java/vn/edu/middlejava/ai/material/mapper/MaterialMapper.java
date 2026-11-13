package vn.edu.middlejava.ai.material.mapper;

import org.springframework.stereotype.Component;
import vn.edu.middlejava.ai.material.dto.MaterialResponse;
import vn.edu.middlejava.ai.material.entity.Material;

@Component
public class MaterialMapper {
    public MaterialResponse toResponse(Material material) {
        return new MaterialResponse(material.getId(), material.getCourseId(), material.getUnitId(), material.getTitle(), material.getStatus().name());
    }
}
