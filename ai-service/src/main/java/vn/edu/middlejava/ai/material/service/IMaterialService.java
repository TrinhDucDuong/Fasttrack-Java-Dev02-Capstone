package vn.edu.middlejava.ai.material.service;

import vn.edu.middlejava.ai.material.dto.MaterialResponse;
import vn.edu.middlejava.ai.material.dto.CreateMaterialRequest;

public interface IMaterialService {
    MaterialResponse create(CreateMaterialRequest request);
    MaterialResponse get(Long materialId);
}
