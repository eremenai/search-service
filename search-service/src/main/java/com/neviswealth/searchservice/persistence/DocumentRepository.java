package com.neviswealth.searchservice.persistence;

import com.neviswealth.searchservice.config.SearchingProperties;
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

    private static final RowMapper<Document> FULL_DOCUMENT_ROW_MAPPER = new FullDocumentRowMapper();
    private static final RowMapper<Document> NO_CONTENT_DOCUMENT_ROW_MAPPER = new NoContentDocumentRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final double embeddingThreshold;
    private final double similarityThreshold;

    public DocumentRepository(NamedParameterJdbcTemplate jdbcTemplate, SearchingProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingThreshold = properties.getEmbedding();
        this.similarityThreshold = properties.getSimilarity();
    }

    public Document insert(Document document) {
        var params = new MapSqlParameterSource()
                .addValue("client_id", document.clientId())
                .addValue("title", document.title())
                .addValue("content", document.content())
                .addValue("content_hash", document.contentHash());

        String sql = """
                INSERT INTO documents (client_id, title, content, content_hash)
                VALUES (:client_id, :title, :content, :content_hash)
                RETURNING id, client_id, title, created_at
                """;
        return jdbcTemplate.queryForObject(sql, params, NO_CONTENT_DOCUMENT_ROW_MAPPER);
    }

    public Optional<Document> findById(UUID id) {
        String sql = "SELECT * FROM documents WHERE id = :id";
        var params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, params, FULL_DOCUMENT_ROW_MAPPER).stream().findFirst();
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

    public List<DocumentSearchRow> searchLexically(UUID clientId, String query, int limit) {
        String sql = """
                SELECT *
                FROM (
                    SELECT
                        d.id as id,
                        d.client_id,
                        d.title,
                        d.created_at,
                        dc.content AS chunk_content,
                        CASE WHEN dc.content ILIKE '%' || :q || '%' THEN 1 ELSE 0 END AS prefix_match,
                        CASE WHEN dc.content ILIKE '%' || :q || '%' THEN 1 ELSE similarity(dc.content, :q) END as score,
                        ROW_NUMBER() OVER (
                            PARTITION BY d.id
                            ORDER BY
                                CASE WHEN dc.content ILIKE '%' || :q || '%' THEN 1 ELSE 0 END DESC,
                                similarity(dc.content, :q) DESC
                        ) AS rn
                    FROM documents d
                    JOIN document_chunks dc ON dc.document_id = d.id
                    WHERE
                        (:clientId::uuid IS NULL OR d.client_id = :clientId)
                         AND
                        ((dc.content ILIKE '%' || :q || '%') OR (similarity(dc.content, :q) >= :threshold))
                ) ranked
                WHERE rn = 1
                ORDER BY
                    prefix_match DESC,
                    score DESC
                LIMIT :limit;
                """;
        var params = new MapSqlParameterSource()
                .addValue("q", query)
                .addValue("clientId", clientId, Types.OTHER)
                .addValue("limit", limit)
                .addValue("threshold", similarityThreshold);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new DocumentSearchRow(
                NO_CONTENT_DOCUMENT_ROW_MAPPER.mapRow(rs, rowNum),
                rs.getDouble("score"),
                rs.getString("chunk_content"),
                true
        ));
    }

    public List<DocumentSearchRow> searchWithEmbeddings(UUID clientId, float[] queryVector, int limit) {
        String sql = """
                SELECT id,
                       client_id,
                       title,
                       created_at,
                       chunk_content,
                       score
                FROM (
                         SELECT d.id AS id,
                                d.client_id,
                                d.title,
                                d.created_at,
                                dc.content AS chunk_content,
                                exp(-(dc.embedding <-> :queryVector)) AS score,
                                ROW_NUMBER() OVER (PARTITION BY d.id ORDER BY dc.embedding <-> :queryVector) AS rn
                         FROM document_chunks dc
                         JOIN documents d ON d.id = dc.document_id
                         WHERE :clientId::uuid IS NULL OR d.client_id = :clientId
                     ) ranked
                WHERE rn = 1 AND score >= :threshold
                ORDER BY score DESC
                LIMIT :limit
                """;
        var params = new MapSqlParameterSource()
                .addValue("clientId", clientId, Types.OTHER)
                .addValue("queryVector", new SqlParameterValue(Types.OTHER, new PGvector(queryVector)))
                .addValue("limit", limit)
                .addValue("threshold", embeddingThreshold);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new DocumentSearchRow(
                NO_CONTENT_DOCUMENT_ROW_MAPPER.mapRow(rs, rowNum),
                rs.getDouble("score"),
                rs.getString("chunk_content"),
                false
        ));
    }

    public List<Document> getAll() {
        return jdbcTemplate.query("Select * from documents", NO_CONTENT_DOCUMENT_ROW_MAPPER);
    }

    public void updateDocumentWithSummary(UUID id, String summary) {
        String query = "update documents set summary = :summary where id = :id";

        var params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("summary", summary);

        jdbcTemplate.update(query, params);
    }

    private static class FullDocumentRowMapper implements RowMapper<Document> {
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

    private static class NoContentDocumentRowMapper implements RowMapper<Document> {
        @Override
        public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Document(
                    rs.getObject("id", UUID.class),
                    rs.getObject("client_id", UUID.class),
                    rs.getString("title"),
                    null,
                    null,
                    null,
                    rs.getObject("created_at", java.time.OffsetDateTime.class)
            );
        }
    }

    public record DocumentSearchRow(Document document, double score, String matchedSnippet, boolean lexically) {
    }
}
