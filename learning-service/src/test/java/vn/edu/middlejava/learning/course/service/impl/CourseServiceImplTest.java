package vn.edu.middlejava.learning.course.service.impl;

import org.junit.jupiter.api.Test;
import vn.edu.middlejava.learning.course.dto.CourseResponse;
import vn.edu.middlejava.learning.course.dto.UnitResponse;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.entity.CourseUnit;
import vn.edu.middlejava.learning.course.mapper.CourseMapper;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.repository.ICourseUnitRepository;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourseServiceImplTest {
    private final ICourseRepository courseRepository = mock(ICourseRepository.class);
    private final ICourseUnitRepository unitRepository = mock(ICourseUnitRepository.class);
    private final CourseServiceImpl service = new CourseServiceImpl(courseRepository, unitRepository, new CourseMapper());

    @Test
    void mapsCourseUnitsInSyllabusOrder() {
        Course course = new Course("middle-java", "Middle Java", "Course");
        CourseUnit first = new CourseUnit(course, 1, "SOLID", "Principles");
        when(courseRepository.existsById(10L)).thenReturn(true);
        when(unitRepository.findByCourseIdOrderByUnitNumber(10L)).thenReturn(List.of(first));

        List<UnitResponse> result = service.getUnits(10L);

        assertThat(result).containsExactly(new UnitResponse(null, null, 1, "SOLID", "Principles"));
    }

    @Test
    void failsWhenCourseDoesNotExist() {
        when(courseRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getUnits(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void returnsNotFoundForUnitOutsideCourse() {
        when(unitRepository.findByIdAndCourseId(4L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUnit(2L, 4L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void mapsCourseList() {
        when(courseRepository.findByActiveTrueOrderByIdAsc()).thenReturn(List.of(new Course("middle-java", "Middle Java", "Course")));

        List<CourseResponse> result = service.getCourses();

        assertThat(result).hasSize(1).first().extracting(CourseResponse::slug).isEqualTo("middle-java");
    }
}
