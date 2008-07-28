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

package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;

import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface LookupDO extends IMObjectDO {
    /**
     * Returns the lookup code.
     *
     * @return the code
     */
    String getCode();

    /**
     * Sets the lookup code.
     *
     * @param code the code to set
     */
    void setCode(String code);

    /**
     * Determines if this is the default lookup.
     *
     * @return <code>true</code> if this is the default lookup, otherwise
     *         <code>false</code>
     */
    boolean isDefaultLookup();

    /**
     * Determines if this is the default lookup.
     *
     * @param defaultLookup if <code>true</code> this is the default lookup
     */
    void setDefaultLookup(boolean defaultLookup);

    /**
     * Returns the the source lookup relationships.
     *
     * @return the source lookup relationships
     */
    Set<LookupRelationshipDO> getSourceLookupRelationships();

    /**
     * Add a source {@link LookupRelationshipDOImpl}.
     *
     * @param source the relationship to add
     */
    void addSourceLookupRelationship(LookupRelationshipDO source);

    /**
     * Remove a source {@link LookupRelationshipDOImpl}.
     *
     * @param source the relationship to remove
     */
    void removeSourceLookupRelationship(LookupRelationshipDO source);

    /**
     * Returns the target lookup relationships.
     *
     * @return the target lookup relationships
     */
    Set<LookupRelationshipDO> getTargetLookupRelationships();

    /**
     * Adds a target {@link LookupRelationshipDOImpl}.
     *
     * @param target the relationship to add
     */
    void addTargetLookupRelationship(LookupRelationshipDO target);

    /**
     * Removes a target {@link LookupRelationshipDOImpl}.
     *
     * @param target the relationship to remove
     */
    void removeTargetLookupRelationship(LookupRelationshipDO target);
}
