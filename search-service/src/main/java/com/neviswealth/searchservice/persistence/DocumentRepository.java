package com.neviswealth.searchservice.persistence;

import com.neviswealth.searchservice.domain.Document;
import com.neviswealth.searchservice.domain.DocumentChunk;
import com.pgvector.PGvector;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
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

    public void insertChunks(UUID documentId, List<DocumentChunk> chunks) {
        String sql = """
                INSERT INTO document_chunks (document_id, chunk_index, content, embedding)
                VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DocumentChunk chunk = chunks.get(i);
                ps.setObject(1, documentId);
                ps.setInt(2, chunk.chunkIndex());
                ps.setString(3, chunk.content());
                ps.setObject(4, new PGvector(chunk.embedding()));
            }

            @Override
            public int getBatchSize() {
                return chunks.size();
            }
        });
    }

    public List<DocumentChunkSearchRow> searchChunks(UUID clientId, float[] queryVector, int limit) {
        String sql = """
                SELECT dc.document_id,
                       dc.chunk_index,
                       dc.content,
                       d.client_id,
                       d.title,
                       (dc.embedding <-> :queryVector) AS distance,
                       d.created_at
                FROM document_chunks dc
                JOIN documents d ON d.id = dc.document_id
                WHERE (:clientId IS NULL OR d.client_id = :clientId)
                ORDER BY dc.embedding <-> :queryVector
                LIMIT :limit
                """;
        var params = new MapSqlParameterSource()
                .addValue("clientId", clientId)
                .addValue("queryVector", new SqlParameterValue(Types.OTHER, new PGvector(queryVector)))
                .addValue("limit", limit);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new DocumentChunkSearchRow(
                rs.getObject("document_id", UUID.class),
                rs.getObject("client_id", UUID.class),
                rs.getString("title"),
                rs.getInt("chunk_index"),
                rs.getString("content"),
                rs.getDouble("distance"),
                rs.getObject("created_at", java.time.OffsetDateTime.class)
        ));
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

    public record DocumentChunkSearchRow(
            UUID documentId,
            UUID clientId,
            String title,
            int chunkIndex,
            String chunkContent,
            double distance,
            java.time.OffsetDateTime createdAt
    ) {
    }
}
