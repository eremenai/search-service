package com.neviswealth.searchservice.persistence;

import com.neviswealth.searchservice.domain.Document;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DocumentRepository {

    private static final RowMapper<Document> DOCUMENT_ROW_MAPPER = new DocumentRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DocumentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Document insert(Document document) {
        var params = new MapSqlParameterSource()
                .addValue("client_id", document.clientId())
                .addValue("title", document.title())
                .addValue("content", document.content())
                .addValue("content_hash", document.contentHash())
                .addValue("summary", document.summary());

        String sql = """
                INSERT INTO documents (client_id, title, content, content_hash, summary)
                VALUES (:client_id, :title, :content, :content_hash, :summary)
                RETURNING id, client_id, title, content, content_hash, summary, created_at
                """;
        return jdbcTemplate.queryForObject(sql, params, DOCUMENT_ROW_MAPPER);
    }

    public Optional<Document> findById(UUID id) {
        String sql = """
                SELECT id, client_id, title, content, content_hash, summary, created_at
                FROM documents
                WHERE id = :id
                """;
        var params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, params, DOCUMENT_ROW_MAPPER).stream().findFirst();
    }

    public boolean existsByClientIdAndTitle(UUID clientId, String title) {
        String sql = "SELECT EXISTS (SELECT 1 FROM documents WHERE client_id = :client_id AND title = :title)";
        var params = new MapSqlParameterSource()
                .addValue("client_id", clientId)
                .addValue("title", title);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    private static class DocumentRowMapper implements RowMapper<Document> {
        @Override
        public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Document(
                    rs.getObject("id", UUID.class),
                    rs.getObject("client_id", UUID.class),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getString("content_hash"),
                    rs.getString("summary"),
                    rs.getObject("created_at", java.time.OffsetDateTime.class)
            );
        }
    }
}
