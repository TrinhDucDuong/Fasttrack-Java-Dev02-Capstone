package vn.edu.middlejava.learning.commerce.service;

import vn.edu.middlejava.learning.commerce.dto.CatalogRequest;
import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;

import java.util.List;

public interface ICatalogAdminService {
    List<CourseAdminResponse> getAll();

    CourseAdminResponse create(CatalogRequest request);

    CourseAdminResponse update(Long courseId, CatalogRequest request);

    void deactivate(Long courseId);
}
