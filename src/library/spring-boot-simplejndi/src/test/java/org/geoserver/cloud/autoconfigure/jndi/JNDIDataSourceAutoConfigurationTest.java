/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.autoconfigure.jndi;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;

import org.geoserver.cloud.config.jndi.JNDIDataSourceConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import jakarta.naming.Context;
import jakarta.naming.spi.NamingManager;

/**
 * @since 1.0
 */
class JNDIDataSourceAutoConfigurationTest {

    private ApplicationContextRunner runner =
            new ApplicationContextRunner()
                    .withInitializer(new SimpleJNDIStaticContextInitializer())
                    .withConfiguration(AutoConfigurations.of(JNDIDataSourceConfiguration.class));

    @Test
    void testInitialContextLookup() {

        runner.withPropertyValues( //
                        "jndi.datasources.ds1.url: jdbc:h2:mem:ds1", //
                        "jndi.datasources.ds1.username: sa", //
                        "jndi.datasources.ds1.password: sa", //
                        "jndi.datasources.ds1.connection-timeout: 250", //
                        "jndi.datasources.ds1.idle-timeout: 60000" //
                        )
                .run(
                        context -> {
                            Context initialContext = NamingManager.getInitialContext(null);
                            Object object = initialContext.lookup("java:comp/env/jdbc/ds1");
                            assertThat(object).isInstanceOf(HikariDataSource.class);
                        });
    }
}
