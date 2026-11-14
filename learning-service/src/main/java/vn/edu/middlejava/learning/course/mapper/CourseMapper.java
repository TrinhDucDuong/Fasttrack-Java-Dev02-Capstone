package vn.edu.middlejava.learning.course.mapper;

import org.springframework.stereotype.Component;
import vn.edu.middlejava.learning.course.dto.CourseAdminResponse;
import vn.edu.middlejava.learning.course.dto.CourseResponse;
import vn.edu.middlejava.learning.course.dto.CourseVideoResponse;
import vn.edu.middlejava.learning.course.dto.UnitResponse;
import vn.edu.middlejava.learning.course.entity.Course;
import vn.edu.middlejava.learning.course.entity.CourseUnit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CourseMapper {
    private static final Pattern YOUTUBE_VIDEO_ID = Pattern.compile("(?:youtu\\.be/|[?&]v=)([A-Za-z0-9_-]{6,})");

    public CourseResponse toResponse(Course course) {
        return new CourseResponse(course.getId(), course.getSlug(), course.getTitle(), course.getDescription(),
                course.getPrice());
    }

    public CourseAdminResponse toAdminResponse(Course course) {
        return new CourseAdminResponse(course.getId(), course.getSlug(), course.getTitle(),
                course.getDescription(), course.getPrice(), course.getYoutubeUrl());
    }

    public CourseVideoResponse toVideoResponse(Course course) {
        return new CourseVideoResponse(course.getId(), course.getTitle(), toEmbedUrl(course.getYoutubeUrl()));
    }

    private String toEmbedUrl(String youtubeUrl) {
        if (youtubeUrl == null) {
            return null;
        }
        Matcher matcher = YOUTUBE_VIDEO_ID.matcher(youtubeUrl);
        return matcher.find() ? "https://www.youtube.com/embed/" + matcher.group(1) : null;
    }

    public UnitResponse toResponse(CourseUnit unit) {
        return new UnitResponse(unit.getId(), unit.getCourse().getId(), unit.getUnitNumber(), unit.getTitle(), unit.getOutline());
    }
}
