/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.domain.im.common;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Describes the relationship between two entities.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityRelationship extends PeriodRelationship {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A relationship may also have an associated entity identity
     */
    private EntityIdentity identity;

    /**
     * TODO Definition for reason
     */
    private String reason;

    /**
     * An optional sequence for the relationship, used to order similar
     * relationships.
     */
    private int sequence;

    /**
     * Default constructor.
     */
    public EntityRelationship() {
        // do nothing
    }

    /**
     * Creates a new <tt>EntityRelationship</tt>.
     *
     * @param archetypeId the archetype id constraining this object
     * @param source      the relationship source
     * @param target      the relationship target
     */
    public EntityRelationship(ArchetypeId archetypeId,
                              IMObjectReference source,
                              IMObjectReference target) {
        super(archetypeId);
        setSource(source);
        setTarget(target);
    }

    /**
     * @return Returns the entityIdentity.
     */
    public EntityIdentity getIdentity() {
        return identity;
    }

    /**
     * @return Returns the reason.
     * @deprecated no replacement
     */
    @Deprecated
    public String getReason() {
        return reason;
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

    /**
     * @param identity The identity to set.
     */
    public void setIdentity(EntityIdentity identity) {
        this.identity = identity;
    }

    /**
     * @param reason The reason to set.
     * @deprecated no replacement
     */
    @Deprecated
    public void setReason(String reason) {
        this.reason = reason;
    }

    /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(null)
                .append("identity", identity)
                .toString();
    }
}
