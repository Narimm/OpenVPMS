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

// java core
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.callback.ICallback;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.apache.tapestry.valid.ValidationConstraint;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.presentation.tapestry.Visit;
import org.openvpms.component.presentation.tapestry.callback.CollectionCallback;
import org.openvpms.component.presentation.tapestry.callback.EditCallback;
import org.openvpms.component.presentation.tapestry.component.LookupSelectionModel;
import org.openvpms.component.presentation.tapestry.component.Utils;
import org.openvpms.component.presentation.tapestry.validation.OpenVpmsValidationDelegate;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public abstract class EditPage extends OpenVpmsPage {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(EditPage.class);

    // These methods represent page properties managed by Tapestry

    @Persist("session")
    public abstract Integer getObjectId();

    public abstract void setObjectId(Integer bookId);

    @Persist("session")
    public abstract Object getModel();

    public abstract void setModel(Object model);

    public abstract String getCurrentActiveTab();

    public abstract void setCurrentActiveTab(String name);

    public abstract ICallback getNextPage();

    public abstract void setNextPage(ICallback NextPage);

    public abstract String[] getNodeNames();

    public abstract void setNodeNames(String[] nodeNames);

    public abstract Object getCurrentObject();

    public abstract void setCurrentObject(Object CurrentObject);

    public abstract NodeDescriptor getDescriptor();

    public abstract void setDescriptor(NodeDescriptor descriptor);

    // The Validation delegate injected by Tapestry
    @Bean
    public abstract OpenVpmsValidationDelegate getDelegate();

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void pushCallback() {
        Visit visit = (Visit) getVisitObject();
        visit.getCallbackStack().push(
                new EditCallback(getPageName(), getModel()));
    }

    /**
     * @param cycle
     */
    public void onMainApply(IRequestCycle cycle) {
        save();
    }

    /**
     * @param cycle
     */
    public void onMainRemove(IRequestCycle cycle) {
        ICallback callback = null;
        try {
            callback = (ICallback) getVisitObject().getCallbackStack().pop();
        } catch (Exception e) {
        }

        if (callback instanceof CollectionCallback) {
            ((CollectionCallback) callback).remove(getModel());
        } else {
            try {
                getArchetypeService().remove((IMObject) getModel());
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
    public void onMainOK(IRequestCycle cycle) {
        if (save()) {
            ICallback callback = null;
            try {
                callback = (ICallback) getVisitObject().getCallbackStack()
                        .pop();
                if (callback == null)
                    cycle.activate("Home");
                else
                    callback.performCallback(cycle);
            } catch (Exception e) {
                cycle.activate("Home");
            }
        }
    }

    /**
     * 
     * @param cycle
     */
    public void onMainCancel(IRequestCycle cycle) {
        try {
            ICallback callback = (ICallback) getVisitObject()
                    .getCallbackStack().pop();
            if (callback == null)
                cycle.activate("Home");
            else
                callback.performCallback(cycle);
        } catch (Exception pe) {
            cycle.activate("Home");
        }
    }

    /**
     * 
     * @param cycle
     */
    public void onMainSubmit(IRequestCycle cycle) {
        // If we have a indirection to another page then goto that page
        if (getNextPage() != null) {
            // Clear any Validation errors
            getDelegate().clearErrors();
            getNextPage().performCallback(cycle);
        }
    }

    /**
     * 
     * @param cycle
     */
    public void onMainRefresh(IRequestCycle cycle) {
        // If we are only refreshing then clear any validation errors
        getDelegate().clearErrors();
    }

    /**
     * @return
     */
    protected boolean save() {
        // Check the delegator for errors first
        if (getDelegate().getHasErrors())
            return false;

        // Now use Archetype service to Validate the object
        // TODO need list of errors returned from validator to populate in
        // delegator
        try {
            getArchetypeService().validateObject((IMObject) getModel());
        } catch (ValidationException e) {
            getDelegate().setFormComponent(null);
            List<ValidationError> errors = e.getErrors();
            for (ValidationError error : errors) {
                getDelegate().record(
                        error.getNodeName() + ' ' + error.getErrorMessage(),
                        ValidationConstraint.CONSISTENCY);
            }
            return false;
        }
        // Check to see if we are coming from editing a collection or not
        ICallback callback = null;
        try {
            callback = (ICallback) getVisitObject().getCallbackStack().peek();
        } catch (Exception e) {
            // ignore error just means we have a empty stack so callback is null
        }
        // If collection call the callbacks add method with the model
        if (callback instanceof CollectionCallback) {
            ((CollectionCallback) callback).add(getModel());
        } else {
            try {
                getArchetypeService().save((IMObject) getModel());
            } catch (Exception pe) {
                ((OpenVpmsValidationDelegate) getDelegate()).record(pe);
                return false;
            }
        }
        // If we get to here then all is well
        return true;
    }

    /**
     * @return
     */
    public boolean isModelNew() {
        return ((IMObject) getModel()).isNew();
    }

    /**
     * This will return the archetype descriptor for the object that will
     * be rendered by this edit page.
     * <p>
     * It will locate the {@link ArchetypeDescriptor} for the specified object.
     * <p>
     * TODO At the moment we have the logic to determine whether the descriptor
     * is an AssertionTypeDescriptor and then switch accordingly in this object.
     * This needs to be transparent
     * 
     * @return ArchetypeDescriptor
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        IMObject imObj = (IMObject)getModel();

        
        ArchetypeDescriptor archetypeDescriptor = null;
        ArchetypeId archId = imObj.getArchetypeId();
        
        //TODO This is a work around until we resolve the current 
        // problem with archetyping and archetype. We need to 
        // extend this page and create a new archetype specific 
        // edit page.
        if (imObj instanceof AssertionDescriptor) {
           AssertionTypeDescriptor atDesc = getArchetypeService()
               .getAssertionTypeDescriptor(imObj.getName()); 
           archId = new ArchetypeId(atDesc.getPropertyArchetype());
        }
        
        archetypeDescriptor = getArchetypeService().getArchetypeDescriptor(archId);
        if (logger.isDebugEnabled()) {
            logger.debug("Returning archetypDescriptor: " 
                    + (archetypeDescriptor == null ? null :archetypeDescriptor.getName())
                    + " for archId: " + archId 
                    + " and object: " + imObj.getClass().getName());
        }
        
        if (archetypeDescriptor == null) {
            return getArchetypeService().getArchetypeDescriptor(
                    imObj.getArchetypeId().getShortName());
        } else {
            return archetypeDescriptor;
        }
    }

    /**
     * @return
     */
    public List getDescriptors() {
        if (getNodeNames() == null || getNodeNames().length == 0) {
            return getArchetypeDescriptor().getAllNodeDescriptors();
        } else {
            return getArchetypeDescriptor().getNodeDescriptors(getNodeNames());
        }
    }

    /**
     * 
     * @return
     */
    public List getSimpleDescriptors() {
        if (getNodeNames() == null || getNodeNames().length == 0) {
            return getArchetypeDescriptor().getSimpleNodeDescriptors();
        } else {
            return getArchetypeDescriptor().getNodeDescriptors(getNodeNames());
        }
    }

    /**
     * 
     * @return
     */
    public List getComplexDescriptors() {
        if (getNodeNames() == null || getNodeNames().length == 0) {
            return getArchetypeDescriptor().getComplexNodeDescriptors();
        } else {
            return getArchetypeDescriptor().getNodeDescriptors(getNodeNames());
        }
    }

    /**
     * 
     * @param descriptor
     * @return
     */
    public IPropertySelectionModel getLookupModel(NodeDescriptor descriptor) {
        return new LookupSelectionModel(getLookupService().get(descriptor,
                (IMObject) getModel()), !descriptor.isRequired());
    }

    /**
     * 
     * @param descriptor
     * @return
     */
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
        
        ArchetypeId archId = ((IMObject) getModel()).getArchetypeId();
        String conceptName = null;
        
        if (archId == null) {
            conceptName = Utils.unCamelCase(getModel().getClass().getName());
        } else {
            conceptName = Utils.unCamelCase(archId.getConcept());
        }
            
        if (isModelNew()) {
            return "New " + conceptName;
        } else {
            return "Edit " + conceptName;
        }
    }

    /**
     * 
     * @return
     */
    public boolean isCurrentTabActive() {
        String currenttab = getCurrentActiveTab();
        String displayname = getDescriptor().getDisplayName();
        if (currenttab == null || currenttab.length() == 0) {
            setCurrentActiveTab(displayname);
            return true;
        } else if (displayname.equals(currenttab))
            return true;
        else
            return false;
    }

    /**
     * 
     * @param displayName
     */
    public void onTabClicked(String displayName) {
        setCurrentActiveTab(displayName);
    }
}
