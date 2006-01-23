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


package org.openvpms.component.business.domain.im.archetype.descriptor;

// java core
import java.lang.reflect.Method;
import java.util.TreeSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * This is used to define the assertion type. It is used to map an assertion to
 * its type information
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class AssertionTypeDescriptor extends Descriptor {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * This is the fully qualified archetypeof the object used to collect 
     * property information for this assertion type
     */
    private String propertyArchetype;
    
    /**
     * A list of actions associated with this assertion type
     */
    private Set<ActionTypeDescriptor> actionTypes =
        new TreeSet<ActionTypeDescriptor>();
    
    /**
     * Default constructor
     */
    public AssertionTypeDescriptor() {
        setArchetypeId(new ArchetypeId("openvpms-system-descriptor.assertionType.1.0"));
    }

    /**
     * @return Returns the actionTypes.
     */
    public Set<ActionTypeDescriptor> getActionTypes() {
        return actionTypes;
    }

    /**
     * @param actionTypes The actionTypes to set.
     */
    public void setActionTypes(Set<ActionTypeDescriptor> actionTypes) {
        this.actionTypes = actionTypes;
    }

    /**
     * Retrieve the action types as an array
     * 
     * @return ActionTypeDescriptor[]
     */
    public ActionTypeDescriptor[] getActionTypesAsArray() {
        return (ActionTypeDescriptor[])actionTypes.toArray(
                new ActionTypeDescriptor[actionTypes.size()]);
    }
    
    /**
     * Set the following array of action types
     * 
     * @param actions
     */
    public void setActionTypesAsArray(ActionTypeDescriptor[] actions) {
        for (ActionTypeDescriptor action : actions) {
            addActionType(action);
        }
    }
    
    /**
     * Add an action type
     * 
     * @param actionType
     *            the action type to add
     */
    public void addActionType(ActionTypeDescriptor actionType) {
        this.actionTypes.add(actionType);
    }
    
    /**
     * Retrieve the {@link ActionType} with the specified name or null if 
     * one doesn't exist
     * 
     * @param name
     *            the action name
     * @return ActionTypeDescriptor            
     */
    public ActionTypeDescriptor getActionType(String name) {
        for (ActionTypeDescriptor actionType : actionTypes) {
            if (actionType.getName().equals(name)) {
                return actionType;
            }
        }
        
        return null;
    }
    
    /**
     * Remove the specified action type
     * 
     * @param actionType
     *            the action type to remove
     */
    public void removeActionType(ActionTypeDescriptor actionType) {
        this.actionTypes.remove(actionType);
    }
    
    
    /**
     * @return Returns the propertyArchetype.
     */
    public String getPropertyArchetype() {
        return propertyArchetype;
    }

    /**
     * @param propertyArchetypeQName The propertyArchetypeQName to set.
     */
    public void setPropertyArchetype(String propertyArchetype) {
        this.propertyArchetype = propertyArchetype;
    }

    /**
     * This method will execute the specified action against the nominated
     * node and assertion descriptor and will return the a result to the 
     * caller.
     * <p>
     * The caller is responsible for casting the result to the appropriate type.
     * 
     * @param action
     *            the name of the action.      
     * @param target
     *            this is the object that is the subject of the assetion
     * @param node
     *            the node descriptor            
     * @param assertion
     *            this is the assertion obect holds the parameters to the 
     *            method call
     * @return Object
     *            the result form the action or null if a result is not 
     *            applicable
     * @throws AssertionException
     *            a runtime exception that is raised if the assertion cannot
     *            be evaluated.
     */
    public Object evaluateAction(String action, Object target, 
            NodeDescriptor node, AssertionDescriptor assertion) {
        ActionTypeDescriptor descriptor = getActionTypeDescriptorByName(action);
        if (descriptor == null) {
            throw new AssertionException(
                    AssertionException.ErrorCode.ActionNoSupportedByAssertion,
                    new Object[] {action, this.getName()});
        }
        
        try {
            Class clazz = Thread.currentThread()
                .getContextClassLoader().loadClass(descriptor.getClassName());
            Method method = clazz.getMethod(descriptor.getMethodName(), 
                    new Class[]{Object.class, NodeDescriptor.class, 
                                AssertionDescriptor.class});
            
            return method.invoke(null, new Object[]{target, node, assertion});
        } catch (Exception exception) {
            throw new AssertionException(
                    AssertionException.ErrorCode.FailedToApplyAssertion,
                    new Object[] {action, this.getName()},
                    exception);
            
        }
    }
    
    /**
     * This method will evaluate the assetion against the target object and
     * return true if the assertion holds and false otherwise.
     * <p>
     * The declared assertion is transformed into a method call, which takes the
     * target object as one parameter and a map of properties as the other 
     * parameter. 
     * 
     * @param target
     *            this is the object that is the subject of the assetion
     * @param node
     *            the node descriptor            
     * @param assertion
     *            this is the assertion obect holds the parameters to the 
     *            method call
     * @return boolean
     * @throws AssertionException
     *            a runtime exception that is raised if the assertion cannot
     *            be evaluated.
     */
    public boolean assertTrue(Object target, NodeDescriptor node, 
        AssertionDescriptor assertion) {
        try {
            ActionTypeDescriptor actionType = getActionType("assert");
            if (actionType == null) {
                throw new AssertionException(
                        AssertionException.ErrorCode.NoActionTypeSpecified,
                        new Object[]{ "assert", getName()});
            }
            
            Class clazz = Thread.currentThread()
                .getContextClassLoader().loadClass(actionType.getClassName());
            Method method = clazz.getMethod(actionType.getMethodName(), 
                    new Class[]{Object.class, NodeDescriptor.class, 
                                AssertionDescriptor.class});
            
            return ((Boolean)method.invoke(null, 
                    new Object[]{target, node, assertion})).booleanValue();
        } catch (Exception exception) {
            throw new AssertionException(
                    AssertionException.ErrorCode.FailedToApplyAssertion,
                    new Object[] {"assert", getName()},
                    exception);
            
        }
    }
    
    /**
     * Return the {@link ActionTypeDescriptor} with the specified name or
     * null if one does not exist.
     * 
     * @param action
     *            the name of the action
     * @return ActionTypeDescriptor            
     */
    private ActionTypeDescriptor getActionTypeDescriptorByName(String action) {
        if (StringUtils.isEmpty(action)) {
            return null;
        }
        
        ActionTypeDescriptor descriptor = null;
        for (ActionTypeDescriptor atype : actionTypes) {
            if (atype.getName().equals(action)) {
                descriptor = atype;
                break;
            }
        }
        
        return descriptor;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.Descriptor#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        AssertionTypeDescriptor copy = (AssertionTypeDescriptor)super.clone();
        copy.actionTypes = new TreeSet<ActionTypeDescriptor>(this.actionTypes);
        copy.propertyArchetype = this.propertyArchetype;
        
        return copy;
    }
}
