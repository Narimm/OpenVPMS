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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

import java.util.Date;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PeriodRelationshipState extends RelationshipState {

    /**
     * Creates a new <tt>PeriodRelationshipState</tt>.
     *
     * @param relationship      the relationship
     * @param sourceId          the source id
     * @param sourceName        the source name
     * @param sourceDescription the source description
     * @param targetId          the target id
     * @param targetName        the target name
     * @param targetDescription the target description
     * @param active            determines if the source and target are active
     */
    public PeriodRelationshipState(PeriodRelationship relationship,
                                   long sourceId, String sourceName,
                                   String sourceDescription, long targetId,
                                   String targetName, String targetDescription,
                                   boolean active) {
        super(relationship, sourceId, sourceName, sourceDescription, targetId,
              targetName, targetDescription, active);
    }

    /**
     * Creates a new <tt>PeriodRelationshipState</tt>.
     *
     * @param parent       the parent object
     * @param relationship the relationship
     * @param source       determines if parent is the source or target of the
     *                     relationship
     * @throws ArchetypeServiceException for any archetype service exception
     */
    public PeriodRelationshipState(IMObject parent,
                                   PeriodRelationship relationship,
                                   boolean source) {
        super(parent, relationship, source);
    }

    /**
     * Determines if the relationship is active.
     * It is active if:
     * <ul>
     * <li>the underlying {@link IMObjectRelationship} is active
     * <li>the underlying objects are active
     * <li>{@link PeriodRelationship#getActiveEndTime} is null or greater than
     * the current time
     * </ul>
     *
     * @return <tt>true</tt> if this is active; otherwise <tt>false</tt>
     */
    @Override
    public boolean isActive() {
        boolean result = super.isActive();
        if (result) {
            Date endTime = getRelationship().getActiveEndTime();
            if (endTime == null
                    || endTime.getTime() > System.currentTimeMillis()) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Returns the relationship.
     *
     * @return the relationship
     */
    @Override
    public PeriodRelationship getRelationship() {
        return (PeriodRelationship) super.getRelationship();
    }
}
