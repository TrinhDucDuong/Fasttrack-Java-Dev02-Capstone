package vn.edu.middlejava.learning.commerce.service;

import vn.edu.middlejava.learning.commerce.dto.CatalogRequest;
import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;

public interface ICatalogAdminService {
    CourseAdminResponse create(CatalogRequest request);

    CourseAdminResponse update(Long courseId, CatalogRequest request);
}
