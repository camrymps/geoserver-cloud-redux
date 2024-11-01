package org.geoserver.cloud.observability.logging.servlet;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "logging.mdc.include.application")
public class SpringEnvironmentMdcConfigProperties {

    private boolean name = true;
    private boolean version = true;
    private boolean instanceId = true;
    private List<String> instanceIdProperties =
            List.of("info.instance-id", "spring.application.instance_id");
    private boolean activeProfiles = true;
}
