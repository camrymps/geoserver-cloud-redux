/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.jackson.databind.catalog.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("WMSLayerInfo")
public class WMSLayer extends Resource {
    private String forcedRemoteStyle = "";
    private String preferredFormat;
    private Double minScale;
    private Double maxScale;
    private boolean metadataBBoxRespected;
    private List<String> selectedRemoteFormats;
    private List<String> selectedRemoteStyles;
}
