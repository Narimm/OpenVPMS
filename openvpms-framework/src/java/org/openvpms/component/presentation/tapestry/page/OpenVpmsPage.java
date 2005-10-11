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

package org.openvpms.component.presentation.tapestry.page;

import javax.servlet.http.HttpSession;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.context.HttpSessionContextIntegrationFilter;
import net.sf.acegisecurity.context.security.SecureContext;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;

import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.PageRedirectException;
import org.apache.tapestry.html.BasePage;
import org.apache.tapestry.valid.IValidationDelegate;
import org.openvpms.component.business.service.act.IActService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.entity.IEntityService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.presentation.tapestry.validation.OpenVpmsValidationDelegate;

/**
 * 
 * The base page of any OpenVPMS application. Provides funtionality common to all
 * application pages. Has a lot of utility funtions in order to retrieve some
 * data about the current user. (Logins and such are processed by Acegi Security
 * System).
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class OpenVpmsPage extends BasePage {

    /**
     * A property to be bound to all pages, used by the ShowMessage component
     * which is used by the Border component.
     * 
     * @return Returns the message to show
     */
    public String message;

    /**
     * An error message, that is not connected with an form component.
     * 
     */
    public String errorMessage;

    /**
     * the default screen name, used if no other specified
     */
    public static final String SCREEN_NAME = "OpenVPMS";

    /** the validation delegate */
    private IValidationDelegate delegate;

    /**
     * @return Returns the errorMessage.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage The errorMessage to set.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public IValidationDelegate getDelegate()
    {
        if (delegate == null)
            delegate = new OpenVpmsValidationDelegate();

        return delegate;
    }

    /**
     * 
     */
    public abstract void pushCallback();

    public abstract IArchetypeService getArchetypeService();
    
    public abstract IEntityService getEntityService();
    
    public abstract IActService getActService();
    
    public abstract ILookupService getLookupService();
    
    /**
     * cleanup operations
     */
    public void detach() {
        super.detach();
        setMessage(null);
        setErrorMessage(null);
    }

    /**
     * Tapestry listener method to logout the current user.
     * 
     */

    public void logout(IRequestCycle cycle) {

        HttpSession session = getRequestCycle().getRequestContext()
                .getSession();
        if (session != null) {
            SecureContext sc = (SecureContext) session
                    .getAttribute(HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY);
            if (sc != null) {
                sc.setAuthentication(null);
            }
        }

        Login login = (Login) cycle.getPage("Login");
        login.setMessage("Goodbye");
        throw new PageRedirectException(login);

    }

    /**
     * View helper function to display the applications screen name, which is
     * defined in the Tapestry page spec.
     * 
     * @return screenName The title of the current page
     */
    public String getScreenName() {
        IPage page = getPage();
        String screenName = page.getSpecification().getProperty("screenName");
        if (screenName == null)
            return SCREEN_NAME + " - " + getPage().getPageName();
        return screenName;
    }

    /**
     * Return the Authentification object that is stored by Acegi security
     * inside the HttpSession
     * 
     * @return
     */
    private Authentication getAuthentification() {
        HttpSession session = getPage().getRequestCycle().getRequestContext()
                .getSession();
        if (session != null) {
            SecureContext sc = (SecureContext) session
                    .getAttribute(HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY);
            if (sc != null) {
                Authentication auth = sc.getAuthentication();
                return auth;
            }
        }
        return null;
    }

    /**
     * Get the username (user credentials) that is automatically stored by Acegi
     * Security after succesfull login.
     * 
     * @return
     */
    public String getUserName() {
        Authentication auth = getAuthentification();
        if (auth != null) {
            return (String) auth.getCredentials();
        }
        return "Anonymous";
    }

    /**
     * Return true, if user has succesfully logged in. Returns false, if not.
     * Returns false for the Anonymous user. Makes use of Aceg Security.
     * 
     * @return
     */
    public boolean getIsLoggedIn() {
        Authentication auth = getAuthentification();
        if (auth != null) {
            if (auth instanceof AnonymousAuthenticationToken)
                return false;
            return true;
        }
        return false;
    }

    /**
     * Return true if the current user is Anonymous (Anonymous means the Acegi
     * AnonymousAutheticationToken is given or user is not logged in at all)
     * Makes use of Acegi Security.
     * 
     * @return
     */
    public boolean getIsAnonymous() {
        Authentication auth = getAuthentification();
        if (auth != null) {
            if (!(auth instanceof AnonymousAuthenticationToken))
                return false;
        }
        return true;
    }

    /**
     * Return true, if the given user is in a given role. The role prefix ROLE_
     * must not be provided. Anonymous users are not handled. Makes use of Acegi
     * Security.
     * 
     * @param roleName
     * @return
     */
    public boolean userHasRole(String roleName) {
        Authentication auth = getAuthentification();
        if (auth != null) {
            if (auth instanceof UsernamePasswordAuthenticationToken) {
                GrantedAuthority[] authorities = auth.getAuthorities();
                for (int i = 0; i < authorities.length; i++) {
                    if (authorities[i].getAuthority().equalsIgnoreCase(
                            "role_" + roleName))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Return true, if the current user has one or more of the given comma
     * separated roles. The roles prefix ROLE_ must not be used by callers.
     * Anonymous user is not handled!
     * 
     * @param rolesAsString
     *            A comma separated list of rules (without the RULES_ prefix)
     * @return
     */
    public boolean userHasAnyRole(String rolesAsString) {

        String[] roles = rolesAsString.split(",");

        Authentication auth = getAuthentification();
        if (auth != null) {
            if (auth instanceof UsernamePasswordAuthenticationToken) {
                GrantedAuthority[] authorities = auth.getAuthorities();
                for (int i = 0; i < authorities.length; i++) {
                    for (int k = 0; k < roles.length; k++) {
                        if (authorities[i].getAuthority().equalsIgnoreCase(
                                "role_" + roles[k]))
                            return true;
                    }

                }
            }
        }
        return false;
    }

    /**
     * Returns true, if the current user is in all of the given roles. Returns
     * false, if one or more rules are not assigned. The role prefix ROLE_ must
     * not be provided by callers. Makes use of Acegi Security.
     * 
     * @param rolesAsString
     *            Roles in a comma separated list.
     * @return
     */
    public boolean userHasAllRoles(String rolesAsString) {

        String[] roles = rolesAsString.split(",");
        Authentication auth = getAuthentification();

        if (roles.length == 0)
            throw new IllegalArgumentException("no roles provided to check");

        if (auth == null || auth.getAuthorities().length == 0)
            return false;

        // check for each role
        for (int i = 0; i < roles.length; i++) {
            boolean userIsInRole = false;

            for (int k = 0; k < auth.getAuthorities().length; k++) {
                String roleName = "role_" + roles[i];
                String authorityName = (String) auth.getAuthorities()[k]
                        .getAuthority();

                if (roleName.equalsIgnoreCase(authorityName)) {
                    userIsInRole = true;
                }
            }
            if (!userIsInRole) {
                return false;
            }
        }
        return true;
    }


}
