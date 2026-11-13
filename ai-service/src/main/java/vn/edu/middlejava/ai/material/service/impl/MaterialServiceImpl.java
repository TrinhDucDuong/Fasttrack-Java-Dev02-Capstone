package vn.edu.middlejava.ai.material.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.ai.config.RabbitConfiguration;
import vn.edu.middlejava.ai.material.dto.CreateMaterialRequest;
import vn.edu.middlejava.ai.material.dto.MaterialResponse;
import vn.edu.middlejava.ai.material.entity.Material;
import vn.edu.middlejava.ai.material.mapper.MaterialMapper;
import vn.edu.middlejava.ai.material.repository.IMaterialRepository;
import vn.edu.middlejava.ai.material.service.IMaterialService;
import vn.edu.middlejava.ai.messaging.MaterialIndexingRequest;

@Service
public class MaterialServiceImpl implements IMaterialService {
    private final IMaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    private final RabbitTemplate rabbitTemplate;

    public MaterialServiceImpl(IMaterialRepository materialRepository, MaterialMapper materialMapper, RabbitTemplate rabbitTemplate) {
        this.materialRepository = materialRepository;
        this.materialMapper = materialMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public MaterialResponse create(CreateMaterialRequest request) {
        Material saved = materialRepository.save(new Material(request.courseId(), request.unitId(), request.title(), request.content()));
        rabbitTemplate.convertAndSend(RabbitConfiguration.MATERIAL_INDEXING_QUEUE, new MaterialIndexingRequest(saved.getId()));
        return materialMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponse get(Long materialId) {
        return materialRepository.findById(materialId).map(materialMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));
    }
}
