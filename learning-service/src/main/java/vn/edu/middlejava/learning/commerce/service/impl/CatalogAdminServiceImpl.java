package vn.edu.middlejava.learning.commerce.service.impl;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.commerce.dto.CatalogRequest;
import vn.edu.middlejava.learning.commerce.service.ICatalogAdminService;
import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.mapper.CourseMapper;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.util.List;

/** SRP: product (course) management for ADMIN, separated from the public read-only catalog. */
@Service
public class CatalogAdminServiceImpl implements ICatalogAdminService {
    private final ICourseRepository courses;
    private final CourseMapper mapper;

    public CatalogAdminServiceImpl(ICourseRepository courses, CourseMapper mapper) {
        this.courses = courses;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseAdminResponse> getAll() {
        return courses.findAll(Sort.by("id")).stream()
                .map(mapper::toAdminResponse)
                .toList();
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
        Course course = findCourse(courseId);
        if (!course.getSlug().equals(request.slug())) {
            throw new IllegalArgumentException("Course slug cannot be changed");
        }
        course.updateCatalog(request.title(), request.description(), request.price(), request.youtubeUrl());
        return mapper.toAdminResponse(courses.save(course));
    }

    @Override
    @Transactional
    public void deactivate(Long courseId) {
        findCourse(courseId).deactivate();
    }

    private Course findCourse(Long courseId) {
        return courses.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }
}
