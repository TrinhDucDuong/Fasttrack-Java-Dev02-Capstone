package vn.edu.middlejava.learning.progress.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.course.repository.ICourseUnitRepository;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;
import vn.edu.middlejava.learning.progress.dto.CreateReminderRequest;
import vn.edu.middlejava.learning.progress.dto.ReminderResponse;
import vn.edu.middlejava.learning.progress.entity.StudyReminder;
import vn.edu.middlejava.learning.progress.repository.IStudyReminderRepository;
import vn.edu.middlejava.learning.progress.service.IReminderService;

import java.util.List;

@Service
public class ReminderServiceImpl implements IReminderService {
    private final IStudyReminderRepository reminderRepository;
    private final ICourseUnitRepository unitRepository;

    public ReminderServiceImpl(IStudyReminderRepository reminderRepository, ICourseUnitRepository unitRepository) {
        this.reminderRepository = reminderRepository;
        this.unitRepository = unitRepository;
    }

    @Override
    @Transactional
    public ReminderResponse create(Long userId, CreateReminderRequest request) {
        if (!unitRepository.existsById(request.unitId())) {
            throw new ResourceNotFoundException("Unit not found");
        }
        return toResponse(reminderRepository.save(new StudyReminder(userId, request.unitId(), request.dueAt())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getForUser(Long userId) {
        return reminderRepository.findByUserIdOrderByDueAtDesc(userId).stream().map(this::toResponse).toList();
    }

    private ReminderResponse toResponse(StudyReminder reminder) {
        return new ReminderResponse(reminder.getId(), reminder.getUnitId(), reminder.getDueAt(), reminder.getNotifiedAt());
    }
}
