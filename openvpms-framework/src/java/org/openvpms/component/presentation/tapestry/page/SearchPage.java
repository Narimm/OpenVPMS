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
 *  $Id: PropertyEditor.java 118 2005-09-21 09:36:09Z tony $
 */

package org.openvpms.component.presentation.tapestry.page;

import java.util.List;

import org.apache.tapestry.IExternalPage;
import org.apache.tapestry.IRequestCycle;
import org.openvpms.component.presentation.tapestry.Visit;
import org.openvpms.component.presentation.tapestry.callback.SearchCallback;



/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class SearchPage extends OpenVpmsPage implements IExternalPage
{
    public abstract List getInstances();

    public abstract void setInstances(List instances);

    /* (non-Javadoc)
     * @see org.apache.tapestry.IExternalPage#activateExternalPage(java.lang.Object[], org.apache.tapestry.IRequestCycle)
     */
    public void activateExternalPage(Object[] args, IRequestCycle cycle)
    {
        cycle.activate(this);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.presentation.tapestry.page.OvpmsPage#pushCallback()
     */
    public void pushCallback()
    {
        Visit visit = (Visit)getVisit();
        visit.getCallbackStack().push(new SearchCallback(getPageName()));
    }
}
