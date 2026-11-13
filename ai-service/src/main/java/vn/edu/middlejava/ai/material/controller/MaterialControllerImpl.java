package vn.edu.middlejava.ai.material.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.ai.material.dto.CreateMaterialRequest;
import vn.edu.middlejava.ai.material.dto.MaterialResponse;
import vn.edu.middlejava.ai.material.service.IMaterialService;

@RestController
@RequestMapping("/api/materials")
public class MaterialControllerImpl implements IMaterialController {
    private final IMaterialService materialService;

    public MaterialControllerImpl(IMaterialService materialService) {
        this.materialService = materialService;
    }

    @Override
    @PostMapping
    public ResponseEntity<MaterialResponse> create(@Valid @RequestBody CreateMaterialRequest request) {
        return ResponseEntity.accepted().body(materialService.create(request));
    }

    @Override
    @GetMapping("/{materialId}")
    public ResponseEntity<MaterialResponse> get(@PathVariable Long materialId) {
        return ResponseEntity.ok(materialService.get(materialId));
    }
}
