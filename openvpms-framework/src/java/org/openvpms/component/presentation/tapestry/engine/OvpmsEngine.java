/*
 * Engine.java
 * Created on 06.04.2005 by andyman
 * project wirteverein-admin
 * Version $Id: SpringTapestryEngine.java,v 1.1 2005/04/17 20:15:19 andyman232323 Exp $
 */

package org.openvpms.component.presentation.tapestry.engine;

import org.openvpms.component.presentation.tapestry.Global;
import org.apache.log4j.Logger;
import org.apache.tapestry.engine.BaseEngine;
import org.apache.tapestry.request.RequestContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 
 * The custom application engine. Main purpose of this application
 * specific engine  is to provide the Springframework ApplicationContext.
 * 
 * BaseEngine will be depreceated in Tapestry 3.1, so this will certainly
 * go away.
 * 
 * @author andyman
 *  
 */
public class OvpmsEngine extends BaseEngine {
    private static final long serialVersionUID = 3257288032683177524L;
    
    private static Logger log = Logger.getLogger(OpenVpmsEngine.class);
    
    protected void setupForRequest(RequestContext context) {
        
        super.setupForRequest(context);
        
         if(log.isDebugEnabled())
             log.debug("entering setupForRequest()");
        
        // insert ApplicationContext in global, if not there
        Global global = (Global) getGlobal();
        ApplicationContext ac = 
            (ApplicationContext) global.getAppContext();
        if (ac == null) {
            if(log.isDebugEnabled()) 
                log.debug("ApplicationContext ac is null");
            ac = WebApplicationContextUtils.getWebApplicationContext(
                context.getServlet().getServletContext());
            global.setAppContext(ac);
        }
   
    }
}