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

// java core
import java.util.StringTokenizer;

// acegi-security
import org.acegisecurity.GrantedAuthority;

// commons-lang
import org.apache.commons.lang.StringUtils;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Represents permission for a service, method and associated archetype short
 * name. 
 * <p>
 * The precise format of an authority is 
 * 
 * archetype:archetypService.save:person.party
 * archetype:archetypeService.*:*.*
 * archetype:archetypeService.create:party.*
 * archetype:archetypeService.create:act.invoice
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
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
     * @param str
     *            a stringified version of the authority
     */
    public ArchetypeAwareGrantedAuthority(String str) {
        StringTokenizer tokens = new StringTokenizer(str, ":");
        if (tokens.countTokens() != 3) {
            throw new GrantedAuthorityException(
                    GrantedAuthorityException.ErrorCode.InvalidGrantAuthorityFormat,
                    new Object[] {str});
        }
        
        // the first token must be the prefix
        if (!tokens.nextToken().equals(prefix)) {
            throw new GrantedAuthorityException(
                    GrantedAuthorityException.ErrorCode.InvalidPrefix);
        }
        
        // the second token is the service and the method. The service 
        // cannot have any wildcards but the method can.
        StringTokenizer temp = new StringTokenizer(tokens.nextToken(), ".");
        if (temp.countTokens() !=2) {
            throw new GrantedAuthorityException(
                    GrantedAuthorityException.ErrorCode.InvalidServiceMethodFormat,
                    new Object[] {temp});
        }
        
        serviceName = temp.nextToken();
        method = StringUtils.replace(temp.nextToken(), "*", ".*");
        
        // the last token is the archetype short name, which can also be 
        // a regular expression
        archetypeShortName = tokens.nextToken();
        if (archetypeShortName.contains("*")) {
            archetypeShortName = StringUtils.replace(archetypeShortName, ".", "\\.");
            archetypeShortName = StringUtils.replace(archetypeShortName, "*", ".*");
        }
        
        // store the original str
        this.authority = str;
    }
    
    /**
    /* (non-Javadoc)
     * @see org.acegisecurity.GrantedAuthority#getAuthority()
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
     * @return Returns the archetypeShortName.
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
}
