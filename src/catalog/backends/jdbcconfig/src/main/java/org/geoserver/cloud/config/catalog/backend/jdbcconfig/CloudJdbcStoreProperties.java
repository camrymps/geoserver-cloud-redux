/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.config.catalog.backend.jdbcconfig;

import dev.mccue.guava.base.Optional;
import dev.mccue.guava.base.Preconditions;

import lombok.EqualsAndHashCode;

import org.geoserver.jdbcloader.DataSourceFactoryBean;
import org.geoserver.jdbcstore.internal.JDBCResourceStoreProperties;
import org.geoserver.jdbcstore.internal.JDBCResourceStorePropertiesFactoryBean;
import org.geoserver.platform.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Extends {@link JDBCResourceStoreProperties} to not need a {@link
 * JDBCResourceStorePropertiesFactoryBean}
 */
@EqualsAndHashCode(callSuper = true)
public class CloudJdbcStoreProperties extends JDBCResourceStoreProperties {
    private static final long serialVersionUID = 1L;

    private transient DataSource dataSource;

    public CloudJdbcStoreProperties(DataSource dataSource) {
        super((JDBCResourceStorePropertiesFactoryBean) null);
        this.dataSource = dataSource;
    }

    public File getCacheDirectory() {
        String defaultCacheDirectory = defaultCacheDirectory();
        String location = super.getProperty("cache-directory", defaultCacheDirectory);
        File cacheDirectory = new File(location);
        cacheDirectory.mkdirs();
        return cacheDirectory;
    }

    private static String defaultCacheDirectory() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        return "%s%sgeoserver-jdbcconfig-cache".formatted(tmpdir, File.separator);
    }

    /**
     * Override to not save at all, the canonical source of config settings is the spring boot
     * configuration properties
     */
    @Override
    public void save() throws IOException {
        // no-op
    }

    /** Override to return {@code true} only if the db schema is not already created */
    @Override
    public boolean isInitDb() {
        boolean initDb = Boolean.parseBoolean(getProperty("initdb", "false"));
        if (initDb) {
            try (Connection c = dataSource.getConnection();
                    Statement st = c.createStatement()) {
                if (dbSchemaExists(st)) {
                    initDb = false;
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
        return initDb;
    }

    private boolean dbSchemaExists(Statement st) {
        try (ResultSet rs = st.executeQuery("select count(*) from resources")) {
            return true;
        } catch (SQLException e) {
            // table not found, proceed with initialization
            return false;
        }
    }

    /**
     * Override to get the init script directly from the ones in the classpath (inside
     * gs-jdbcconfig.jar)
     */
    @Override
    public Resource getInitScript() {
        final String driverClassName = getProperty("datasource.driverClassname");
        String scriptName;
        switch (driverClassName) {
            case "org.h2.Driver":
                scriptName = "init.h2.sql";
                break;
            case "org.postgresql.Driver":
                scriptName = "init.postgres.sql";
                break;
            default:
                scriptName = null;
        }
        if (scriptName == null) {
            return null;
        }
        URL initScript = JDBCResourceStoreProperties.class.getResource(scriptName);
        Preconditions.checkState(
                initScript != null,
                "Init script does not exist: %s/%s",
                JDBCResourceStoreProperties.class.getPackage().getName(),
                scriptName);

        return org.geoserver.platform.resource.URIs.asResource(initScript);
    }

    /**
     * Override to throw an {@link UnsupportedOperationException}, we're not using {@link
     * DataSourceFactoryBean}, the datasource is provided by spring instead
     */
    @Override
    public Optional<String> getJdbcUrl() {
        throw new UnsupportedOperationException(
                "shouldn't be called, this module doesn't use org.geoserver.jdbcloader.DataSourceFactoryBean");
    }
}
