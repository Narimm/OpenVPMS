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

package org.openvpms.component.business.dao.hibernate.im.security;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * Implementation of the {@link ArchetypeAuthorityDO} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeAuthorityDOImpl extends IMObjectDOImpl
        implements ArchetypeAuthorityDO {

    /**
     * The service name, which is an alias to the actual service interface.
     */
    private String serviceName;

    /**
     * The method, which can be a regular expression.
     */
    private String method;

    /**
     * The archetype short name, which can also be a regular expression.
     */
    private String shortName;

    /**
     * The role that the authority belongs to.
     */
    private SecurityRoleDO role;


    /**
     * Default constructor.
     */
    public ArchetypeAuthorityDOImpl() {
        // no op
    }

    /**
     * Creates a new <tt>ArchetypeAuthorityDOImpl</tt>.
     *
     * @param archetypeId the archetype id
     */
    public ArchetypeAuthorityDOImpl(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the archetype short name.
     *
     * @param shortName the archetype short name
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Returns the method.
     *
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the method.
     *
     * @param method the method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Returns the service name.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the service name.
     *
     * @param name the service name
     */
    public void setServiceName(String name) {
        serviceName = name;
    }

    /**
     * Returns the role.
     *
     * @return the role
     */
    public SecurityRoleDO getRole() {
        return role;
    }

    /**
     * Sets the role.
     *
     * @param role the role
     */
    public void setRole(SecurityRoleDO role) {
        this.role = role;
    }

}
