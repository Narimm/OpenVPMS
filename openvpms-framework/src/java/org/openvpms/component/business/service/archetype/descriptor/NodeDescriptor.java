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

package org.openvpms.component.business.service.archetype.descriptor;

// java core
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

// commons-lang
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

// log4j
import org.apache.log4j.Logger;
import org.apache.oro.text.perl.Perl5Util;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NodeDescriptor implements Serializable {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(NodeDescriptor.class);
    
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name given to the object id node
     */
    public static final String IDENTIFIER_NODE_NAME = "uid";
    
    /**
     * This is used to identify a max cardinality that is unbounded
     */
    public static final int UNBOUNDED = -1;
    public static final String UNBOUNDED_AS_STRING = "*";
    
    /** 
     * The default maximum Length if one is not defined in the node definition 
     */
    public static final int DEFAULT_MAX_LENGTH = 255;

    /** 
     * The default display length if one is not defined in the node definition 
     */
    public static final int DEFAULT_DISPLAY_LENGTH = 50;

    /**
     * The display name or well known name of the node. This must be unique
     * within the set of nodeDescriptors for an {@link ArchetypeDescriptor}
     */
    private String name;

    /**
     * This is the display name, which is only supplied if it is different to
     * the node name
     */
    private String displayName;

    /**
     * Attribute, which defines whether this node is hidden or can be displayed
     */
    private boolean isHidden = false;
    
    /**
     * Determine whether the value for this node is derived
     */
    private boolean isDerived = false;

    /**
     * This is a jxpath expression, which is used to determine the 
     * value of the node
     */
    private String derivedValue;
    
    /**
     * The XPath/JXPath expression that is used to resolve this node within the
     * associated domain object.
     */
    private String path;

    /**
     * The fully qualified class name that defines the node type
     */
    private String type;

    /**
     * The default value
     */
    private String defaultValue;

    /**
     * The minimum cardinality, which defaults to 0
     */
    private int minCardinality = 0;

    /**
     * The maximum cardinality, which defaults to 1
     */
    private int maxCardinality = 1;

    /**
     * The minimum length
     */
    private int minLength;
    
    /**
     * The maximum length
     */
    private int maxLength;
    
    /**
     * This is an option property, which is required for nodes that 
     * represent collections. It is the name that denotes the individual
     * elements stored in the collection.
     */
    private String baseName;

    /**
     * Contains a list of {@link AssertionDescriptor} instances
     */
    private Map<String, AssertionDescriptor> assertionDescriptors = 
        new LinkedHashMap<String, AssertionDescriptor>();

    /**
     * A node can have other nodeDescriptors to define a nested structure
     */
    private Map<String, NodeDescriptor> nodeDescriptors = 
        new LinkedHashMap<String, NodeDescriptor>();

    /**
     * Cache the clazz
     */
    private transient Class clazz;

    /**
     * Default constructor
     */
    public NodeDescriptor() {
    }

    /**
     * @return Returns the maxCardinality.
     */
    public int getMaxCardinality() {
        return maxCardinality;
    }

    /**
     * @param maxCardinality
     *            The maxCardinality to set.
     */
    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }
    
    /**
     * This setter enabled the user to specify an unbounded maximum
     * collection using '*'. 
     * 
     * @param maxCardinality
     *            The maxCardinality to set.
     */
    public void setMaxCardinalityAsString(String maxCardinality) {
        if (maxCardinality.equals(UNBOUNDED_AS_STRING)) {
            setMaxCardinality(UNBOUNDED);
        } else {
            setMaxCardinality(Integer.parseInt(maxCardinality));
        }
    }
    
    /**
     * The getterthat returns the max cardinality as a string
     * 
     * @return String
     */
    public String getMaxCardinalityAsString() {
        if (maxCardinality == UNBOUNDED) {
            return UNBOUNDED_AS_STRING;
        } else {
            return Integer.toString(maxCardinality);
        }
    }
    
    /**
     * @return Returns the minCardinality.
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /**
     * @param minCardinality
     *            The minCardinality to set.
     */
    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return StringUtils.isEmpty(displayName) ? unCamelCase(getName()) : displayName;
    }

    /**
     * @param displayName
     *            The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return Returns the isHidden.
     */
    public boolean isHidden() {
        return isHidden;
    }

    /**
     * @param isHidden
     *            The isHidden to set.
     */
    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    /**
     * @return Returns the isDerived.
     */
    public boolean isDerived() {
        return isDerived;
    }

    /**
     * @return Returns the derivedValue.
     */
    public String getDerivedValue() {
        return derivedValue;
    }

    /**
     * @param derivedValue The derivedValue to set.
     */
    public void setDerivedValue(String derivedValue) {
        this.derivedValue = derivedValue;
    }

    /**
     * @param isDerived The isDerived to set.
     */
    public void setDerived(boolean isDerived) {
        this.isDerived = isDerived;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return Returns the typeName.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(String type) {
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(
                    type);
            this.type = type;
        } catch (Exception exception) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.InvalidType,
                    new Object[] { type }, exception);
        }
    }

    /**
     * @return Returns the defaultValue.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return Returns the minLength.
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * @param minLength The minLength to set.
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * @return Returns the maxLength.
     */
    public int getMaxLength() {
        return maxLength <= 0 ? DEFAULT_MAX_LENGTH : maxLength;
    }

    /**
     * @param maxLength
     *            The maxLength to set.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @param defaultValue
     *            The defaultValue to set.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return Returns the baseName.
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * @param baseName The baseName to set.
     */
    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /**
     * Return the assertion descriptors as a map
     * 
     * @return Returns the assertionDescriptors.
     */
    public Map<String, AssertionDescriptor> getAssertionDescriptorsAsMap() {
        return assertionDescriptors;
    }

    /**
     * Return the assertion descriptors as a map
     */
    public AssertionDescriptor[] getAssertionDescriptors() {
        return (AssertionDescriptor[])assertionDescriptors.values().toArray(
                new AssertionDescriptor[assertionDescriptors.size()]);
    }

    /**
     * @param assertionDescriptors
     *            The assertionDescriptors to set.
     */
    public void setAssertionDescriptors(AssertionDescriptor[] assertionDescriptors) {
        this.assertionDescriptors = new LinkedHashMap<String, AssertionDescriptor>();
        for (AssertionDescriptor descriptor : assertionDescriptors) {
            this.assertionDescriptors.put(descriptor.getType(), descriptor);
        }
    }

    /**
     * @return Returns the nodeDescriptors.
     */
    public NodeDescriptor[] getNodeDescriptors() {
        return (NodeDescriptor[])nodeDescriptors.values().toArray(
                new NodeDescriptor[nodeDescriptors.size()]);
    }

    /**
     * Return the {@link NodeDescriptor} instances as a map of name and 
     * descriptor
     * @return Returns the nodeDescriptors.
     */
    public Map<String, NodeDescriptor> getNodeDescriptorsAsMap() {
        return this.nodeDescriptors;
    }

    /**
     * @param nodeDescriptors The nodeDescriptors to set.
     */
    public void setNodeDescriptors(NodeDescriptor[] nodeDescriptors) {
        this.nodeDescriptors = new LinkedHashMap<String, NodeDescriptor>();
        for (NodeDescriptor descriptor : nodeDescriptors) {
            this.nodeDescriptors.put(descriptor.getName(), descriptor);
        }
    }

    /**
     * Indicates that this node defines an identifier. When creating the nodes
     * for an Archetype a node should be added for the generic IMObject uid
     * identifier. This will allow the id to be displayed when viewing or
     * editing the Archetyped object even though the Id is not definined as a
     * real node in the Archetype.
     * 
     * @return boolean
     */
    public boolean isIdentifier() {
        return getName().equals(IDENTIFIER_NODE_NAME);

    }

    /**
     * Check whether this node is a numeric type.
     * 
     * @return boolean
     */
    public boolean isNumeric() {

        if ((Number.class.isAssignableFrom(clazz)) || 
            (byte.class == clazz) || 
            (short.class == clazz) || 
            (int.class == clazz) || 
            (long.class == clazz) || 
            (float.class == clazz) || 
            (double.class == clazz)) {
            return true;
        }

        return false;
    }

    /**
     * Check whether this node is a boolean type.
     * 
     * @return boolean
     */
    public boolean isBoolean() {
        if ((Boolean.class == clazz) || 
            (boolean.class == clazz)) {
            return true;
        }

        return false;
    }

    /**
     * Check whether this node a date type.
     * 
     * @return boolean
     */
    public boolean isDate() {
        if ((Date.class == clazz) || 
            (DvDateTime.class == clazz)) {
            return true;
        }

        return false;
    }

    /**
     * Check whether this node is a string
     * 
     * @return boolean
     */
    public boolean isString() {
        if (String.class == clazz) {
            return true;
        }
        
        return false;
    }

    /**
     * Check whether this node is an object reference. An object reference is a
     * node that references another oject subclassed from IMObject.
     * 
     * @return boolean
     */
    public boolean isObjectReference() {
        if (IMObject.class.isAssignableFrom(clazz)) {
            return true;
        }
        
        return false;
    }

    /**
     * Check whether this node is a lookup
     * 
     * @return boolean
     */
    public boolean isLookup() {
        boolean result = false;
        for (String key : assertionDescriptors.keySet()) {
            if (key.startsWith("lookup")) {
                result = true;
                break;
            }
        }
        
        return result;
        
    }

    /**
     * Check whether this node is a collection
     * 
     * @return boolean
     */
    public boolean isCollection() {
        try {
            return Collection.class.isAssignableFrom(Thread.currentThread()
                .getContextClassLoader().loadClass(getType()));
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * Check whether this node is mandatory.
     * 
     * @return boolean
     */
    public boolean isRequired() {
        return getMinCardinality() > 0;
    }

    /**
     * Method to indicate if the node is read only. Note: Should be set to true
     * for identifier nodes and any additional nodes were the values are not set
     * by the presentation layer but by the service layer during processing. i.e
     * timestamps, createduser etc
     * 
     * @return boolean
     */
    public boolean isReadOnly() {
        return isIdentifier();
    }

    /**
     * Check whether the node maximum length is large. Should be set to true for
     * string nodes where the display length > DEFAULT_DISPLAY_LENGTH.
     * Presentation layer will utilise this to decide whether to display as
     * TextField or TextArea.
     * 
     * @return boolean
     */
    public boolean isLarge() {
        return getMaxLength() > DEFAULT_MAX_LENGTH;
    }

    /**
     * Return the length of the displayed field. Not currently defined in
     * archetype so set to minimum of maxlength or DEFAULT_DISPLAY_LENGTH. Used
     * for Strings or Numerics.
     * 
     * @return int the display length
     */
    public int getDisplayLength() {
        return DEFAULT_DISPLAY_LENGTH;
    }

    /**
     * Return the maximum value of the node. If no maximum defined for node then
     * return 0. Only valid for numeric nodes.
     * 
     * @return Number 
     *            the minimum value
     * @throws DescriptorException
     *            a runtim exception            
     */
    public Number getMaxValue() {
        Number number = NumberUtils.createNumber("0");
        if (isNumeric()) {
            AssertionDescriptor descriptor = (AssertionDescriptor)
                assertionDescriptors.get("numericRange");
            if (descriptor != null) {
             number = NumberUtils.createNumber((String)
                     descriptor.getPropertiesAsMap().get("maxValue").getValue());    
            }
        } else {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.UnsupportedOperation,
                    new Object[] {"getMaxValue", getType()});
        }
        
        return number;
    }

    /**
     * Return the minimum value of the node. If no minimum defined for node then
     * return 0. Only valid for numeric nodes.
     * 
     * @return Number 
     *            the minimum value
     * @throws DescriptorException
     *            a runtim exception            
     */
    public Number getMinValue() {
        Number number = NumberUtils.createNumber("0");
        if (isNumeric()) {
            AssertionDescriptor descriptor = (AssertionDescriptor)
                assertionDescriptors.get("numericRange");
            if (descriptor != null) {
             number = NumberUtils.createNumber((String)
                     descriptor.getPropertiesAsMap().get("minValue").getValue());    
            }
        } else {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.UnsupportedOperation,
                    new Object[] {"getMinValue", getType()});
        }
        
        return number;
    }

    /**
     * Return the regular expression associated with the node. Only valid for
     * string nodes.
     * 
     * @return String regular expression pattern
     */
    public String getStringPattern() {
        String expression = null;
        if (isString()) {
            AssertionDescriptor descriptor = (AssertionDescriptor)
                assertionDescriptors.get("regularExpression");
            if (descriptor != null) {
             expression = (String)descriptor.getPropertiesAsMap()
                 .get("expression").getValue();    
            }
        } else {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.UnsupportedOperation,
                    new Object[] {"getMinValue", getType()});
        }
        
        return expression;
    }

    /**
     * Return the archetype names associated with a particular object reference
     * or collection.
     * 
     * @return String pattern
     */
    @SuppressWarnings("unchecked")
    public String[] getArchetypeNames() {
        AssertionDescriptor descriptor = (AssertionDescriptor)
            assertionDescriptors.get("validArchetypes");
        if (descriptor != null) {
            return (String[])descriptor.getPropertiesAsMap().keySet().toArray(
                    new String[descriptor.getPropertiesAsMap().size()]);
        } else {
            return new String[0];
        }        
    }
    
    /**
     * Return an array of short names or short name regular expression that
     * are associated with the archetypeRange assertion. If the node does
     * not have such an assertion then return a zero length string array
     * 
     * TODO Should we more this into a utility class
     * 
     * @return String[]
     *            the array of short names
     */
    public String[] getArchetypeRange() {
        if (assertionDescriptors.containsKey("archetypeRange")) {
            ArrayList<String> range = new ArrayList<String>();
            AssertionDescriptor desc = assertionDescriptors.get("archetypeRange");
            for (AssertionProperty property : desc.getProperties()) {
                range.add(property.getKey());
            }
            
            return (String[])range.toArray(new String[range.size()]);
        } else {
            return new String[0];
        }
    }
    
    /**
     * This will return the node value for the supplied {@link IMObject}. If 
     * the node is derived then it will return the derived value. If the node is 
     * not dervied then it will use the path to return the value.
     * 
     * @param context
     *            the context object to work from
     * @return Object
     *            the returned object            
     */
    public Object getValue(IMObject context) {
        if (isDerived()) {
            return JXPathContext.newContext(context).getValue(getDerivedValue());
        } else {
            return JXPathContext.newContext(context).getValue(getPath());
        }
    }

    /**
     * Check whether this assertion type is defined for this node
     * 
     * @param type
     *            the assertion type
     * @return boolean
     */
    public boolean containsAssertionType(String type) {
        return assertionDescriptors.containsKey(type);
    }
    
    /**
     * Indicates if this node is acomplex node. If the node has
     * an archetypeRange assertion or the node has a cardinality > 1 then 
     * the node is deemed to be a complex node
     * 
     * @return boolean
     *            true if complex
     */
    public boolean isComplexNode() {
        return (getMaxCardinality() == NodeDescriptor.UNBOUNDED) ||
               (getMaxCardinality() > 1) || 
               (containsAssertionType("archetypeRange"));
    }

    /**
     * This method will uncamel case the speified string and return
     * it to the caller
     * 
     * @param name
     *            the camel cased strig
     * @return String
     */
    private String unCamelCase(String name) {
        ArrayList<String> words = new ArrayList<String>();
        Perl5Util perl = new Perl5Util();

        while (perl.match("/(\\w+?)([A-Z].*)/", name)) {
            String word = perl.group(1);
            name = perl.group(2);
            words.add(StringUtils.capitalize(word));
        }

        words.add(StringUtils.capitalize(name));

        return StringUtils.join(words.iterator(), " ");
    }
}
