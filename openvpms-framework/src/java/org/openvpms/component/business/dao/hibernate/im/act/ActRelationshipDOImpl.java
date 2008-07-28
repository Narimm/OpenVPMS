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

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.common.PeriodRelationshipDOImpl;
import org.openvpms.component.business.domain.im.act.Act;


/**
 * A class that represents the directed association between two {@link Act}s.
 * In parent/child act relationships, the source act is the parent, the target
 * act is the child.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-04-01 14:58:48 +1100 (Tue, 01 Apr 2008) $
 */
public class ActRelationshipDOImpl extends PeriodRelationshipDOImpl
        implements ActRelationshipDO {

    /**
     * Indicates whether the relationship is one of parent-child. This means
     * that the parent is the owner of the relationship and is responsible for
     * managing its lifecycle. When the parent is deleted then it will also
     * delete the child
     */
    private boolean parentChildRelationship;


    /**
     * Default constructor.
     */
    public ActRelationshipDOImpl() {
        // do nothing
    }

    /**
     * Determines if this is a parent/child relationship between two acts.
     *
     * @param parentChildRelationship if <tt>true</true> it is a parent/child
     *                                relationship
     */
    public void setParentChildRelationship(boolean parentChildRelationship) {
        this.parentChildRelationship = parentChildRelationship;
    }

    /**
     * Determines if this is a parent/child relationship between two acts.
     * If <tt>true</tt> it indicates that the parent act is the owner of the
     * relationship and is responsible for managing its lifecycle. When the
     * parent act is deleted, then the child act must also be deleted.
     *
     * @return <tt>true</tt> if this is a parent/child relationship
     */
    public boolean isParentChildRelationship() {
        return parentChildRelationship;
    }

}
