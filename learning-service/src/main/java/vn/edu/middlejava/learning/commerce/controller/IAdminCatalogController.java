package vn.edu.middlejava.learning.commerce.controller;

import org.springframework.http.ResponseEntity;
import vn.edu.middlejava.learning.commerce.dto.CatalogRequest;
import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;

public interface IAdminCatalogController {
    ResponseEntity<CourseAdminResponse> create(CatalogRequest request);

    CourseAdminResponse update(Long courseId, CatalogRequest request);
}
