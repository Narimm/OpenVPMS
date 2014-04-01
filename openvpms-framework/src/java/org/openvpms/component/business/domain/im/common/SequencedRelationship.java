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
 * A relationship that may be ordered on a sequence.
 *
 * @author Tim Anderson
 */
public abstract class SequencedRelationship extends PeriodRelationship {

    /**
     * An optional sequence for the relationship, used to order similar relationships.
     */
    private int sequence;


    /**
     * Default constructor.
     */
    public SequencedRelationship() {
        super();
    }

    /**
     * Constructs an {@link SequencedRelationship}.
     *
     * @param archetypeId the archetype identifier
     */
    public SequencedRelationship(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the relationship sequence.
     * <p/>
     * This may be used to order relationships.
     *
     * @return the sequence
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Sets the relationship sequence.
     * <p/>
     * This may be used to order relationships.
     *
     * @param sequence the sequence
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
