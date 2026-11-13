package vn.edu.middlejava.learning.quiz.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.middlejava.learning.course.repository.ICourseUnitRepository;
import vn.edu.middlejava.learning.exception.ResourceNotFoundException;
import vn.edu.middlejava.learning.quiz.dto.AttemptResponse;
import vn.edu.middlejava.learning.quiz.dto.CreateQuizRequest;
import vn.edu.middlejava.learning.quiz.dto.GenerateQuizRequest;
import vn.edu.middlejava.learning.quiz.dto.QuizResponse;
import vn.edu.middlejava.learning.quiz.dto.SubmitAttemptRequest;
import vn.edu.middlejava.learning.quiz.entity.Quiz;
import vn.edu.middlejava.learning.quiz.entity.QuizAttempt;
import vn.edu.middlejava.learning.quiz.entity.QuizOption;
import vn.edu.middlejava.learning.quiz.entity.QuizQuestion;
import vn.edu.middlejava.learning.quiz.mapper.QuizMapper;
import vn.edu.middlejava.learning.quiz.repository.IQuizAttemptRepository;
import vn.edu.middlejava.learning.quiz.repository.IQuizRepository;
import vn.edu.middlejava.learning.quiz.client.IAiQuizClient;
import vn.edu.middlejava.learning.quiz.service.IQuizService;

import java.util.List;
import java.util.Map;

@Service
public class QuizServiceImpl implements IQuizService {
    private final IQuizRepository quizRepository;
    private final IQuizAttemptRepository attemptRepository;
    private final ICourseUnitRepository unitRepository;
    private final QuizMapper quizMapper;
    private final IAiQuizClient aiQuizClient;

    public QuizServiceImpl(IQuizRepository quizRepository, IQuizAttemptRepository attemptRepository,
                          ICourseUnitRepository unitRepository, QuizMapper quizMapper, IAiQuizClient aiQuizClient) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
        this.unitRepository = unitRepository;
        this.quizMapper = quizMapper;
        this.aiQuizClient = aiQuizClient;
    }

    @Override
    @Transactional
    public QuizResponse create(CreateQuizRequest request) {
        if (!unitRepository.existsById(request.unitId())) {
            throw new ResourceNotFoundException("Unit not found");
        }
        List<QuizQuestion> questions = java.util.stream.IntStream.range(0, request.questions().size())
                .mapToObj(index -> toQuestion(request.questions().get(index), index + 1)).toList();
        Quiz quiz = quizRepository.save(new Quiz(request.unitId(), request.title().trim(), questions));
        return quizMapper.toResponse(quizRepository.findDetailedById(quiz.getId()).orElseThrow());
    }

    @Override
    public QuizResponse generate(GenerateQuizRequest request, String bearerToken) {
        if (!unitRepository.existsById(request.unitId())) {
            throw new ResourceNotFoundException("Unit not found");
        }
        List<CreateQuizRequest.QuestionRequest> questions = aiQuizClient.generate(request.unitId(), request.questionCount(), bearerToken)
                .stream().map(question -> new CreateQuizRequest.QuestionRequest(question.prompt(), question.explanation(),
                        question.options().stream().map(option -> new CreateQuizRequest.OptionRequest(option.text(), option.correct())).toList()))
                .toList();
        return create(new CreateQuizRequest(request.unitId(), "AI practice quiz", questions));
    }

    @Override
    @Transactional(readOnly = true)
    public QuizResponse get(Long quizId) {
        return quizRepository.findDetailedById(quizId).map(quizMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponse> getForUnit(Long unitId) {
        if (!unitRepository.existsById(unitId)) {
            throw new ResourceNotFoundException("Unit not found");
        }
        return quizRepository.findDetailedByUnitIdOrderByCreatedAtDesc(unitId).stream().map(quizMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AttemptResponse submit(Long userId, Long quizId, SubmitAttemptRequest request) {
        Quiz quiz = quizRepository.findDetailedById(quizId).orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        Map<Long, Long> answers = request.answers();
        if (answers.size() != quiz.getQuestions().size()) {
            throw new IllegalArgumentException("Submit one answer for every question");
        }
        int score = 0;
        for (QuizQuestion question : quiz.getQuestions()) {
            Long selectedOptionId = answers.get(question.getId());
            QuizOption selected = question.getOptions().stream()
                    .filter(option -> option.getId().equals(selectedOptionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("An answer does not belong to its question"));
            if (selected.isCorrect()) {
                score++;
            }
        }
        QuizAttempt attempt = attemptRepository.save(new QuizAttempt(userId, quizId, score, quiz.getQuestions().size()));
        return new AttemptResponse(attempt.getId(), quizId, score, attempt.getTotal(),
                attempt.getTotal() == 0 ? 0.0 : (double) score / attempt.getTotal() * 100.0, attempt.getCompletedAt());
    }

    private QuizQuestion toQuestion(CreateQuizRequest.QuestionRequest request, int order) {
        long correctCount = request.options().stream().filter(CreateQuizRequest.OptionRequest::correct).count();
        if (correctCount != 1) {
            throw new IllegalArgumentException("Each question must have exactly one correct option");
        }
        List<QuizOption> options = request.options().stream().map(option -> new QuizOption(option.text().trim(), option.correct())).toList();
        return new QuizQuestion(request.prompt().trim(), request.explanation().trim(), order, options);
    }
}
