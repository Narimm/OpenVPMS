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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.hibernate.im.common.PeriodRelationshipDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * Implementation of the {@link SequencedRelationshipDO} interface.
 *
 * @author Tim Anderson
 */
public class SequencedRelationshipDOImpl extends PeriodRelationshipDOImpl implements SequencedRelationshipDO {

    /**
     * The relationship sequence.
     */
    private int sequence;

    /**
     * Default constructor.
     */
    public SequencedRelationshipDOImpl() {
        super();
    }

    /**
     * Constructs an {@link SequencedRelationshipDOImpl}.
     *
     * @param archetypeId the archetype identifier
     */
    public SequencedRelationshipDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the relationship sequence.
     * <p/>
     * This may be used to order relationships.
     *
     * @return the relationship sequence
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * Sets the relationship sequence.
     * <p/>
     * This may be used to order relationships.
     *
     * @param sequence the relationship sequence
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
