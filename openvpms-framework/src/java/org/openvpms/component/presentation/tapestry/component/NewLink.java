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

package org.openvpms.component.presentation.tapestry.component;

// jakarta hivemind
import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.tapestry.IRequestCycle;
import org.openvpms.component.presentation.tapestry.callback.EditCallback;
import org.openvpms.component.presentation.tapestry.callback.SearchCallback;
import org.openvpms.component.presentation.tapestry.page.EditPage;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class NewLink extends Link {
    public static String SUFFIX = "Edit";

    public abstract String getArchetypeName();

    public abstract void setArchetypeName(String typeName);

    public void click(IRequestCycle cycle) {
        EditPage page = (EditPage) findPage(cycle, SUFFIX);
        if (getPage() instanceof EditPage)
            page.setCallback(new EditCallback(getPage().getPageName(),((EditPage)getPage()).getModel()));
        else
            page.setCallback(new SearchCallback(getPage().getPageName()));
        try {
            page.setModel(page.getArchetypeService().createDefaultObject(getArchetypeName()));
            cycle.activate(page);
        } catch (Exception ex) {
            throw new ApplicationRuntimeException(ex);
        }
    }

    public String getLinkText() {
        return "New " + getArchetypeName();
    }
}
