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


package org.openvpms.component.business.service.security;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.acegisecurity.Authentication;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.vote.AccessDecisionVoter;
import org.aopalliance.intercept.MethodInvocation;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

/**
 * Votes if any configuration attribute begins with <code>archetype:</code>. If
 * no confoguration begins with this value then the voter abstains from voting.
 * <p>
 * It will vote to grant access if the user has a grant authority matching one
 * of the config attributes, otherwise a deny access will be returned.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeAwareVoter implements AccessDecisionVoter {

    private final static String archetypePrefix = "archetypeService";

    /**
     * Default constructor
     */
    public ArchetypeAwareVoter() {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see org.acegisecurity.vote.AccessDecisionVoter#supports(org.acegisecurity.ConfigAttribute)
     */
    public boolean supports(ConfigAttribute attribute) {
        if ((attribute.getAttribute() != null) && 
            (attribute.getAttribute().startsWith(archetypePrefix))) {
                return true;
            } else {
                return false;
            }
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.vote.AccessDecisionVoter#supports(java.lang.Class)
     */
    public boolean supports(Class clazz) {
        return clazz == MethodInvocation.class;
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.vote.AccessDecisionVoter#vote(org.acegisecurity.Authentication, java.lang.Object, org.acegisecurity.ConfigAttributeDefinition)
     */
    public int vote(Authentication authentication, Object object,
            ConfigAttributeDefinition config) {
        int result = ACCESS_ABSTAIN;
        
        // make sure that we are dealing with an {@link IMObject}
        if (object instanceof ReflectiveMethodInvocation) {
            ReflectiveMethodInvocation method = (ReflectiveMethodInvocation)object;
            String shortName = getArchetypeShortNameForPrimaryObject(method);
            Iterator iter = config.getConfigAttributes();

            while (iter.hasNext()) {
                ConfigAttribute attribute = (ConfigAttribute)iter.next();
                if (this.supports(attribute)) {
                    result = ACCESS_DENIED;
    
                    // Attempt to find a matching granted authority
                    for (GrantedAuthority authority : authentication.getAuthorities()) {
                        if (isAccessGranted((ArchetypeAwareGrantedAuthority)authority, 
                                attribute, shortName)) {
                           return ACCESS_GRANTED;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Determine if the access will be granted for the specified service on the
     * specifed archetype given an authority
     * 
     * @param authority
     *            the authority to test against
     * @param attribute
     *            the associated contrib attributes
     * @param shortName
     *            the archetype shortName
     * @return boolean
     *            true if access is granted                        
     */
    private boolean isAccessGranted(ArchetypeAwareGrantedAuthority authority, 
            ConfigAttribute attr, String shortName) {
        
        // this is in the form of <serviceName>.<method>
        String value = attr.getAttribute();
        StringTokenizer tokens = new StringTokenizer(value, ".");
        
        // the first token is the method name and it must match
        // exactly
        String service = tokens.nextToken();
        if (!service.equals(authority.getServiceName())) {
            return false;
        }

        // second toke is the method name
        String method = tokens.nextToken();
        String authMethod = authority.getMethod();
        boolean methodMatch = false;
        if (authMethod.contains("*")) {
            methodMatch = method.matches(authMethod);
        } else {
            methodMatch = method.equals(authMethod);
        }
        
        if (!methodMatch) {
            return false;
        }
        
        // now check the archetype id
        String authShortName = authority.getArchetypeShortName();
        if (authShortName.contains("*")) {
            return shortName.matches(authShortName);
        } else {
            return shortName.equals(authShortName);
        }
    }
    
    /**
     * Examine the method and return the archetypeid for the primary objects
     * 
     * @param method
     *            the method to examine.
     * @return String
     *          the archetype short name or null
     */
    private String getArchetypeShortNameForPrimaryObject(ReflectiveMethodInvocation method) {
        String shortName = null;
        
        if (method != null) {
            String methodName = method.getMethod().getName();
            if (method.getMethod().getDeclaringClass().getName().equals(IArchetypeService.class.getName())) {
                if (methodName.equals("create")) {
                    shortName = (String)method.getArguments()[0];
                } else if (methodName.equals("save")) {
                    shortName = ((IMObject)method.getArguments()[0]).getArchetypeId().getShortName();
                } else if (methodName.equals("remove")) {
                    shortName = ((IMObject)method.getArguments()[0]).getArchetypeId().getShortName();
                } else {
                    // we need to handle the other methods here
                }
            }
        }
        
        return shortName;
    }
}
