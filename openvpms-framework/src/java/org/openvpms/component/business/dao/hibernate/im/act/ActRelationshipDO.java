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

import org.openvpms.component.business.dao.hibernate.im.common.PeriodRelationshipDO;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ActRelationshipDO extends PeriodRelationshipDO {
    /**
     * Determines if this is a parent/child relationship between two acts.
     *
     * @param parentChildRelationship if <tt>true</true> it is a parent/child
     *                                relationship
     */
    void setParentChildRelationship(boolean parentChildRelationship);

    /**
     * Determines if this is a parent/child relationship between two acts.
     * If <tt>true</tt> it indicates that the parent act is the owner of the
     * relationship and is responsible for managing its lifecycle. When the
     * parent act is deleted, then the child act must also be deleted.
     *
     * @return <tt>true</tt> if this is a parent/child relationship
     */
    boolean isParentChildRelationship();
}
