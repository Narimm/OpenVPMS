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

import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.callback.ICallback;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.event.PageRenderListener;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.INodeDescriptor;
import org.openvpms.component.presentation.tapestry.Global;
import org.openvpms.component.presentation.tapestry.Visit;
import org.openvpms.component.presentation.tapestry.callback.EditCallback;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public abstract class EditPage extends OpenVpmsPage implements PageRenderListener {

    public abstract Object getModel();

    public abstract void setModel(Object model);

    public abstract ICallback getNextPage();

    public abstract void setNextPage(ICallback NextPage);

    /* (non-Javadoc)
     * @see org.openvpms.component.presentation.tapestry.page.OvpmsPage#pushCallback()
     */
    public void pushCallback() {
        Visit visit = (Visit) getVisit();
        visit.getCallbackStack().push(
                new EditCallback(getPageName(), getModel()));
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.event.PageRenderListener#pageBeginRender(org.apache.tapestry.event.PageEvent)
     */
    public void pageBeginRender(PageEvent arg0) {
    }

    /**
     * @param cycle
     */
    public void save(IRequestCycle cycle) {
        save();
    }

    /**
     * @param cycle
     */
    public void remove(IRequestCycle cycle)
    {
          Visit visit = (Visit)getVisit();
          ICallback callback = (ICallback)visit.getCallbackStack().pop();
          if (getModel() instanceof Entity)
              ((Global)getGlobal()).getEntityService().remove((Entity)getModel());
          else if (getModel() instanceof Act)
              ((Global)getGlobal()).getActService().remove((Act)getModel());
          else if (getModel() instanceof Lookup)
              ((Global)getGlobal()).getLookupService().remove((Lookup)getModel());
              
          callback.performCallback(cycle);
    }

    /**
     * @param cycle
     */
    public void saveAndReturn(IRequestCycle cycle) {
        if (save()) {
            Visit visit = (Visit) getVisit();
            ICallback callback = (ICallback) visit.getCallbackStack().pop();
            callback.performCallback(cycle);
        }
    }

    public void onFormSubmit(IRequestCycle cycle) {
        if (getNextPage() != null) {
            getNextPage().performCallback(cycle);
        }
    }

    /**
     * @return
     */
    /**
     * @return
     */
    protected boolean save() {
        if (!getDelegate().getHasErrors()) {
            try {
                Global global = (Global)getGlobal();
                if (getModel() instanceof Entity)
                    global.getEntityService().save((Entity)getModel());
                else if (getModel() instanceof Act)
                    global.getActService().save((Act)getModel());
                else if (getModel() instanceof Lookup)
                    global.getLookupService().save((Lookup)getModel());                   
            } catch (Exception pe) {
//                getDelegate().record(pe);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * @return
     */
    public boolean isModelNew() {
        return ((IMObject)getModel()).isNew();
    }

    /**
     * @return
     */
    public IArchetypeDescriptor getArchetypeDescriptor() {
        return ((Global)getGlobal()).getArchetypeService().getArchetypeDescriptor(
                ((IMObject)getModel()).getArchetypeId().getShortName());
    }

    public IPropertySelectionModel getLookupModel(INodeDescriptor descriptor) {
        // TODO need to work out how to get lookup selection models from node descriptor
        // information and model data available to the page.  This method should populate
        // a special implementation of IPropertySelectionModel called LookupSelectionModel
        // from either the Lookup service or the Archetyped defined values.
        return null;
    }

    public IPropertySelectionModel getEntityModel(INodeDescriptor descriptor) {
        // TODO need to work out how to get Entity selection models from node descriptor
        // information and model data available to the page.  This method should populate
        // a special implementation of IPropertySelectionModel called EntitySelectionModel
        // from the Entity service based on the Archetype constraints in the descriptor.
        return null;
    }
    /**
     * @return
     */
    public String getTitle() {
        if (isModelNew()) {
            return "Add " + getArchetypeDescriptor().getDisplayName();
        } else {
            return "Edit " + getArchetypeDescriptor().getDisplayName();
        }
    }
}
