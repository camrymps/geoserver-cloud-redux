/*
 * (c) 2024 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.gwc.backend.pgconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NonNull;
import lombok.SneakyThrows;

import org.geoserver.catalog.PublishedInfo;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.CatalogInfoRowMapper;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigObjectMapper;
import org.geoserver.cloud.backend.pgconfig.catalog.repository.PgconfigStyleRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.UncheckedIOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @since 1.7
 */
class PgconfigTileLayerInfoRowMapper implements RowMapper<TileLayerInfo> {

    protected static final ObjectMapper objectMapper = PgconfigObjectMapper.newObjectMapper();

    private final RowMapper<PublishedInfo> publishedMapper;

    private PgconfigTileLayerInfoRowMapper(RowMapper<PublishedInfo> publishedMapper) {
        this.publishedMapper = publishedMapper;
    }

    public static PgconfigTileLayerInfoRowMapper newInstance(@NonNull JdbcTemplate template) {
        PgconfigStyleRepository styleLoader = new PgconfigStyleRepository(template);
        RowMapper<PublishedInfo> publishedMapper =
                CatalogInfoRowMapper.published(styleLoader::findById);
        return new PgconfigTileLayerInfoRowMapper(publishedMapper);
    }

    /**
     *
     *
     * <pre>{@code
     *      Column     |   Type   |
     * ----------------+----------+
     *  &#64;type      | infotype |
     *  name           | text     |
     *  enabled        | boolean  |
     *  advertised     | boolean  |
     *  type           | text     |
     *  workspace.name | text     |
     *  published.name | text     |
     *  workspace      | jsonb    |
     *  namespace      | jsonb    |
     *  store          | jsonb    |
     *  resource       | jsonb    |
     *  publishedinfo  | jsonb    |
     *  tilelayer      | jsonb    |
     *
     * }</pre>
     */
    @Override
    public TileLayerInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        TileLayerInfo tileLayerInfo;
        try {
            String tileInfoValue = rs.getString("tilelayer");
            tileLayerInfo = objectMapper.readValue(tileInfoValue, TileLayerInfo.class);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
        PublishedInfo publishedInfo = publishedMapper.mapRow(rs, rowNum);
        tileLayerInfo.setPublished(publishedInfo);
        return tileLayerInfo;
    }

    @SneakyThrows(JsonProcessingException.class)
    public static String encode(@NonNull TileLayerInfo info) {
        return objectMapper.writeValueAsString(info);
    }
}
