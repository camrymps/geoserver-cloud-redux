/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.locking;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.CoverageStoreInfo;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.Info;
import org.geoserver.catalog.LayerGroupInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.MapInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.WMTSLayerInfo;
import org.geoserver.catalog.WMTSStoreInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServerInfo;
import org.geoserver.config.LoggingInfo;
import org.geoserver.config.ServiceInfo;
import org.geoserver.config.SettingsInfo;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
enum ConfigInfoType {
    CATALOG(Catalog.class), //
    WORKSPACEINFO(WorkspaceInfo.class), //
    NAMESPACEINFO(NamespaceInfo.class), //
    COVERAGESTOREINFO(CoverageStoreInfo.class), //
    DATASTOREINFO(DataStoreInfo.class), //
    WMSSTOREINFO(WMSStoreInfo.class), //
    WMTSSTOREINFO(WMTSStoreInfo.class), //
    FEATURETYPEINFO(FeatureTypeInfo.class), //
    COVERAGEINFO(CoverageInfo.class), //
    WMSLAYERINFO(WMSLayerInfo.class), //
    WMTSLAYERINFO(WMTSLayerInfo.class), //
    LAYERINFO(LayerInfo.class), //
    LAYERGROUPINFO(LayerGroupInfo.class), //
    MAPINFO(MapInfo.class), //
    STYLEINFO(StyleInfo.class), //
    GEOSERVERINFO(GeoServerInfo.class), //
    SERVICEINFO(ServiceInfo.class), //
    SETTINGSINFO(SettingsInfo.class), //
    LOGGINGINFO(LoggingInfo.class); //

    private final @Getter @NonNull Class<? extends Info> type;

    public boolean isInstance(Info object) {
        return object != null && getType().isInstance(object);
    }

    public static ConfigInfoType valueOf(@NonNull Info object) {
        for (ConfigInfoType enumVal : ConfigInfoType.values()) {
            if (enumVal.isInstance(object)) {
                return enumVal;
            }
        }
        throw new IllegalArgumentException("Unknown info type for object %s".formatted(object));
    }

    public boolean isA(Class<? extends Info> type) {
        return type.isAssignableFrom(getType());
    }
}
