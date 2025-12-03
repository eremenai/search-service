package com.neviswealth.searchservice.persistence;

import com.neviswealth.searchservice.domain.Client;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ClientRepository {

    private static final RowMapper<Client> CLIENT_ROW_MAPPER = new ClientRowMapper();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ClientRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Client insert(Client client) {
        var params = new MapSqlParameterSource()
                .addValue("email", client.email())
                .addValue("email_domain", client.emailDomain())
                .addValue("email_domain_slug", client.emailDomainSlug())
                .addValue("first_name", client.firstName())
                .addValue("last_name", client.lastName())
                .addValue("full_name", client.fullName())
                .addValue("country_of_residence", client.countryOfResidence());

        String sql = """
                INSERT INTO clients (email, email_domain, email_domain_slug, first_name, last_name, full_name, country_of_residence)
                VALUES (:email, :email_domain, :email_domain_slug, :first_name, :last_name, :full_name, :country_of_residence)
                RETURNING id, email, email_domain, email_domain_slug, first_name, last_name, full_name, country_of_residence, created_at
                """;
        return jdbcTemplate.queryForObject(sql, params, CLIENT_ROW_MAPPER);
    }

    public Optional<Client> findById(UUID id) {
        String sql = """
                SELECT id, email, email_domain, email_domain_slug, first_name, last_name, full_name, country_of_residence, created_at
                FROM clients
                WHERE id = :id
                """;
        var params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.query(sql, params, CLIENT_ROW_MAPPER).stream().findFirst();
    }

    public boolean existsById(UUID id) {
        String sql = "SELECT EXISTS (SELECT 1 FROM clients WHERE id = :id)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), Boolean.class));
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT EXISTS (SELECT 1 FROM clients WHERE email = :email)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("email", email), Boolean.class));
    }

    public List<ClientSearchRow> searchByEmail(String query, int limit) {
        String sql = """
                SELECT *,
                       CASE WHEN email LIKE '%' || :query || '%' THEN 1 ELSE similarity(email, :query) END AS score
                FROM clients
                WHERE (
                    email LIKE '%' || :query || '%' OR
                    email % :query
                )
                ORDER BY score DESC
                LIMIT :limit
                """;
        var params = new MapSqlParameterSource()
                .addValue("query", query)
                .addValue("limit", limit);
        return jdbcTemplate.query(sql, params, new ClientSearchRowMapper());
    }

    public List<ClientSearchRow> searchByNameOrDomain(String normalizedQuery, String slugQuery, int limit) {
        String sql = """
                SELECT *,
                       CASE WHEN (full_name LIKE '%' || :normalizedQuery || '%' OR email LIKE '%' || :normalizedQuery || '%')
                       THEN 1
                       ELSE GREATEST(
                            similarity(full_name, :normalizedQuery),
                            similarity(full_name, :slugQuery),
                            similarity(email_domain_slug, :slugQuery),
                            similarity(email_domain, :slugQuery),
                            similarity(first_name, :normalizedQuery),
                            similarity(last_name, :normalizedQuery)
                       )
                       END AS score
                FROM clients
                WHERE  (
                    full_name LIKE '%' || :normalizedQuery || '%' OR
                    email LIKE '%' || :normalizedQuery || '%' OR
                    full_name % :normalizedQuery OR
                    full_name % :slugQuery OR
                    email_domain_slug % :slugQuery OR
                    email_domain % :slugQuery OR
                    first_name % :normalizedQuery OR
                    last_name % :normalizedQuery
                )
                ORDER BY score DESC
                LIMIT :limit
                """;
        var params = new MapSqlParameterSource()
                .addValue("normalizedQuery", normalizedQuery)
                .addValue("slugQuery", slugQuery)
                .addValue("limit", limit);
        return jdbcTemplate.query(sql, params, new ClientSearchRowMapper());
    }

    public List<Client> getAll() {
        return jdbcTemplate.query("SELECT * from clients", CLIENT_ROW_MAPPER);
    }

    private static class ClientRowMapper implements RowMapper<Client> {
        @Override
        public Client mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Client(
                    rs.getObject("id", UUID.class),
                    rs.getString("email"),
                    rs.getString("email_domain"),
                    rs.getString("email_domain_slug"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("full_name"),
                    rs.getString("country_of_residence"),
                    rs.getObject("created_at", OffsetDateTime.class)
            );
        }
    }

    private static class ClientSearchRowMapper implements RowMapper<ClientSearchRow> {
        @Override
        public ClientSearchRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            Client client = new Client(
                    rs.getObject("id", UUID.class),
                    rs.getString("email"),
                    rs.getString("email_domain"),
                    rs.getString("email_domain_slug"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("full_name"),
                    rs.getString("country_of_residence"),
                    rs.getObject("created_at", OffsetDateTime.class)
            );
            double score = rs.getDouble("score");
            return new ClientSearchRow(client, score);
        }
    }

    public record ClientSearchRow(Client client, double score) {
    }
}
