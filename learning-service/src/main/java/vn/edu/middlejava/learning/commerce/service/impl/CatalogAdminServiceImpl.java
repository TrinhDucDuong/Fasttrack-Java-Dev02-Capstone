package vn.edu.middlejava.learning.commerce.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.commerce.dto.CatalogRequest;
import vn.edu.middlejava.learning.commerce.service.ICatalogAdminService;
import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.mapper.CourseMapper;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

@Service
public class CatalogAdminServiceImpl implements ICatalogAdminService {
    private final ICourseRepository courses;
    private final CourseMapper mapper;

    public CatalogAdminServiceImpl(ICourseRepository courses, CourseMapper mapper) {
        this.courses = courses;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CourseAdminResponse create(CatalogRequest request) {
        if (courses.findBySlug(request.slug()).isPresent()) {
            throw new IllegalArgumentException("Course slug already exists");
        }
        Course course = new Course(request.slug(), request.title(), request.description(),
                request.price(), request.youtubeUrl());
        return mapper.toAdminResponse(courses.save(course));
    }

    @Override
    @Transactional
    public CourseAdminResponse update(Long courseId, CatalogRequest request) {
        Course course = courses.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (!course.getSlug().equals(request.slug())) {
            throw new IllegalArgumentException("Course slug cannot be changed");
        }
        course.updateCatalog(request.title(), request.description(), request.price(), request.youtubeUrl());
        return mapper.toAdminResponse(courses.save(course));
    }
}
