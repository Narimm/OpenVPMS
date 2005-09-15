/**
 * created 04-2005
 * net.sf.tash.tapestry.ApplicationBasePage.java
 */
package org.openvpms.component.presentation.tapestry.page;

import javax.servlet.http.HttpSession;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.context.HttpSessionContextIntegrationFilter;
import net.sf.acegisecurity.context.security.SecureContext;
import net.sf.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import net.sf.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.openvpms.component.presentation.tapestry.page.Login;

import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.PageRedirectException;
import org.apache.tapestry.html.BasePage;

/**
 * 
 * The base page of the application. Provides funtionality common
 * to all application pages. Has a lot of utility funtions in order
 * to retrieve some data about the current user. (Logins and such
 * are processed by Acegi Security System).
 * 
 * @author andyman
 *  
 */
public abstract class OvpmsPage extends BasePage {

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
	
	
    /**
     * cleanup operations
     */
    public void detach() {
        super.detach();
        setMessage(null);
		setErrorMessage(null);
    }
	
	
	/** 
     * Tapestry listener method to logout the current 
     * user.
     * 
     */

    public void logout(IRequestCycle cycle){

		HttpSession session = getRequestCycle().getRequestContext().getSession(); 
		if(session != null){
			SecureContext sc = (SecureContext) session.getAttribute(HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY);
			if(sc != null){
			    sc.setAuthentication(null);
			}	
		}
		
		Login login = (Login) cycle.getPage("Login");
		login.setMessage("Goodbye");
		throw new PageRedirectException(login);
		
    }
	
	
    /**
     * View helper function to display the applications screen name,
     * which is defined in the Tapestry page spec.
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
	 * @return
	 */
	private Authentication getAuthentification(){
		HttpSession session = getPage().getRequestCycle().getRequestContext().getSession(); 
		if(session != null){
			SecureContext sc = (SecureContext) session.getAttribute(HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY);
			if(sc != null){
			    Authentication auth = sc.getAuthentication();
			    return auth;
			}
		}
		return null;
	}
	
	
	/**
	 * Get the username (user credentials) that is automatically stored by 
	 * Acegi Security after succesfull login.
	 * @return
	 */
	public String getUserName(){
		Authentication auth = getAuthentification();
		if(auth != null){
			return (String) auth.getCredentials();
		}
		return "Anonymous";
	}
	
	
	
	/**
	 * Return true, if user has succesfully logged in.
	 * Returns false, if not.
	 * Returns false for the Anonymous user. 
	 * Makes use of Aceg Security. 
	 * @return
	 */
	public boolean getIsLoggedIn(){
		Authentication auth = getAuthentification();
		if(auth != null){
			if (auth instanceof AnonymousAuthenticationToken)
				return false;
			return true;
		}
		return false;
	}
	
	/**
	 * Return true if the current user is Anonymous 
	 * (Anonymous means the Acegi AnonymousAutheticationToken is given
	 *  or user is not logged in at all)
	 * Makes use of Acegi Security.
	 * @return
	 */
	public boolean getIsAnonymous(){
		Authentication auth = getAuthentification();
		if(auth != null){
			if ( ! (auth instanceof AnonymousAuthenticationToken) )
				return false;
		}
		return true;
	}
	
	
	/**
	 * Return true, if the given user is in a given role.
	 * The role prefix ROLE_ must not be provided. Anonymous users are 
	 * not handled.
	 * Makes use of Acegi Security.
	 * @param roleName
	 * @return
	 */
	public boolean userHasRole(String roleName){
		Authentication auth = getAuthentification();
		if (auth != null){
			if(auth instanceof UsernamePasswordAuthenticationToken){
				GrantedAuthority[] authorities = auth.getAuthorities();
				for (int i = 0; i < authorities.length; i++) {
					if(authorities[i].getAuthority().equalsIgnoreCase("role_" + roleName))
						return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Return true, if the current user has one or more of the
	 * given comma separated roles. The roles prefix ROLE_ must not
	 * be used by callers. Anonymous user is not handled!
	 * @param rolesAsString A comma separated list of rules (without the RULES_ prefix)
	 * @return
	 */
	public boolean userHasAnyRole(String rolesAsString){
		
		String[] roles = rolesAsString.split(",");
		
		Authentication auth = getAuthentification();
		if (auth != null){
			if(auth instanceof UsernamePasswordAuthenticationToken){
				GrantedAuthority[] authorities = auth.getAuthorities();
				for (int i = 0; i < authorities.length; i++) {
					for (int k = 0; k < roles.length; k++) {
						if(authorities[i].getAuthority().equalsIgnoreCase("role_" + roles[k]))
							return true;
					}
					
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true, if the current user is in all of the given roles.
	 * Returns false, if one or more rules are not assigned.
	 * The role prefix ROLE_ must not be provided by callers.
	 * Makes use of Acegi Security.
	 * @param rolesAsString Roles in a comma separated list.
	 * @return
	 */
	public boolean userHasAllRoles(String rolesAsString){
		
		String[] roles = rolesAsString.split(",");
		Authentication auth = getAuthentification();
		
		if(roles.length == 0) 
			throw new IllegalArgumentException("no roles provided to check");
		
		if(auth == null || auth.getAuthorities().length == 0)
			return false;
		
		// check for each role
		for (int i = 0; i < roles.length; i++) {
			boolean userIsInRole = false;
			
			for (int k = 0; k < auth.getAuthorities().length; k++) {
				String roleName = "role_" + roles[i];
				String authorityName = (String) auth.getAuthorities()[k].getAuthority();
				
				if(roleName.equalsIgnoreCase(authorityName)){
					userIsInRole = true;
				}
			}
			if(!userIsInRole){
				return false;
			}
		}
		return true;
	}
	
	
	
	

    public String getMessage() {
        return this.message;
    }
	
	public void setMessage(String message){
		this.message = message;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	
}
