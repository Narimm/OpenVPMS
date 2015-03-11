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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.domain.im.common;

import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * Describes a link between two entities.
 * <p/>
 * This is used by {@link Entity} to establish uni-directional links where both the source and targets may be queried.
 *
 * @author Tim Anderson
 */
public class EntityLink extends SequencedRelationship {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public EntityLink() {
        super();
    }

    /**
     * Constructs an {@link SequencedRelationship}.
     *
     * @param archetypeId the archetype identifier
     */
    public EntityLink(ArchetypeId archetypeId) {
        super(archetypeId);
    }
}
