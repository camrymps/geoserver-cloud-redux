/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.catalog.plugin.forwarding;

import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.plugin.CatalogInfoRepository.StyleRepository;

import java.util.Optional;
import java.util.stream.Stream;

public class ForwardingStyleRepository
        extends ForwardingCatalogRepository<StyleInfo, StyleRepository> implements StyleRepository {

    public ForwardingStyleRepository(StyleRepository subject) {
        super(subject);
    }

    @Override
    public Stream<StyleInfo> findAllByNullWorkspace() {
        return subject.findAllByNullWorkspace();
    }

    @Override
    public Stream<StyleInfo> findAllByWorkspace(WorkspaceInfo ws) {
        return subject.findAllByWorkspace(ws);
    }

    @Override
    public Optional<StyleInfo> findByNameAndWordkspaceNull(String name) {
        return subject.findByNameAndWordkspaceNull(name);
    }

    @Override
    public Optional<StyleInfo> findByNameAndWorkspace(String name, WorkspaceInfo workspace) {
        return subject.findByNameAndWorkspace(name, workspace);
    }
}
