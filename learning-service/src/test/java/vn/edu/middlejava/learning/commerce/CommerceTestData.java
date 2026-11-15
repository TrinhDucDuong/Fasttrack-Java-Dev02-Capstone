package vn.edu.middlejava.learning.commerce;

import vn.edu.middlejava.learning.course.entity.Course;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Shared test fixtures for the commerce unit tests. */
public final class CommerceTestData {
    private CommerceTestData() {
    }

    public static Course course(long id, String title, String price) {
        return course(id, title, price, true);
    }

    public static Course course(long id, String title, String price, boolean active) {
        Course course = mock(Course.class);
        when(course.getId()).thenReturn(id);
        when(course.getTitle()).thenReturn(title);
        when(course.getPrice()).thenReturn(new BigDecimal(price));
        when(course.isActive()).thenReturn(active);
        return course;
    }
}
