package vn.edu.middlejava.learning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.edu.middlejava.learning.course.repository.ICourseRepository;
import vn.edu.middlejava.learning.course.repository.ICourseUnitRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LearningServiceContextTest {
    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private ICourseUnitRepository unitRepository;

    @Test
    void startsAndSeedsTheCourseOutline() {
        Long courseId = courseRepository.findBySlug("middle-java").orElseThrow().getId();

        assertThat(unitRepository.findByCourseIdOrderByUnitNumber(courseId)).hasSize(14);
    }
}
