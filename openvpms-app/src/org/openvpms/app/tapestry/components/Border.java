/*
 * Created on 19.06.2004
 *
 * 
 */
package org.openvpms.app.tapestry.components;


import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.event.PageRenderListener;
import org.openvpms.component.presentation.tapestry.component.OpenvpmsComponent;

/**
 * 
 * The Border component's corresponding java class file. Mainly used for
 * navigation and the application's overall look and feel.
 * 
 * @author andyman
 * 
 */
public abstract class Border extends OpenvpmsComponent implements
        PageRenderListener {


    /** navigate to the Login form  */ 
    public void login(IRequestCycle cycle) {
        cycle.activate("Login");
    }


    /** log the user out */
    public void logout(IRequestCycle cycle) {
        // TODO logout
        
    }

    /**
     * setting a few values needed to render the border
     * 
     * @see org.apache.tapestry.event.PageRenderListener#pageBeginRender(org.apache.tapestry.event.PageEvent)
     */
    public void pageBeginRender(PageEvent event) {

		
    }




}