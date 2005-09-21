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

import java.util.ArrayList;

import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.callback.ICallback;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.event.PageRenderListener;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.openvpms.component.business.domain.im.Entity;
import org.openvpms.component.business.domain.im.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IPropertyDescriptor;
import org.openvpms.component.business.service.entity.IEntityService;
import org.openvpms.component.presentation.tapestry.Visit;
import org.openvpms.component.presentation.tapestry.callback.CollectionCallback;
import org.openvpms.component.presentation.tapestry.callback.EditCallback;
import org.openvpms.component.presentation.tapestry.validation.OvpmsValidationDelegate;


/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class EditPage extends OvpmsPage implements PageRenderListener
{
    
    public void pageBeginRender(PageEvent arg0)
    {
    }

    public abstract Object getModel();

    public abstract void setModel(Object model);

    public abstract OvpmsValidationDelegate getDelegate();

    public abstract void setDelegate(OvpmsValidationDelegate Delegate);

    public abstract ICallback getNextPage();

    public abstract void setNextPage(ICallback NextPage);

    public abstract IEntityService getEntityService();

    public abstract void setEntityService(IEntityService esvc);

    public abstract IArchetypeService getArchetypeService();

    public abstract void setArchetypeService(
        IArchetypeService PropertyDescriptorService);

    public void save(IRequestCycle cycle)
    {
        save();       
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.trails.page.TrailsPage#pushCallback()
     */
    public void pushCallback()
    {
        Visit visit = (Visit)getVisit();
        visit.getCallbackStack().push(new EditCallback(getPageName(), getModel()));
    }
    
    protected abstract boolean save();

    public abstract void remove(IRequestCycle cycle);

    public void onFormSubmit(IRequestCycle cycle)
    {
        if (getNextPage() != null)
        {
            getNextPage().performCallback(cycle);
        }
    }
    
    /**
     * @return
     */
    public IArchetypeDescriptor getArchetypeDescriptor()
    {
        return getArchetypeService().getArchetypeDescriptor(((IMObject)getModel()).getName());
    }

    public IPropertySelectionModel getSelectionModel(IPropertyDescriptor descriptor)
    {
//        ArrayList instances = new ArrayList();
// instances.addAll(getEntityService().getAllInstances(descriptor.getPropertyType()));
// IdentifierSelectionModel selectionModel = new
// IdentifierSelectionModel(instances,
// getArchetypeService().getArchetypeDescriptor(descriptor.getPropertyType())
// .getIdentifierDescriptor().getName(),
// !descriptor.isRequired());

        return null;
    }

    /**
     * @return
     */
    public boolean isModelNew()
    {
        return false;
    }

    /**
     * @param cycle
     */
    public void saveAndReturn(IRequestCycle cycle)
    {
        if (save())
        {
            Visit visit = (Visit)getVisit();
            ICallback callback = (ICallback)visit.getCallbackStack().pop();
            if (callback instanceof CollectionCallback)
            {
                ((CollectionCallback)callback).add(getModel());
            }
            callback.performCallback(cycle);
        }
    }

    /**
     * @return
     */
    public String getTitle()
    {
        if (cameFromCollection() && isModelNew())
        {
            return "Add " + getArchetypeDescriptor().getDisplayName();
        }
        else
        {
            return "Edit " + getArchetypeDescriptor().getDisplayName();
        }
    }

    public boolean cameFromCollection()
    {
        Visit visit = (Visit)getVisit();
        return visit.getCallbackStack().peek() instanceof CollectionCallback;
    }
    
    public boolean cameFromChildCollection()
    {
        Visit visit = (Visit)getVisit();
        if (visit.getCallbackStack().peek() instanceof CollectionCallback)
        {
            return ((CollectionCallback)visit.getCallbackStack().peek()).isChildRelationship();
        }
        else return false;
    }
}
