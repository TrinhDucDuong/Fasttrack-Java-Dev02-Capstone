package vn.edu.middlejava.ai.chat.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import vn.edu.middlejava.ai.chat.dto.AnswerResponse;
import vn.edu.middlejava.ai.chat.dto.AskRequest;
import vn.edu.middlejava.ai.chat.dto.ConversationResponse;
import vn.edu.middlejava.ai.chat.dto.SourceResponse;
import vn.edu.middlejava.ai.chat.repository.VectorSearchRepository;
import vn.edu.middlejava.ai.chat.repository.ConversationRepository;
import vn.edu.middlejava.ai.chat.service.IChatService;
import vn.edu.middlejava.ai.config.AiProperties;
import vn.edu.middlejava.ai.provider.IChatProvider;
import vn.edu.middlejava.ai.provider.IEmbeddingProvider;

import java.util.List;

@Service
public class ChatServiceImpl implements IChatService {
    private final IEmbeddingProvider embeddingProvider;
    private final IChatProvider chatProvider;
    private final VectorSearchRepository vectorSearchRepository;
    private final AiProperties properties;
    private final MeterRegistry meterRegistry;
    private final ConversationRepository conversationRepository;

    public ChatServiceImpl(IEmbeddingProvider embeddingProvider, IChatProvider chatProvider,
                           VectorSearchRepository vectorSearchRepository, AiProperties properties, MeterRegistry meterRegistry,
                           ConversationRepository conversationRepository) {
        this.embeddingProvider = embeddingProvider;
        this.chatProvider = chatProvider;
        this.vectorSearchRepository = vectorSearchRepository;
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public AnswerResponse ask(Long userId, AskRequest request) {
        long started = System.nanoTime();
        Long conversationId = request.conversationId();
        if (conversationId == null) {
            conversationId = conversationRepository.create(userId, request.unitId());
        } else if (!conversationRepository.belongsToUser(conversationId, userId, request.unitId())) {
            throw new IllegalArgumentException("Conversation not found for this user and Unit");
        }
        conversationRepository.addMessage(conversationId, "USER", request.question(), null);
        List<Double> queryEmbedding = embeddingProvider.embed(request.question());
        List<SourceResponse> sources = vectorSearchRepository.findRelevantChunks(request.unitId(), toVectorLiteral(queryEmbedding), properties.getRetrievalLimit());
        List<String> context = sources.stream().map(source -> "[" + source.title() + ", chunk " + source.chunkIndex() + "] " + source.excerpt()).toList();
        String answer = sources.isEmpty() || sources.get(0).score() < 0.15
                ? "I could not find sufficiently relevant course material for this question."
                : chatProvider.answer(request.question(), context);
        conversationRepository.addMessage(conversationId, "ASSISTANT", answer, chatProvider.modelName());
        meterRegistry.counter("studycoach.ai.questions", "model", chatProvider.modelName()).increment();
        meterRegistry.timer("studycoach.ai.response", "model", chatProvider.modelName()).record(System.nanoTime() - started, java.util.concurrent.TimeUnit.NANOSECONDS);
        return new AnswerResponse(conversationId, answer, chatProvider.modelName(), sources);
    }

    @Override
    public ConversationResponse getConversation(Long userId, Long conversationId) {
        ConversationRepository.ConversationView conversation = conversationRepository.findForUser(conversationId, userId);
        if (conversation == null) {
            throw new IllegalArgumentException("Conversation not found");
        }
        List<ConversationResponse.Message> messages = conversationRepository.findMessages(conversationId).stream()
                .map(message -> new ConversationResponse.Message(message.role(), message.content(), message.model(), message.createdAt())).toList();
        return new ConversationResponse(conversation.id(), conversation.unitId(), conversation.createdAt(), messages);
    }

    private String toVectorLiteral(List<Double> vector) {
        if (vector.size() != properties.getEmbeddingDimensions()) {
            throw new IllegalArgumentException("Embedding dimension does not match configured dimension");
        }
        return vector.toString().replace(" ", "");
    }
}
