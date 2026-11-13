package vn.edu.middlejava.ai;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.DriverManager;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class PostgresVectorIntegrationTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:pg16"))
            .withDatabaseName("vector_test")
            .withUsername("test")
            .withPassword("test");

    @Test
    void storesAndRanksVectorsUsingPostgresVectorExtension() throws Exception {
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = connection.createStatement()) {
            statement.execute("CREATE EXTENSION IF NOT EXISTS vector");
            statement.execute("CREATE TABLE vector_probe(id BIGINT PRIMARY KEY, embedding VECTOR(3) NOT NULL)");
            try (var insert = connection.prepareStatement("INSERT INTO vector_probe(id, embedding) VALUES (?, CAST(? AS vector))")) {
                insert.setLong(1, 1L);
                insert.setString(2, "[1,0,0]");
                insert.executeUpdate();
            }
            try (var search = connection.prepareStatement("SELECT id FROM vector_probe ORDER BY embedding <=> CAST(? AS vector) LIMIT 1")) {
                search.setString(1, "[0.9,0.1,0]");
                try (var result = search.executeQuery()) {
                    assertThat(result.next()).isTrue();
                    assertThat(result.getLong(1)).isEqualTo(1L);
                }
            }
        }
    }
}
