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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface IMObjectRelationshipDO extends IMObjectDO {
    /**
     * Returns the source object.
     *
     * @return the source object
     */
    IMObjectDO getSource();

    /**
     * Sets the source object.
     *
     * @param source the source object
     */
    void setSource(IMObjectDO source);

    /**
     * Returns a the target object.
     *
     * @return the target object
     */
    IMObjectDO getTarget();

    /**
     * Sets the target object.
     *
     * @param target the target object
     */
    void setTarget(IMObjectDO target);
}
