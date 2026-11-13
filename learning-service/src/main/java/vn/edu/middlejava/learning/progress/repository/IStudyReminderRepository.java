package vn.edu.middlejava.learning.progress.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.middlejava.learning.progress.entity.StudyReminder;

import java.time.Instant;
import java.util.List;

public interface IStudyReminderRepository extends JpaRepository<StudyReminder, Long> {
    List<StudyReminder> findByUserIdOrderByDueAtDesc(Long userId);

    @Query("select r from StudyReminder r where r.notifiedAt is null and r.dueAt <= :now order by r.dueAt")
    List<StudyReminder> findDue(@Param("now") Instant now);
}
