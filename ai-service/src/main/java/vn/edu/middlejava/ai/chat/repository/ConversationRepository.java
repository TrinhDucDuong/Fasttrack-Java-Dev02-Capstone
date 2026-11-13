package vn.edu.middlejava.ai.chat.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class ConversationRepository {
    private final JdbcTemplate jdbcTemplate;

    public ConversationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long create(Long userId, Long unitId) {
        return jdbcTemplate.queryForObject("INSERT INTO conversations(user_id, unit_id, created_at) VALUES (?, ?, ?) RETURNING id",
                Long.class, userId, unitId, Timestamp.from(Instant.now()));
    }

    public boolean belongsToUser(Long conversationId, Long userId, Long unitId) {
        Boolean exists = jdbcTemplate.queryForObject("SELECT EXISTS(SELECT 1 FROM conversations WHERE id = ? AND user_id = ? AND unit_id = ?)",
                Boolean.class, conversationId, userId, unitId);
        return Boolean.TRUE.equals(exists);
    }

    public void addMessage(Long conversationId, String role, String content, String model) {
        jdbcTemplate.update("INSERT INTO conversation_messages(conversation_id, role, content, model, created_at) VALUES (?, ?, ?, ?, ?)",
                conversationId, role, content, model, Timestamp.from(Instant.now()));
    }

    public ConversationView findForUser(Long conversationId, Long userId) {
        return jdbcTemplate.query("SELECT id, unit_id, created_at FROM conversations WHERE id = ? AND user_id = ?",
                rs -> rs.next() ? new ConversationView(rs.getLong("id"), rs.getLong("unit_id"), rs.getTimestamp("created_at").toInstant()) : null,
                conversationId, userId);
    }

    public List<MessageView> findMessages(Long conversationId) {
        return jdbcTemplate.query("SELECT role, content, model, created_at FROM conversation_messages WHERE conversation_id = ? ORDER BY created_at, id",
                (rs, rowNumber) -> new MessageView(rs.getString("role"), rs.getString("content"), rs.getString("model"), rs.getTimestamp("created_at").toInstant()),
                conversationId);
    }

    public record ConversationView(Long id, Long unitId, Instant createdAt) {
    }

    public record MessageView(String role, String content, String model, Instant createdAt) {
    }
}
