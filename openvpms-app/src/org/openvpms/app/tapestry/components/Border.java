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


package org.openvpms.app.tapestry.components;


import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.event.PageRenderListener;
import org.openvpms.component.presentation.tapestry.component.OpenVpmsComponent;

/**
 * 
 * The Border component's corresponding java class file. Mainly used for
 * navigation and the application's overall look and feel.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class Border extends OpenVpmsComponent implements
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