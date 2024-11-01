/*
 * (c) 2020 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;

import org.geoserver.cloud.event.info.InfoEvent;
import org.geoserver.cloud.event.security.SecurityConfigChanged;
import org.geoserver.config.GeoServerInfo;
import org.springframework.core.style.ToStringCreator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
    @JsonSubTypes.Type(value = InfoEvent.class),
    @JsonSubTypes.Type(value = SecurityConfigChanged.class)
})
@JsonTypeName("UpdateSequence")
@SuppressWarnings("serial")
public class UpdateSequenceEvent extends GeoServerEvent implements Comparable<UpdateSequenceEvent> {
    /**
     * The provided {@link GeoServerInfo}'s {@link GeoServerInfo#getUpdateSequence() update
     * sequence}. Being the most frequently updated property, it's readily available for remote
     * listeners even when the {@link #getPatch() patch} is not sent over the wire.
     */
    private @Getter long updateSequence;

    protected UpdateSequenceEvent() {
        // no-op default constructor for deserialization
    }

    protected UpdateSequenceEvent(long updateSequence) {
        super(System.currentTimeMillis(), resolveAuthor());
        this.updateSequence = updateSequence;
    }

    protected @Override ToStringCreator toStringBuilder() {
        return super.toStringBuilder().append("updateSequence", updateSequence);
    }

    private static String resolveAuthor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return null == authentication ? null : authentication.getName();
    }

    public static UpdateSequenceEvent createLocal(long value) {
        return new UpdateSequenceEvent(value);
    }

    @SuppressWarnings("java:S1210")
    @Override
    public int compareTo(UpdateSequenceEvent o) {
        return Long.compare(getUpdateSequence(), o.getUpdateSequence());
    }

    @Override
    public String toShortString() {
        String originService = getOrigin();
        String type = getClass().getSimpleName();
        return "%s[origin: %s, updateSequence: %s]"
                .formatted(type, originService, getUpdateSequence());
    }
}
