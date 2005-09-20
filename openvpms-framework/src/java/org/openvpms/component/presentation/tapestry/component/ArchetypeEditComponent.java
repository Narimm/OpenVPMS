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

import org.apache.tapestry.IPage;
import org.apache.tapestry.components.Block;
import org.openvpms.component.presentation.tapestry.page.EditPage;

/**
 * @author fus8882
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class ArchetypeEditComponent extends ArchetypeComponent
{

    public boolean hasBlock(String propertyName)
    {
        if (getPage().getComponents().containsKey(propertyName))
        {
            return true;
        }
        else {
            IPage editPage = getEditPage();
            if (editPage != null)
            {
                return editPage.getComponents().containsKey(propertyName);
            }
        }
        return false;
    }

    protected EditPage getEditPage()
    {
        EditPage editPage = (EditPage)Utils.findPage(getPage().getRequestCycle(), 
            Utils.unqualify(getModel().getClass().getName()) + "Edit",
            "Edit");
        return editPage;
    }

    public Block getBlock(String propertyName)
    {
        if (getPage().getComponents().containsKey(propertyName))
        {
            return (Block)getPage().getComponent(propertyName);
        }
        EditPage editPage = getEditPage();
        if (editPage != null)
        {
            
            if (editPage.getComponents().containsKey(propertyName))
            {
                editPage.setModel(getModel());
                return (Block)editPage.getComponent(propertyName);
            }
        }
        return null;
    }

    public abstract Object getModel();

    public abstract void setModel(Object model);

}
