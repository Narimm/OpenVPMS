/*
 * Global.java
 * Created on 21.06.2004 by andyman
 * wirteverein-admin
 * 
 */
package org.openvpms.component.presentation.tapestry;

import org.springframework.context.ApplicationContext;

/**
 * 
 * 
 * The tapestry applications Global object, common to all clients and all
 * components (pages). 
 * 
 * 
 * 
 * @author andyman
 * @version $Id: Global.java,v 1.1 2005/04/17 20:15:19 andyman232323 Exp $
 *  
 */
public class Global {


    
    /** the Springframework application context */
    private ApplicationContext appContext;
    
            
    /**
     * @return Returns the Springframeweork application context appContext.
     */
    public ApplicationContext getAppContext() {
        return this.appContext;
    }

    /**
     * set the Springframework application context
     * @param ac
     */
    public void setAppContext(ApplicationContext ac) {
        this.appContext = ac;
    }
}