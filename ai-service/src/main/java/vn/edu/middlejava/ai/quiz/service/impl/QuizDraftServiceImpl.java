package vn.edu.middlejava.ai.quiz.service.impl;

import org.springframework.stereotype.Service;
import vn.edu.middlejava.ai.chat.repository.VectorSearchRepository;
import vn.edu.middlejava.ai.config.AiProperties;
import vn.edu.middlejava.ai.provider.IChatProvider;
import vn.edu.middlejava.ai.provider.IEmbeddingProvider;
import vn.edu.middlejava.ai.provider.QuizDraftQuestion;
import vn.edu.middlejava.ai.quiz.dto.QuizDraftRequest;
import vn.edu.middlejava.ai.quiz.dto.QuizDraftResponse;
import vn.edu.middlejava.ai.quiz.service.IQuizDraftService;

import java.util.List;

@Service
public class QuizDraftServiceImpl implements IQuizDraftService {
    private final IEmbeddingProvider embeddingProvider;
    private final IChatProvider chatProvider;
    private final VectorSearchRepository vectorSearchRepository;
    private final AiProperties properties;

    public QuizDraftServiceImpl(IEmbeddingProvider embeddingProvider, IChatProvider chatProvider,
                                VectorSearchRepository vectorSearchRepository, AiProperties properties) {
        this.embeddingProvider = embeddingProvider;
        this.chatProvider = chatProvider;
        this.vectorSearchRepository = vectorSearchRepository;
        this.properties = properties;
    }

    @Override
    public QuizDraftResponse generate(QuizDraftRequest request) {
        List<Double> embedding = embeddingProvider.embed("Core concepts and learning outcomes for this Unit");
        String vector = embedding.toString().replace(" ", "");
        List<String> context = vectorSearchRepository.findRelevantChunks(request.unitId(), vector, properties.getRetrievalLimit())
                .stream().map(source -> "[" + source.title() + "] " + source.excerpt()).toList();
        List<QuizDraftQuestion> questions = chatProvider.generateQuiz(context, request.questionCount());
        if (questions.size() != request.questionCount()) {
            throw new IllegalStateException("AI provider returned an unexpected number of questions");
        }
        return new QuizDraftResponse(questions.stream().map(question -> new QuizDraftResponse.Question(
                question.prompt(), question.explanation(), question.options().stream()
                .map(option -> new QuizDraftResponse.Option(option.text(), option.correct())).toList())).toList());
    }
}
