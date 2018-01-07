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

package org.openvpms.component.model.object;

/**
 * A reference to an {@link IMObject}.
 *
 * @author Tim Anderson
 */
public interface Reference {

    /**
     * Return the object's archetype.
     *
     * @return the archetype
     */
    String getArchetype();

    /**
     * Returns the object's persistent identifier.
     *
     * @return the object's persistent identifier, or {@code -1} if the object is not persistent.
     */
    long getId();

    /**
     * Returns the object link identifier.
     * <p>
     * This is a UUID that is used to link objects until they can be made
     * persistent, and to provide support for object equality.
     *
     * @return the link identifier. May be {@code null}
     */
    String getLinkId();

    /**
     * Determines if the object is new. A new object is one that has not been made persistent.
     *
     * @return {@code true} if the object is new, {@code false} if it has been made persistent
     */
    boolean isNew();

    /**
     * Determines if the reference is to an instance of a particular archetype.
     *
     * @param archetype the archetype short name. May contain wildcards
     * @return {@code true} if the object is an instance of {@code archetype}
     */
    boolean isA(String archetype);

    /**
     * Determines if the reference is to an object of one of a set of archetypes.
     *
     * @param archetypes the archetype short names. May contain wildcards
     * @return {@code true} if object is one of {@code archetypes}
     */
    boolean isA(String... archetypes);

    /**
     * Determines if this reference is equal to another object.
     * <p>
     * A reference is equal to another if the {@link #getArchetype() archetype} and {@link #getLinkId() linkId}
     * are the same.
     *
     * @param obj the object
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    boolean equals(Object obj);

    /**
     * Determines if an archetype and link identifier match this.
     *
     * @param archetype the archetype
     * @param linkId    the link identifier
     * @return {@code true} if they match, otherwise {@code false}
     */
    boolean equals(String archetype, String linkId);
}
