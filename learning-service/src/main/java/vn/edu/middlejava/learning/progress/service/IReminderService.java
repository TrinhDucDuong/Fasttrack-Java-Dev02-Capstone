package vn.edu.middlejava.learning.progress.service;

import vn.edu.middlejava.learning.progress.dto.CreateReminderRequest;
import vn.edu.middlejava.learning.progress.dto.ReminderResponse;

import java.util.List;

public interface IReminderService {
    ReminderResponse create(Long userId, CreateReminderRequest request);
    List<ReminderResponse> getForUser(Long userId);
}
