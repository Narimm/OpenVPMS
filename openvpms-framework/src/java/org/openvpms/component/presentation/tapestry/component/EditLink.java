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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.presentation.tapestry.page.EditPage;
import org.openvpms.component.presentation.tapestry.page.OpenVpmsPage;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class EditLink extends Link
{
  
    public abstract ArchetypeDescriptor getArchetypeDescriptor();

    public abstract void setArchetypeDescriptor(ArchetypeDescriptor archetypeDescriptor);
    
    public static String SUFFIX = "Edit";

    public String getArchetypeName()
    {
        return ((IMObject)getModel()).getArchetypeId().getShortName();
    }

    public abstract Object getModel();

    public abstract void setModel(Object model);

    public void click(Object model)
    {
        EditPage page = (EditPage) findPage(getPage().getRequestCycle(), ((IMObject)model).getArchetypeId().getShortName(),SUFFIX);
        ((OpenVpmsPage)getPage()).pushCallback();
        page.setModel(model);
        getPage().getRequestCycle().activate(page);
    }
}
