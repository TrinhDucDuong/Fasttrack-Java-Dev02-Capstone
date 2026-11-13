package vn.edu.middlejava.learning.progress.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.progress.dto.ProgressResponse;
import vn.edu.middlejava.learning.progress.service.IProgressService;
import vn.edu.middlejava.learning.quiz.entity.QuizAttempt;
import vn.edu.middlejava.learning.quiz.repository.IQuizAttemptRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProgressServiceImpl implements IProgressService {
    private final IQuizAttemptRepository attemptRepository;

    public ProgressServiceImpl(IQuizAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Override
    public ProgressResponse getForUser(Long userId) {
        List<QuizAttempt> attempts = attemptRepository.findByUserIdOrderByCompletedAtDesc(userId);
        int correct = attempts.stream().mapToInt(QuizAttempt::getScore).sum();
        int total = attempts.stream().mapToInt(QuizAttempt::getTotal).sum();
        double accuracy = total == 0 ? 0.0 : (double) correct / total * 100.0;
        List<ProgressResponse.AttemptSummary> recent = attempts.stream().limit(10)
                .map(attempt -> new ProgressResponse.AttemptSummary(attempt.getQuizId(), attempt.getScore(), attempt.getTotal(), attempt.getCompletedAt())).toList();
        return new ProgressResponse(userId, attempts.size(), correct, total, accuracy, recent);
    }
}
