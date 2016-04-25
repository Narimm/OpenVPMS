/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.service.security;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.ArchetypeAwareGrantedAuthority;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.util.StringUtilities;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Votes if any configuration attribute begins with <code>archetype:</code>. If
 * no configuration begins with this value then the voter abstains from voting.
 * <p/>
 * It will vote to grant access if the user has a grant authority matching one
 * of the config attributes, otherwise a deny access will be returned.
 *
 * @author Jim Alateras
 */
public class ArchetypeAwareVoter implements AccessDecisionVoter<MethodInvocation> {

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

    /**
     * Indicates whether or not access is granted.
     * <p/>
     * The decision must be affirmative ({@code ACCESS_GRANTED}), negative ({@code ACCESS_DENIED})
     * or the {@code AccessDecisionVoter} can abstain ({@code ACCESS_ABSTAIN}) from voting.
     * Under no circumstances should implementing classes return any other value. If a weighting of results is desired,
     * this should be handled in a custom {@link AccessDecisionManager} instead.
     * <p/>
     * Unless an {@code AccessDecisionVoter} is specifically intended to vote on an access control
     * decision due to a passed method invocation or configuration attribute parameter, it must return
     * {@code ACCESS_ABSTAIN}. This prevents the coordinating {@code AccessDecisionManager} from counting
     * votes from those {@code AccessDecisionVoter}s without a legitimate interest in the access control
     * decision.
     * <p/>
     * Whilst the secured object (such as a {@code MethodInvocation}) is passed as a parameter to maximise flexibility
     * in making access control decisions, implementing classes should not modify it or cause the represented invocation
     * to take place (for example, by calling {@code MethodInvocation.proceed()}).
     *
     * @param authentication the caller making the invocation
     * @param object         the secured object being invoked
     * @param attributes     the configuration attributes associated with the secured object
     * @return either {@link #ACCESS_GRANTED}, {@link #ACCESS_ABSTAIN} or {@link #ACCESS_DENIED}
     */
    @Override
    public int vote(Authentication authentication, MethodInvocation object, Collection<ConfigAttribute> attributes) {
        int result = ACCESS_ABSTAIN;
        String[] shortNames = getArchetypeShortNames(object);
        for (ConfigAttribute attribute : attributes) {
            if (supports(attribute)) {
                result = isAccessGranted(shortNames, authentication, attribute);
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
     * otherwise <tt>ACCESS_DENIED</tt>.
     */
    private int isAccessGranted(String[] shortNames,
                                Authentication authentication,
                                ConfigAttribute attribute) {
        boolean granted = false;
        for (String shortName : shortNames) {
            granted = false;
            // Attempt to find a matching granted authority
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority instanceof ArchetypeAwareGrantedAuthority
                    && isAccessGranted((ArchetypeAwareGrantedAuthority) authority, attribute, shortName)) {
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
     * true if access is granted
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
        String authShortName = authority.getShortName();
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
                        Set<String> shortNames = new HashSet<>();
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
