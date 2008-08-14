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
import org.openvpms.component.business.domain.im.lookup.Lookup;

import java.util.Set;


/**
 * Data object interface corresponding to the {@link Lookup} class.
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
     * @return <tt>true</tt> if this is the default lookup, otherwise
     *         <tt>false</tt>
     */
    boolean isDefaultLookup();

    /**
     * Determines if this is the default lookup.
     *
     * @param defaultLookup if <tt>true</tt> this is the default lookup
     */
    void setDefaultLookup(boolean defaultLookup);

    /**
     * Returns the source lookup relationships.
     *
     * @return the source lookup relationships
     */
    Set<LookupRelationshipDO> getSourceLookupRelationships();

    /**
     * Adds a relationship where this is the source.
     *
     * @param source the relationship to add
     */
    void addSourceLookupRelationship(LookupRelationshipDO source);

    /**
     * Removes a source relationship.
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
     * Adds a relationship where this is the target.
     *
     * @param target the relationship to add
     */
    void addTargetLookupRelationship(LookupRelationshipDO target);

    /**
     * Removes a target relationship.
     *
     * @param target the relationship to remove
     */
    void removeTargetLookupRelationship(LookupRelationshipDO target);
}
