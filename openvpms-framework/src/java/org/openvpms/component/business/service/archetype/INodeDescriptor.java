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

package org.openvpms.component.business.service.archetype;


/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public interface INodeDescriptor extends IDescriptor
{
    /** */
    public static final int UNDEFINED_INDEX = -1;
    
    /** The default Maximum Length if one is not defined in the node definition */
    public static final int DEFAULT_MAX_LENGTH = 255;

    /** The default Display Length if one is not defined in the node definition */
    public static final int DEFAULT_DISPLAY_LENGTH = 50;

    /**
     * Is the Node hidden in the presentation view.
     * Note: Not deduced from the Archetype information but manipulated by other presentation
     * components. 
     * @return True or False
     */
    public boolean isHidden();
    
    /**
     * Method to allow users to set Node as hidden.
     * 
     * @param boolean hidden.  
     */
    public void setHidden(boolean hidden);
    
    /**
     * Method to indicate that Node is an indentifier. When creating the nodes for an Archetype
     * a node should be added for the generic IMObject uid identifier.  This will allow the id to 
     * be displayed when viewing or editing the Archetyped object even though the Id is not definined
     * as a real node in the Archetype.  
     * @return True or False
     */
    public boolean isIdentifier();

    /**
     * Method to indicate if Node is of a numeric type.  
     * 
     * @return True or False
     */
    public boolean isNumeric();

    /**
     * Method to indicate if Node is of a boolean type.  These will usually be rendered as 
     * check boxes.  
     * 
     * @return True or False
     */
    public boolean isBoolean();

    /**
     * Method to indicate if a Node is of a Date type.  
     * 
     * @return True or False
     */
    public boolean isDate();

    /**
     * Method to indicate if node is of a String type. 
     * @return True or False
     */
    public boolean isString();

    /**
     * Method to indicate if the node is an Object reference.  
     * An Object Reference is any node that references another oject subclassed from IMObject.  
     *  
     * @return True or False
     */
    public boolean isObjectReference();

    /**
     * Method to indicate if the node is a reference to a lookup object.  
     *  
     * @return True or False
     */
    public boolean isLookup();

    /**
     * Method to indicate if the node is a collection of other objects.  
     *  
     * @return True or False
     */
    public boolean isCollection();

    /**
     * Method to indicate if the node is required.
     * 
     * @return True or False.
     */
    public boolean isRequired();

    /**
     * Method to indicate if the node is read only.
     * Note:  Should be set to true for identifier nodes and any additional nodes were the values
     * are not set by the presentation layer but by the service layer during processing.  i.e timestamps, 
     * createduser etc
     *     
     * @return True or False
     */
    public boolean isReadOnly();

    /**
     * Method to allow ReadOnly status to be updated by the application. 
     * Note:   Only allow readOnlt to be false for truly mutable nodes.  
     * 
     * @param readOnly The readOnly to set.
     */
    public void setReadOnly(boolean readOnly);

    /**
     * Method to indicate if the node maximum length is large.
     * Note:  Should be set to true for string nodes where the display
     * length > DEFAULT_DISPLAY_LENGTH.  Presentation layer will utilise this to
     * decide whether to display as TextField or TextArea.
     *     
     * @return True or False
     */
    public boolean isLarge();

    /**
     * Method to get the length of the displayed field.
     * Note:  Not currently defined in archetype so set to minimum of maxlength or 
     * DEFAULT_DISPLAY_LENGTH.
     * Used for Strings or Numerics.
     *  
     * @return int displaylength
     */   
    public int getDisplayLength();
    
    /**
     * Method to allow application to set the length of the displayed field.
     * Note:  Do not allow display length to exceed Maximum length and should
     * set isLarge for String fields as per above.
     *
     * @param length
     */
    public void setDisplayLength(int length);   
    
    /**
     * Method to get the Minimum Length of a node.  If no minimum defined 
     * for node then return 0.  Only for String fields. 
     *  
     * @return Int minimum length
     */   
    public int getMinimumLength();
    
    /**
     * Method to get the maximum value of a node.  If no maximum defined 
     * for node then return ??.  Only for Numeric nodes.
     *  
     * @return Number maximum value
     */   
    public Number getMaximumValue();
    
    /**
     * Method to get the minimum value of a node.  If no minimum defined 
     * for node then return ??.  Only for numeric nodes.
     *  
     * @return Number minimum value
     */   
    public Number getMinimumValue();
    
    /**
     * Method to get a regular expression pattern for the node.
     * Only for String nodes.
     *  
     * @return String pattern
     */   
    public String getStringPattern();
    
    /**
     * Method to get the Archetype names associated with a particular Object Reference 
     * or Collection. Only for Object Reference and Collection nodes.
     *  
     * @return String pattern
     */   
    public String[] getArchetypeNames();
    
}