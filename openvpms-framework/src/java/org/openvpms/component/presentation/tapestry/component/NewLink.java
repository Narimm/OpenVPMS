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

// jakarta tapestry
import org.apache.tapestry.IRequestCycle;

// openvpms-framework
import org.openvpms.component.presentation.tapestry.Global;
import org.openvpms.component.presentation.tapestry.page.EditPage;
import org.openvpms.component.presentation.tapestry.page.OpenVpmsPage;

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
        ((OpenVpmsPage) getPage()).pushCallback();
        EditPage page = (EditPage) findPage(cycle, SUFFIX);

        try {
            page.setModel(((Global) page.getGlobal()).getArchetypeService()
                    .createDefaultObject(getArchetypeName()));
            cycle.activate(page);
        } catch (Exception ex) {
            throw new ApplicationRuntimeException(ex);
        }
    }

    public String getLinkText() {
        return "New " + getArchetypeName();
    }
}
