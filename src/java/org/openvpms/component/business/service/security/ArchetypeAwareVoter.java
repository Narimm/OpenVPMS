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

import org.acegisecurity.Authentication;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.vote.AccessDecisionVoter;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.util.StringUtilities;
import org.springframework.aop.framework.ReflectiveMethodInvocation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Votes if any configuration attribute begins with <code>archetype:</code>. If
 * no configuration begins with this value then the voter abstains from voting.
 * <p/>
 * It will vote to grant access if the user has a grant authority matching one
 * of the config attributes, otherwise a deny access will be returned.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeAwareVoter implements AccessDecisionVoter {

    /**
     * Archetype service name prefix.
     */
    private final static String archetypePrefix = "archetypeService";

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ArchetypeAwareVoter.class);


    /**
     * Default constructor.
     */
    public ArchetypeAwareVoter() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.acegisecurity.vote.AccessDecisionVoter#supports(org.acegisecurity.ConfigAttribute)
     */
    public boolean supports(ConfigAttribute attribute) {
        String a = attribute.getAttribute();
        return (a != null) && (a.startsWith(archetypePrefix));
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
            ReflectiveMethodInvocation method
                    = (ReflectiveMethodInvocation) object;
            String[] shortNames = getArchetypeShortNames(method);
            Iterator iter = config.getConfigAttributes();

            while (iter.hasNext()) {
                ConfigAttribute attribute = (ConfigAttribute) iter.next();
                if (this.supports(attribute)) {
                    result = isAccessGranted(shortNames, authentication, attribute);
                }
            }
        }

        return result;
    }

    /**
     * Verifies that access is granted to each archetype.
     *
     * @param shortNames     the archetype short names
     * @param authentication the authentication
     * @param attribute      the config attribute
     * @return <tt>ACCESS_GRANTED</tt> if access is granted;
     *         otherwise <tt>ACCESS_DENIED</tt>.
     */
    private int isAccessGranted(String[] shortNames,
                                Authentication authentication,
                                ConfigAttribute attribute) {
        boolean granted = false;
        for (String shortName : shortNames) {
            granted = false;
            // Attempt to find a matching granted authority
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (isAccessGranted((ArchetypeAwareGrantedAuthority) authority,
                                    attribute, shortName)) {
                    granted = true;
                    break;
                }
            }
            if (!granted) {
                if (log.isWarnEnabled()) {
                    log.warn("Access denied to principal=" + authentication.getPrincipal() + ", operation="
                             + attribute.getAttribute() + ", archetype=" + shortName);
                }
                break;
            }
        }
        return (granted) ? ACCESS_GRANTED : ACCESS_DENIED;
    }

    /**
     * Determine if the access will be granted for the specified service on the
     * specifed archetype given an authority
     *
     * @param authority the authority to test against
     * @param attr      the associated contrib attributes
     * @param shortName the archetype shortName
     * @return boolean
     *         true if access is granted
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
        // second token is the method name
        String method = tokens.nextToken();
        String authMethod = authority.getMethod();
        String authShortName = authority.getArchetypeShortName();
        return (!StringUtils.isEmpty(authMethod)) &&
               (!StringUtils.isEmpty(authShortName)) &&
               (StringUtilities.matches(method, authMethod)) &&
               (StringUtilities.matches(shortName, authShortName));

    }

    /**
     * Examine the method and return the archetype short names.
     *
     * @param method the method to examine.
     * @return the archetype short names or null
     */
    @SuppressWarnings("unchecked")
    private String[] getArchetypeShortNames(MethodInvocation method) {
        String[] result = {};

        if (method != null) {
            String methodName = method.getMethod().getName();
            Class declaring = method.getMethod().getDeclaringClass();
            if (declaring.getName().equals(IArchetypeService.class.getName())) {
                Object arg = method.getArguments()[0];
                if (methodName.equals("create")) {
                    if (arg instanceof String) {
                        result = new String[]{(String) arg};
                    } else {
                        result = new String[]{((ArchetypeId) arg).getShortName()};
                    }
                } else if (methodName.equals("save")) {
                    if (arg instanceof IMObject) {
                        ArchetypeId id = ((IMObject) arg).getArchetypeId();
                        result = new String[]{id.getShortName()};
                    } else {
                        Collection<IMObject> objects = (Collection<IMObject>) arg;
                        Set<String> shortNames = new HashSet<String>();
                        for (IMObject object : objects) {
                            ArchetypeId id = object.getArchetypeId();
                            shortNames.add(id.getShortName());
                        }
                        result = shortNames.toArray(new String[shortNames.size()]);
                    }
                } else if (methodName.equals("remove")) {
                    ArchetypeId id = ((IMObject) arg).getArchetypeId();
                    result = new String[]{id.getShortName()};
                } else {
                    // we need to handle the other methods here
                }
            }
        }

        return result;
    }
}
