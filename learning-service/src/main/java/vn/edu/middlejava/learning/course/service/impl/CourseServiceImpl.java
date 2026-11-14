package vn.edu.middlejava.learning.course.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.course.dto.CourseResponse;
import vn.edu.middlejava.learning.course.dto.UnitResponse;
import vn.edu.middlejava.learning.course.mapper.CourseMapper;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.repository.ICourseUnitRepository;
import vn.edu.middlejava.learning.course.service.ICourseService;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements ICourseService {
    private final ICourseRepository courseRepository;
    private final ICourseUnitRepository courseUnitRepository;
    private final CourseMapper courseMapper;

    public CourseServiceImpl(ICourseRepository courseRepository, ICourseUnitRepository courseUnitRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseUnitRepository = courseUnitRepository;
        this.courseMapper = courseMapper;
    }

    @Override
    public List<CourseResponse> getCourses() {
        return courseRepository.findAll().stream().map(courseMapper::toResponse).toList();
    }

    @Override
    public List<CourseResponse> searchCourses(String query) {
        if (query == null || query.isBlank()) return getCourses();
        return courseRepository.findByTitleContainingIgnoreCase(query.trim()).stream().map(courseMapper::toResponse).toList();
    }

    @Override
    public List<UnitResponse> getUnits(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }
        return courseUnitRepository.findByCourseIdOrderByUnitNumber(courseId).stream().map(courseMapper::toResponse).toList();
    }

    @Override
    public UnitResponse getUnit(Long courseId, Long unitId) {
        return courseUnitRepository.findByIdAndCourseId(unitId, courseId)
                .map(courseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found"));
    }
}
