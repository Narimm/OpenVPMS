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

import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.callback.ICallback;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.apache.tapestry.form.StringPropertySelectionModel;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.presentation.tapestry.Visit;
import org.openvpms.component.presentation.tapestry.callback.CollectionCallback;
import org.openvpms.component.presentation.tapestry.callback.EditCallback;
import org.openvpms.component.presentation.tapestry.component.LookupSelectionModel;
import org.openvpms.component.presentation.tapestry.validation.OpenVpmsValidationDelegate;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public abstract class EditPage extends OpenVpmsPage {

    public abstract Object getModel();

    public abstract void setModel(Object model);

    public abstract ICallback getNextPage();

    public abstract void setNextPage(ICallback NextPage);

    @Bean
    public abstract OpenVpmsValidationDelegate getDelegate();

    /* (non-Javadoc)
     * @see org.openvpms.component.presentation.tapestry.page.OpenVpmsPage#pushCallback()
     */
    public void pushCallback()
    {
        Visit visit = (Visit)getVisitObject();
        visit.getCallbackStack().push(new EditCallback(getPageName(), getModel()));
    }

    /**
     * @param cycle
     */
    public void save(IRequestCycle cycle) {
        ICallback callback = null;
        try {
            callback = (ICallback)getVisitObject().getCallbackStack().peek();          
        }
        catch (Exception e) {
        }
        if (callback instanceof CollectionCallback) {
            ((CollectionCallback) callback).add(getModel());
        } else {
            save();
        }
    }

    /**
     * @param cycle
     */
    public void remove(IRequestCycle cycle) {
        ICallback callback = null;
        try {
            callback = (ICallback)getVisitObject().getCallbackStack().pop();           
        }
        catch (Exception e) {
        }

        if (callback instanceof CollectionCallback) {
            ((CollectionCallback) callback).remove(getModel());
        } else {
            try {
                if (getModel() instanceof Entity)
                    getEntityService().remove((Entity) getModel());
                else if (getModel() instanceof Act)
                    getActService().remove((Act) getModel());
                else if (getModel() instanceof Lookup)
                    getLookupService().remove((Lookup) getModel());
            } catch (Exception pe) {
                cycle.activate("Home");
            }
        }
        
        if (callback == null)
            cycle.activate("Home");
        else
            callback.performCallback(cycle);
    }

    /**
     * @param cycle
     */
    public void saveAndReturn(IRequestCycle cycle) {
        ICallback callback = null;
        try {
            callback = (ICallback)getVisitObject().getCallbackStack().pop();          
        }
        catch (Exception e){           
        }
        
        if (callback instanceof CollectionCallback) {
            ((CollectionCallback) callback).add(getModel());
        } else {
            if (!save())
                return;
        }
        if (callback == null)
            cycle.activate("Home");
        else
            callback.performCallback(cycle);
    }

    public void cancel(IRequestCycle cycle) {
        try {
            ICallback callback = (ICallback)getVisitObject().getCallbackStack().pop();
            if (callback == null)
                cycle.activate("Home");
            else
                callback.performCallback(cycle);
        } catch (Exception pe) {
            cycle.activate("Home");
        }
    }

    public void onFormSubmit(IRequestCycle cycle) {
        if (getNextPage() != null) {
            getDelegate().clearErrors();
            getNextPage().performCallback(cycle);
        }
    }

    public void onFormRefresh(IRequestCycle cycle) {
        getDelegate().clearErrors();
    }

    /**
     * @return
     */
    protected boolean save() {
        if (!getDelegate().getHasErrors()) {
            try {
                if (getModel() instanceof Entity)
                    getEntityService().save((Entity) getModel());
                else if (getModel() instanceof Act)
                    getActService().save((Act) getModel());
                else if (getModel() instanceof Lookup)
                    getLookupService().save((Lookup) getModel());
            } catch (Exception pe) {
                throw new ApplicationRuntimeException(pe);
                // ((OpenVpmsValidationDelegate)getDelegate()).record(pe);
                // return false;
            }
            return true;
        }
        return false;
    }

    /**
     * @return
     */
    public boolean isModelNew() {
        return ((IMObject) getModel()).isNew();
    }

    /**
     * @return
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        // This method can return one or more descriptors since it expects
        // a regular expression as the input
        ArchetypeDescriptor archetypeDescriptor = getArchetypeService()
                .getArchetypeDescriptor(
                        ((IMObject) getModel()).getArchetypeId());
        if (archetypeDescriptor == null)
            return getArchetypeService().getArchetypeDescriptor(
                    ((IMObject) getModel()).getArchetypeId().getShortName());
        else
            return archetypeDescriptor;
    }

    public IPropertySelectionModel getLookupModel(NodeDescriptor descriptor) {
        return new LookupSelectionModel(getLookupService().get(descriptor),
                !descriptor.isRequired());
    }

    public IPropertySelectionModel getArchetypeNamesModel(
            NodeDescriptor descriptor) {
        return new StringPropertySelectionModel(descriptor.getArchetypeRange());
    }

    public IPropertySelectionModel getEntityModel(NodeDescriptor descriptor) {
        // TODO need to work out how to get Entity selection models from node
        // descriptor
        // information and model data available to the page. This method should
        // populate
        // a special implementation of IPropertySelectionModel called
        // EntitySelectionModel
        // from the Entity service based on the Archetype constraints in the
        // descriptor.
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
