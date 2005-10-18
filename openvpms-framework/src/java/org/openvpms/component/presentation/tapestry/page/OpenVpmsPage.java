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

import org.apache.tapestry.IPage;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.InjectState;
import org.apache.tapestry.callback.ICallback;
import org.apache.tapestry.html.BasePage;
import org.openvpms.component.business.service.act.IActService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.entity.IEntityService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.presentation.tapestry.Visit;

/**
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
     * the default screen name, used if no other specified
     */
    public static final String SCREEN_NAME = "OpenVPMS";

    @InjectObject("spring:archetypeService")
    public abstract IArchetypeService getArchetypeService();
    
    @InjectObject("spring:entityService")
    public abstract IEntityService getEntityService();
    
    public abstract IActService getActService();
    
    @InjectObject("spring:lookupService")
    public abstract ILookupService getLookupService();
    
    public abstract void pushCallback();

    @InjectState("visit")
    public abstract Visit getVisitObject();


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
}
