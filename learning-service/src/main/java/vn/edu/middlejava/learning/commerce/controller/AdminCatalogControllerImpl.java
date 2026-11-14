package vn.edu.middlejava.learning.commerce.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.middlejava.learning.commerce.dto.CatalogRequest;
import vn.edu.middlejava.learning.commerce.service.ICatalogAdminService;
import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;

@RestController
@RequestMapping("/api/admin/courses")
public class AdminCatalogControllerImpl implements IAdminCatalogController {
    private final ICatalogAdminService catalogService;

    public AdminCatalogControllerImpl(ICatalogAdminService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    @PostMapping
    public ResponseEntity<CourseAdminResponse> create(@Valid @RequestBody CatalogRequest request) {
        return ResponseEntity.status(201).body(catalogService.create(request));
    }

    @Override
    @PutMapping("/{courseId}")
    public CourseAdminResponse update(@PathVariable("courseId") Long courseId,
                                 @Valid @RequestBody CatalogRequest request) {
        return catalogService.update(courseId, request);
    }
}
