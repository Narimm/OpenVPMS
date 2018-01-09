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
 * This is the base class for information model objects. An {@link IMObject}
 * object is very generic and is constrained at runtime by applying constriants
 * on the object. These constraints are the foundation of archetypes and
 * archetype languages such as ADL
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface IMObject {

    /**
     * Returns the object's persistent identifier.
     *
     * @return the object identifier, or {@code -1} if the object has not been saved
     */
    long getId();

    /**
     * Returns the object link identifier.
     * <p>
     * This is a UUID that is used to link objects until they can be made
     * persistent, and to provide support for object equality.
     *
     * @return the link identifier
     */
    String getLinkId();

    /**
     * Returns the archetype identifier.
     *
     * @return the archetype identifier.
     */
    String getArchetype();

    /**
     * Returns a reference for the object.
     *
     * @return the reference
     */
    Reference getObjectReference();

    /**
     * Returns the name.
     *
     * @return the name. May be {@code null}
     */
    String getName();

    /**
     * Sets the name.
     *
     * @param name the name. May be {@code null}
     */
    void setName(String name);

    /**
     * Returns the description.
     *
     * @return the description. May be {@code null}
     */
    String getDescription();

    /**
     * Sets the description.
     *
     * @param description the description. May be {@code null}
     */
    void setDescription(String description);

    /**
     * Returns the object version.
     * <p>
     * This is updated when the object is made persistent.
     *
     * @return the version.
     */
    long getVersion();

    /**
     * Determines if the object is active.
     *
     * @return {@code true} if the object is active, {@code false} if it is inactive.
     */
    boolean isActive();

    /**
     * Determines if the object is active.
     *
     * @param active if {@code true}, marks object active, otherwise marks it inactive
     */
    void setActive(boolean active);

    /**
     * Determines if the object is new.
     * <br/>
     * A new object is one that has been created but not yet persisted.
     *
     * @return {@code true} if the object is new, {@code false} if it has been made persistent
     */
    boolean isNew();

    /**
     * Determines if this is an instance of a particular archetype.
     *
     * @param archetype the archetype short name. May contain wildcards
     * @return {@code true} if the object is an instance of {@code archetype}
     */
    boolean isA(String archetype);

    /**
     * Determines if an object is one of a set of archetypes.
     *
     * @param archetypes the archetype short names. May contain wildcards
     * @return {@code true} if object is one of {@code archetypes}
     */
    boolean isA(String... archetypes);

}

