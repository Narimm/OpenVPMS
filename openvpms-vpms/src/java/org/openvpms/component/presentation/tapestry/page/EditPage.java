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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.callback.ICallback;
import org.apache.tapestry.components.Block;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.apache.tapestry.form.StringPropertySelectionModel;
import org.apache.tapestry.form.validator.BaseValidator;
import org.apache.tapestry.form.validator.Max;
import org.apache.tapestry.form.validator.MaxLength;
import org.apache.tapestry.form.validator.Min;
import org.apache.tapestry.form.validator.Pattern;
import org.apache.tapestry.form.validator.Required;
import org.apache.tapestry.valid.ValidationConstraint;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
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

    // These methods represent page properties managed by Tapestry

    @Persist("client")
    public abstract Integer getObjectId();
    public abstract void setObjectId(Integer bookId);

    @Persist("session")
    public abstract Object getModel();
    public abstract void setModel(Object model);

    @Persist("session")
    public abstract String getArchetypeRange();
    public abstract void setArchetypeRange(String archetypeRange);  

    @Persist("session")
    public abstract String getCurrentArchetypeName();
    public abstract void setCurrentArchetypeName(String name);
    
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
    
    // The private list of selected collection table entries for deletion
    private List selected = new ArrayList();

    // Push a Edit Page Callback
    public void pushCallback()
    {
        Visit visit = (Visit)getVisitObject();
        visit.getCallbackStack().push(new EditCallback(getPageName(), getModel()));
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
    public void onMainOK(IRequestCycle cycle) {
        if (save()){
            ICallback callback = null;
            try {
                callback = (ICallback)getVisitObject().getCallbackStack().pop();          
                if (callback == null)
                    cycle.activate("Home");
                else
                    callback.performCallback(cycle);           
            }
            catch (Exception e){           
                cycle.activate("Home");
            }           
        }
    }

    public void onMainCancel(IRequestCycle cycle) {
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

    public void onMainSubmit(IRequestCycle cycle) {
        // If we have a indirection to another page then goto that page 
        if (getNextPage() != null) {
            // Clear any Validation errors
            getDelegate().clearErrors();
            getNextPage().performCallback(cycle);
        }
    }

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
        
        //Now use Archetype service to Validate the object
        //TODO need list of errors returned from validator to populate in delegator
        try {
            getArchetypeService().validateObject((IMObject)getModel());
            }
        catch (ValidationException e){ 
            getDelegate().setFormComponent(null);
            List<ValidationError> errors = e.getErrors();
            for (ValidationError error: errors) {
                getDelegate().record(error.getNodeName() + ' ' + error.getErrorMessage(),
                        ValidationConstraint.CONSISTENCY);                
            }
            return false;           
        }
        // Check to see if we are coming from editing a collection or not
        ICallback callback = null;
        try {
            callback = (ICallback)getVisitObject().getCallbackStack().peek();          
        }
        catch (Exception e) {
            // ignore error just means we have a empty stack so callback is null
        }
        // If collection call the callbacks add method with the model
        if (callback instanceof CollectionCallback) {
            ((CollectionCallback) callback).add(getModel());
        } else {
            try {
                if (getModel() instanceof Entity)
                    getEntityService().save((Entity) getModel());
                else if (getModel() instanceof Act)
                    getActService().save((Act) getModel());
                else if (getModel() instanceof Lookup)
                    getLookupService().save((Lookup) getModel());
            } catch (Exception pe) {
                ((OpenVpmsValidationDelegate)getDelegate()).record(pe);
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
     * @return
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        ArchetypeDescriptor archetypeDescriptor = getArchetypeService()
                .getArchetypeDescriptor(
                        ((IMObject) getModel()).getArchetypeId());
        if (archetypeDescriptor == null)
            return getArchetypeService().getArchetypeDescriptor(
                    ((IMObject) getModel()).getArchetypeId().getShortName());
        else
            return archetypeDescriptor;
    }

    /**
     * @return
     */
    public List getDescriptors() {
        if (getNodeNames() == null || getNodeNames().length == 0) {
            return getArchetypeDescriptor().getAllNodeDescriptors();
        } else {
            return getArchetypeDescriptor().getNodeDescriptors(
                    getNodeNames());
        }
    }
    
    public List getSimpleDescriptors() {
        if (getNodeNames() == null || getNodeNames().length == 0) {
            return getArchetypeDescriptor().getSimpleNodeDescriptors();
        } else {
            return getArchetypeDescriptor().getNodeDescriptors(
                    getNodeNames());
        }
    }
    
    public List getComplexDescriptors() {
        if (getNodeNames() == null || getNodeNames().length == 0) {
            return getArchetypeDescriptor().getComplexNodeDescriptors();
        } else {
            return getArchetypeDescriptor().getNodeDescriptors(
                    getNodeNames());
        }
    }
    
    public IPropertySelectionModel getLookupModel(NodeDescriptor descriptor) {
        return new LookupSelectionModel(getLookupService().get(descriptor,(IMObject)getModel()),
                !descriptor.isRequired());
    }

    public IPropertySelectionModel getArchetypeNamesModel(
            NodeDescriptor descriptor) {
        if (descriptor == null)
            return new StringPropertySelectionModel(new String[]{""});
        else
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
        String entityName = StringUtils.capitalize(((IMObject)getModel()).getArchetypeId().getEntityName());
        if (isModelNew()) {
            return "New " + entityName;
        } else {
            return "Edit " + entityName;
        }
    }

    /**
     * @param propertyName
     * @return
     */
    public boolean hasBlock(String propertyName) {
        if (getPage().getComponents().containsKey(propertyName))
            return true;
        else
            return false;
    }

    /**
     * @param propertyName
     * @return
     */
    public Block getBlock(String propertyName) {
        if (getPage().getComponents().containsKey(propertyName))
            return (Block) getPage().getComponent(propertyName);
        else
            return null;
    }
    /**
     * 
     * TODO Look at this when we get into tapestry
     * 
     * @param descriptor
     * @return IValidator
     * @throws Exception
     *             propagate exception
     */
    public List getValidators(NodeDescriptor descriptor) throws Exception {
        BaseValidator validator = null;
        
        List<BaseValidator> validators = new ArrayList<BaseValidator>();

        if (descriptor.isRequired()) {
            validator = new Required();
            validators.add(validator);
        }
        if (descriptor.isNumeric()) {
            validator = new Pattern();
            ((Pattern)validator).setPattern("#");
            validators.add(validator);
            if (descriptor.getMaxValue() != null) {
                validator = new Max(descriptor.getMaxValue().toString());
                validators.add(validator);
            }

            if (descriptor.getMinValue() != null) {
                validator = new Min(descriptor.getMinValue().toString());
                validators.add(validator);
            }
        } else if  (descriptor.isDate()){
        } else if (descriptor.isString()) {
            if (descriptor.getMaxLength() > 0) {
                validator = new MaxLength();
                ((MaxLength)validator).setMaxLength(descriptor.getMaxLength());
                validators.add(validator);
            }
            if (descriptor.getStringPattern() != null) {
                validator = new Pattern();
                ((Pattern)validator).setPattern(descriptor.getStringPattern());
                validators.add(validator);
            }
        }
        
        return validators;
    }
}
