package vn.edu.middlejava.learning.progress.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import java.util.List;
import vn.edu.middlejava.learning.progress.dto.CreateReminderRequest;
import vn.edu.middlejava.learning.progress.dto.ReminderResponse;
import vn.edu.middlejava.learning.progress.service.IReminderService;
import vn.edu.middlejava.learning.progress.dto.ProgressResponse;
import vn.edu.middlejava.learning.progress.service.IProgressService;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {
    private final IProgressService progressService;
    private final IReminderService reminderService;

    public ProgressController(IProgressService progressService, IReminderService reminderService) {
        this.progressService = progressService;
        this.reminderService = reminderService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProgressResponse> getMyProgress(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(progressService.getForUser(Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/reminders")
    public ResponseEntity<List<ReminderResponse>> getMyReminders(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(reminderService.getForUser(Long.valueOf(jwt.getSubject())));
    }

    @PostMapping("/reminders")
    public ResponseEntity<ReminderResponse> createReminder(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateReminderRequest request) {
        return ResponseEntity.status(201).body(reminderService.create(Long.valueOf(jwt.getSubject()), request));
    }
}
