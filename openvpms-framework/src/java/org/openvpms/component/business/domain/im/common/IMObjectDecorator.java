/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.domain.im.common;

import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;

/**
 * Decorator for an {@link IMObject}.
 *
 * @author Tim Anderson
 */
public class IMObjectDecorator implements IMObject, Beanable{

    /**
     * The peer
     */
    private final IMObject peer;

    /**
     * Constructs an {@link IMObjectDecorator}.
     *
     * @param peer the peer to delegate to
     */
    public IMObjectDecorator(IMObject peer) {
        this.peer = peer;
    }

    /**
     * Returns the object's persistent identifier.
     *
     * @return the object identifier, or {@code -1} if the object has not been saved
     */
    @Override
    public long getId() {
        return peer.getId();
    }

    /**
     * Returns the object link identifier.
     * <p>
     * This is a UUID that is used to link objects until they can be made
     * persistent, and to provide support for object equality.
     *
     * @return the link identifier
     */
    @Override
    public String getLinkId() {
        return peer.getLinkId();
    }

    /**
     * Returns the archetype identifier.
     *
     * @return the archetype identifier.
     */
    @Override
    public String getArchetype() {
        return peer.getArchetype();
    }

    /**
     * Returns a reference for the object.
     *
     * @return the reference
     */
    @Override
    public Reference getObjectReference() {
        return peer.getObjectReference();
    }

    /**
     * Returns the name.
     *
     * @return the name. May be {@code null}
     */
    @Override
    public String getName() {
        return peer.getName();
    }

    /**
     * Sets the name.
     *
     * @param name the name. May be {@code null}
     */
    @Override
    public void setName(String name) {
        peer.setName(name);
    }

    /**
     * Returns the description.
     *
     * @return the description. May be {@code null}
     */
    @Override
    public String getDescription() {
        return peer.getDescription();
    }

    /**
     * Sets the description.
     *
     * @param description the description. May be {@code null}
     */
    @Override
    public void setDescription(String description) {
        peer.setDescription(description);
    }

    /**
     * Returns the object version.
     * <p>
     * This is updated when the object is made persistent.
     *
     * @return the version.
     */
    @Override
    public long getVersion() {
        return peer.getVersion();
    }

    /**
     * Determines if the object is active.
     *
     * @return {@code true} if the object is active, {@code false} if it is inactive.
     */
    @Override
    public boolean isActive() {
        return peer.isActive();
    }

    /**
     * Determines if the object is active.
     *
     * @param active if {@code true}, marks object active, otherwise marks it inactive
     */
    @Override
    public void setActive(boolean active) {
        peer.setActive(active);
    }

    /**
     * Determines if the object is new.
     * <br/>
     * A new object is one that has been created but not yet persisted.
     *
     * @return {@code true} if the object is new, {@code false} if it has been made persistent
     */
    @Override
    public boolean isNew() {
        return peer.isNew();
    }

    /**
     * Determines if this is an instance of a particular archetype.
     *
     * @param archetype the archetype short name. May contain wildcards
     * @return {@code true} if the object is an instance of {@code archetype}
     */
    @Override
    public boolean isA(String archetype) {
        return peer.isA(archetype);
    }

    /**
     * Determines if an object is one of a set of archetypes.
     *
     * @param archetypes the archetype short names. May contain wildcards
     * @return {@code true} if object is one of {@code archetypes}
     */
    @Override
    public boolean isA(String... archetypes) {
        return peer.isA(archetypes);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return getPeer().equals(obj);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return getPeer().hashCode();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return getPeer().toString();
    }

    /**
     * Returns an object suitable for use by {@link IMObjectBean}.
     *
     * @return the object
     * @throws IllegalStateException if an object cannot be resolved
     */
    @Override
    public org.openvpms.component.business.domain.im.common.IMObject getObject() {
        if (peer instanceof org.openvpms.component.business.domain.im.common.IMObject) {
            return (org.openvpms.component.business.domain.im.common.IMObject) peer;
        } else if (peer instanceof IMObjectDecorator) {
            return ((IMObjectDecorator) peer).getObject();
        }
        throw new IllegalStateException("Cannot resolve IMObject for " + peer.getObjectReference());
    }

    /**
     * Returns the peer.
     *
     * @return the peer
     */
    protected IMObject getPeer() {
        return peer;
    }
}
