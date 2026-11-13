package vn.edu.middlejava.ai.material.controller;

import org.springframework.http.ResponseEntity;
import vn.edu.middlejava.ai.material.dto.CreateMaterialRequest;
import vn.edu.middlejava.ai.material.dto.MaterialResponse;

public interface IMaterialController {
    ResponseEntity<MaterialResponse> create(CreateMaterialRequest request);
    ResponseEntity<MaterialResponse> get(Long materialId);
}
