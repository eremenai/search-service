package com.neviswealth.searchservice.persistence;

import com.neviswealth.searchservice.domain.DocumentChunk;
import com.pgvector.PGvector;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

@Repository
public class DocumentChunkRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public DocumentChunkRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
                       (dc.embedding <-> :queryVector) AS distance
                FROM document_chunks dc
                JOIN documents d ON d.id = dc.document_id
                WHERE d.client_id = :clientId
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
                rs.getDouble("distance")
        ));
    }

    public record DocumentChunkSearchRow(
            UUID documentId,
            UUID clientId,
            String title,
            int chunkIndex,
            String chunkContent,
            double distance
    ) {
    }
}
