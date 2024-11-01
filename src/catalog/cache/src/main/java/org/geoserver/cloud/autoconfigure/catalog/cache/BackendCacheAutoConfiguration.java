/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.catalog.cache;

import org.geoserver.cloud.catalog.cache.GeoServerBackendCacheConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

/**
 * {@link EnableAutoConfiguration @EnableAutoConfiguration} auto configuration for geoserver's
 * catalog back-end caching using spring {@link CacheManager}.
 *
 * <p>Caching for the geoserver backend is enabled conditionally on property {@code
 * geoserver.catalog.caching.enabled=true}, defaults to {@code false}.
 *
 * @see GeoServerBackendCacheConfiguration
 */
@AutoConfiguration
@ConditionalOnBackendCacheEnabled
@Import(GeoServerBackendCacheConfiguration.class)
public class BackendCacheAutoConfiguration {}
