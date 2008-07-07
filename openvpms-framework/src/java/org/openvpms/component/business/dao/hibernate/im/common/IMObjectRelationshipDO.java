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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Describes a relationship between two {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class IMObjectRelationshipDO extends IMObjectDO {

    /**
     * The source of the relationship.
     */
    private IMObjectDO source;

    /**
     * The target of the relationship.
     */
    private IMObjectDO target;


    /**
     * Default constructor.
     */
    public IMObjectRelationshipDO() {
        // do nothing
    }

    /**
     * Creates a new <tt>IMObjectRelationshipDO</tt>
     *
     * @param archetypeId the archetype id
     */
    public IMObjectRelationshipDO(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the source object.
     *
     * @return the source object
     */
    public IMObjectDO getSource() {
        return source;
    }

    /**
     * Sets the source object.
     *
     * @param source the source object
     */
    public void setSource(IMObjectDO source) {
        this.source = source;
    }

    /**
     * Returns a the target object.
     *
     * @return the target object
     */
    public IMObjectDO getTarget() {
        return target;
    }

    /**
     * Sets the target object.
     *
     * @param target the target object
     */
    public void setTarget(IMObjectDO target) {
        this.target = target;
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#toString()
     */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("source", source.getObjectReference())
                .append("target", target.getObjectReference())
                .toString();
    }
}
