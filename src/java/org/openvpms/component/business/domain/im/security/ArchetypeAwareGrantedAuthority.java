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


package org.openvpms.component.business.domain.im.security;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.springframework.security.core.GrantedAuthority;

import java.util.StringTokenizer;

/**
 * Represents permission for a service, method and associated archetype short
 * name.
 * <p/>
 * The precise format of an authority is
 * <p/>
 * archetype:archetypService.save:person.party
 * archetype:archetypeService.*:*.*
 * archetype:archetypeService.create:party.*
 * archetype:archetypeService.create:act.invoice
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeAwareGrantedAuthority extends IMObject
        implements GrantedAuthority {

    /**
     * Defualt SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The prefix is used to identify the grant type
     */
    private String prefix = "archetype";

    /**
     * This is the string version of the authority
     */
    private String authority;

    /**
     * The service name, which is an alias to the actual service interface
     */
    private String serviceName;

    /**
     * The method, which can be a regular expression
     */
    private String method;

    /**
     * The archetype short name, which can also be a regular expression
     */
    private String archetypeShortName;

    /**
     * The role that this authhority belongs too
     */
    private SecurityRole role;


    /**
     * Default constructor
     */
    public ArchetypeAwareGrantedAuthority() {
        // no op
    }

    /**
     * Construct an instance given the string representation of the
     * authority
     *
     * @param str a stringified version of the authority
     */
    public ArchetypeAwareGrantedAuthority(String str) {
        StringTokenizer tokens = new StringTokenizer(str, ":");
        if (tokens.countTokens() != 3) {
            throw new GrantedAuthorityException(
                    GrantedAuthorityException.ErrorCode.InvalidGrantAuthorityFormat,
                    new Object[]{str});
        }

        // the first token must be the prefix
        if (!tokens.nextToken().equals(prefix)) {
            throw new GrantedAuthorityException(
                    GrantedAuthorityException.ErrorCode.InvalidPrefix);
        }

        // the second token is the service and the method. The service 
        // cannot have any wildcards but the method can.
        StringTokenizer temp = new StringTokenizer(tokens.nextToken(), ".");
        if (temp.countTokens() != 2) {
            throw new GrantedAuthorityException(
                    GrantedAuthorityException.ErrorCode.InvalidServiceMethodFormat,
                    new Object[]{temp});
        }

        serviceName = temp.nextToken();
        method = temp.nextToken();
        archetypeShortName = tokens.nextToken();

        // store the original str
        this.authority = str;
    }

    /**
     * Returns the string representation of the authority.
     *
     * @return a representation of the granted authority
     */
    public String getAuthority() {
        if (authority == null) {
            StringBuffer buf = new StringBuffer(prefix);
            buf.append(":");
            buf.append(serviceName);
            buf.append(".");
            buf.append(method);
            buf.append(":");
            buf.append(archetypeShortName);

            authority = buf.toString();
        }

        return authority;
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name
     */
    public String getArchetypeShortName() {
        return archetypeShortName;
    }

    /**
     * @param archetypeShortName The archetypeShortName to set.
     */
    public void setArchetypeShortName(String archetypeShortName) {
        this.archetypeShortName = archetypeShortName;
    }

    /**
     * @return Returns the method.
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method The method to set.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return Returns the serviceName.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName The serviceName to set.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @param authority The authority to set.
     */
    void setAuthority(String authority) {
        this.authority = authority;
    }

    /**
     * @return Returns the role.
     */
    public SecurityRole getRole() {
        return role;
    }

    /**
     * @param role The role to set.
     */
    public void setRole(SecurityRole role) {
        this.role = role;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeAwareGrantedAuthority copy = (ArchetypeAwareGrantedAuthority) super.clone();
        copy.archetypeShortName = this.archetypeShortName;
        copy.authority = this.authority;
        copy.method = this.method;
        copy.prefix = this.prefix;
        copy.role = this.role;
        copy.serviceName = this.serviceName;

        return copy;
    }
}
