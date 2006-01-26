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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// commons-lang
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.math.NumberUtils;

// log4j
import org.apache.log4j.Logger;
import org.apache.oro.text.perl.Perl5Util;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyCollection;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NodeDescriptor extends Descriptor {
    /**
     * The default display length if one is not defined in the node definition
     */
    public static final int DEFAULT_DISPLAY_LENGTH = 50;

    /**
     * The default maximum Length if one is not defined in the node definition
     */
    public static final int DEFAULT_MAX_LENGTH = 255;

    /**
     * The name given to the object id node
     */
    public static final String IDENTIFIER_NODE_NAME = "uid";

    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(NodeDescriptor.class);

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * This is used to identify a max cardinality that is unbounded
     */
    public static final int UNBOUNDED = -1;

    public static final String UNBOUNDED_AS_STRING = "*";

    /**
     * Contains a list of {@link AssertionDescriptor} instances
     */
    private Map<String, AssertionDescriptor> assertionDescriptors = new LinkedHashMap<String, AssertionDescriptor>();

    /**
     * This is an option property, which is required for nodes that represent
     * collections. It is the name that denotes the individual elements stored
     * in the collection.
     */
    private String baseName;

    /**
     * Cache the clazz. Do not access this directly. Use the {@link #getClazz()}
     * method instead.
     */
    private Class clazz;

    /**
     * The default value
     */
    private String defaultValue;

    /**
     * This is a jxpath expression, which is used to determine the value of the
     * node
     */
    private String derivedValue;

    /**
     * This is the display name, which is only supplied if it is different to
     * the node name
     */
    private String displayName;

    /**
     * The index of this discriptor within the collection
     */
    private int index;

    /**
     * Determine whether the value for this node is derived
     */
    private boolean isDerived = false;

    /**
     * Attribute, which defines whether this node is hidden or can be displayed
     */
    private boolean isHidden = false;

    /**
     * Indicates that the collection type is a parentChild relationship, which
     * is the default for a collection. If this attribute is set to false then
     * the child lifecycle is independent of the parent lifecycle. This
     * attribute is only meaningful for a collection
     */
    private boolean isParentChild = true;

    /**
     * Indicates whether the descriptor is readOnly
     */
    private boolean isReadOnly = false;

    /**
     * The maximum cardinality, which defaults to 1
     */
    private int maxCardinality = 1;

    /**
     * The maximum length
     */
    private int maxLength;

    /**
     * The minimum cardinality, which defaults to 0
     */
    private int minCardinality = 0;

    /**
     * The minimum length
     */
    private int minLength;

    /**
     * A node can have other nodeDescriptors to define a nested structure
     */
    private Map<String, NodeDescriptor> nodeDescriptors = new LinkedHashMap<String, NodeDescriptor>();

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
     * Default constructor
     */
    public NodeDescriptor() {
        setArchetypeId(new ArchetypeId("openvpms-system-descriptor.node.1.0"));
    }

    /**
     * Add an assertion descriptor to this node
     * 
     * @param descriptor
     */
    public void addAssertionDescriptor(AssertionDescriptor descriptor) {
        assertionDescriptors.put(descriptor.getName(), descriptor);
    }

    /**
     * Add a child object to this node descriptor using the specified
     * {@link IMObject} as the context. If this node descriptor is not of type
     * collection, or the context object is null it will raise an exception.
     * 
     * @param context
     *            the context object, which will be the target of the add
     * @param child
     *            the child element to add
     * @thorws DescriptorException if it fails to complete this request
     */
    public void addChildToCollection(IMObject context, Object child) {
        if (context == null) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToAddChildElement,
                    new Object[] { getName() });
        }

        if (!isCollection()) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToAddChildElement,
                    new Object[] { getName() });
        }

        try {
            if (StringUtils.isEmpty(baseName)) {
                // no base name specified look at the type to determine
                // what method to call
                Class tClass = getClazz();
                if (Collection.class.isAssignableFrom(tClass)) {
                    MethodUtils.invokeMethod(getValue(context), "add", child);
                } else if (Map.class.isAssignableFrom(tClass)) {
                    MethodUtils.invokeMethod(getValue(context), "put",
                            new Object[] { child, child });
                } else {
                    throw new DescriptorException(
                            DescriptorException.ErrorCode.FailedToAddChildElement,
                            new Object[] { getName() });
                }
            } else {
                // if a baseName has been specified then prepend 'add' to the
                // base name and excute the derived method on context object
                String methodName = "add" + StringUtils.capitalize(baseName);

                // TODO This is a tempoaray fix until we resolve the discrepency
                // with collections.
                if (getValue(context) instanceof IMObject) {
                    MethodUtils.invokeMethod(getValue(context), methodName,
                            child);
                } else {
                    MethodUtils.invokeMethod(context, methodName, child);
                }

            }
        } catch (Exception exception) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToAddChildElement,
                    new Object[] { getName() }, exception);
        }
    }

    /**
     * Add a child node descriptor
     * 
     * @param child
     *            the child node descriptor to add
     */
    public void addNodeDescriptor(NodeDescriptor child) {
        nodeDescriptors.put(child.getName(), child);
    }

    /**
     * Check and adjust the archetype id. If the node descriptor has other
     * children then set the archetypeId to collection node, otherwise set it to
     * normal node.
     * <p>
     * TODO This will disappear when we introduce a new collection node class.
     */
    private void checkArchetypeId() {
        if ((nodeDescriptors != null) && (nodeDescriptors.size() > 0)) {
            if (getArchetypeId().getConcept().equals("node")) {
                setArchetypeId(new ArchetypeId(
                        "openvpms-system-descriptor.collectionNode.1.0"));
            }
        } else {
            if (getArchetypeId().getConcept().equals("collectionNode")) {
                setArchetypeId(new ArchetypeId(
                        "openvpms-system-descriptor.node.1.0"));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.Descriptor#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        NodeDescriptor copy = (NodeDescriptor) super.clone();
        copy.assertionDescriptors = new HashMap<String, AssertionDescriptor>(
                this.assertionDescriptors);
        copy.baseName = this.baseName;
        copy.clazz = this.clazz;
        copy.defaultValue = this.defaultValue;
        copy.derivedValue = this.derivedValue;
        copy.index = this.index;
        copy.isDerived = this.isDerived;
        copy.isHidden = this.isHidden;
        copy.isParentChild = this.isParentChild;
        copy.maxCardinality = this.maxCardinality;
        copy.maxLength = this.maxLength;
        copy.minCardinality = this.minCardinality;
        copy.minLength = this.minLength;
        copy.nodeDescriptors = new LinkedHashMap<String, NodeDescriptor>(
                this.nodeDescriptors);
        copy.path = this.path;
        copy.type = this.type;

        return copy;
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
     * Return the archetype names associated with a particular object reference
     * or collection.
     * 
     * @return String pattern
     */
    @SuppressWarnings("unchecked")
    public String[] getArchetypeNames() {
        ArrayList<String> result = new ArrayList<String>();

        AssertionDescriptor desc = (AssertionDescriptor) assertionDescriptors
                .get("validArchetypes");
        if (desc != null) {
            PropertyList archetypes = (PropertyList) desc.getPropertyMap()
                    .getProperties().get("archetypes");
            for (NamedProperty archetype : archetypes.getProperties()) {
                AssertionProperty shortName = (AssertionProperty) ((PropertyMap) archetype)
                        .getProperties().get("shortName");
                result.add(shortName.getValue());
            }
        }

        return (String[]) result.toArray(new String[result.size()]);
    }

    /**
     * Return an array of short names or short name regular expression that are
     * associated with the archetypeRange assertion. If the node does not have
     * such an assertion then return a zero length string array
     * 
     * TODO Should we more this into a utility class TODO Change return type to
     * List
     * 
     * @return String[] the array of short names
     */
    public String[] getArchetypeRange() {
        if (assertionDescriptors.containsKey("archetypeRange")) {
            ArrayList<String> range = new ArrayList<String>();
            AssertionDescriptor desc = assertionDescriptors
                    .get("archetypeRange");
            PropertyList archetypes = (PropertyList) desc.getPropertyMap()
                    .getProperties().get("archetypes");
            for (NamedProperty archetype : archetypes.getProperties()) {
                AssertionProperty shortName = (AssertionProperty) ((PropertyMap) archetype)
                        .getProperties().get("shortName");
                range.add(shortName.getValue());
            }

            return (String[]) range.toArray(new String[range.size()]);
        } else {
            return new String[0];
        }
    }

    /**
     * Retrieve the assertion descriptor with the specified type or null if one
     * does not exist.
     * 
     * @param type
     *            the type of the assertion descriptor
     * @return AssertionDescriptor
     */
    public AssertionDescriptor getAssertionDescriptor(String type) {
        return assertionDescriptors.get(type);
    }

    /**
     * Return the assertion descriptors as a map
     * 
     * @return Returns the assertionDescriptors.
     */
    public Map<String, AssertionDescriptor> getAssertionDescriptors() {
        return assertionDescriptors;
    }

    /**
     * Return the assertion descriptors as a map
     */
    public AssertionDescriptor[] getAssertionDescriptorsAsArray() {
        return (AssertionDescriptor[]) assertionDescriptors.values().toArray(
                new AssertionDescriptor[assertionDescriptors.size()]);
    }

    /**
     * @return Returns the baseName.
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * Return a list of candiate children for the specified node. This is only
     * applicable for collection nodes that have a candidateChildren assertion
     * defined.
     * 
     * @param context
     *            the context object
     * @return List a list of candiate children, which can also be an empty list
     */
    @SuppressWarnings("unchecked")
    public List<IMObject> getCandidateChildren(IMObject context) {
        List<IMObject> result = null;
        AssertionDescriptor descriptor = assertionDescriptors
                .get("candidateChildren");

        if ((descriptor == null)
                || (descriptor.getPropertyMap().getProperties().containsKey(
                        "path") == false)) {
            return result;
        }

        String path = (String) descriptor.getPropertyMap().getProperties().get(
                "path").getValue();
        try {
            Object obj = JXPathContext.newContext(context).getValue(path);
            if (obj == null) {
                result = new ArrayList<IMObject>();
            } else if (obj instanceof Collection) {
                result = new ArrayList<IMObject>((Collection) obj);
            } else {
                logger.warn("getCandidateChildren for path " + path
                        + " returned object of type "
                        + obj.getClass().getName());
            }
        } catch (Exception exception) {
            logger.warn("Failed in getCandidateChildren for path " + path,
                    exception);
        }

        return result;
    }

    /**
     * Return the class for the specified type
     * 
     * @param Class
     *            The class
     */
    private Class getClazz() {
        if (clazz == null) {
            if (StringUtils.isEmpty(type)) {
                clazz = null;
            } else {
                try {
                    clazz = Thread.currentThread().getContextClassLoader()
                            .loadClass(type);
                } catch (Exception exception) {
                    throw new DescriptorException(
                            DescriptorException.ErrorCode.InvalidType,
                            new Object[] { type }, exception);
                }
            }
        }

        return clazz;
    }

    /**
     * @return Returns the defaultValue.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @return Returns the derivedValue.
     */
    public String getDerivedValue() {
        return derivedValue;
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
     * @return Returns the displayName.
     */
    public String getDisplayName() {
        return StringUtils.isEmpty(displayName) ? unCamelCase(getName())
                : displayName;
    }

    /**
     * @return Returns the index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return Returns the maxCardinality.
     */
    public int getMaxCardinality() {
        return maxCardinality;
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
     * @return Returns the maxLength.
     */
    public int getMaxLength() {
        return maxLength <= 0 ? DEFAULT_MAX_LENGTH : maxLength;
    }

    /**
     * Return the maximum value of the node. If no maximum defined for node then
     * return 0. Only valid for numeric nodes.
     * 
     * @return Number the minimum value
     * @throws DescriptorException
     *             a runtim exception
     */
    public Number getMaxValue() {
        Number number = null;
        if (isNumeric()) {
            AssertionDescriptor descriptor = (AssertionDescriptor) assertionDescriptors
                    .get("numericRange");
            if (descriptor != null) {
                number = NumberUtils.createNumber((String) descriptor
                        .getPropertyMap().getProperties().get("maxValue")
                        .getValue());
            }
        } else {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.UnsupportedOperation,
                    new Object[] { "getMaxValue", getType() });
        }

        return number;
    }

    /**
     * @return Returns the minCardinality.
     */
    public int getMinCardinality() {
        return minCardinality;
    }

    /**
     * @return Returns the minLength.
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Return the minimum value of the node. If no minimum defined for node then
     * return 0. Only valid for numeric nodes.
     * 
     * @return Number the minimum value
     * @throws DescriptorException
     *             a runtim exception
     */
    public Number getMinValue() {
        Number number = null;
        if (isNumeric()) {
            AssertionDescriptor descriptor = (AssertionDescriptor) assertionDescriptors
                    .get("numericRange");
            if (descriptor != null) {
                number = NumberUtils.createNumber((String) descriptor
                        .getPropertyMap().getProperties().get("minValue")
                        .getValue());
            }
        } else {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.UnsupportedOperation,
                    new Object[] { "getMinValue", getType() });
        }

        return number;
    }

    /**
     * Return the number of children node descriptors
     * 
     * @return int
     */
    public int getNodeDescriptorCount() {
        return (nodeDescriptors == null) ? 0 : nodeDescriptors.size();
    }

    /**
     * Return the {@link NodeDescriptor} instances as a map of name and
     * descriptor
     * 
     * @return Returns the nodeDescriptors.
     */
    public Map<String, NodeDescriptor> getNodeDescriptors() {
        return nodeDescriptors;
    }

    /**
     * @return Returns the nodeDescriptors.
     */
    public NodeDescriptor[] getNodeDescriptorsAsArray() {
        return (NodeDescriptor[]) nodeDescriptors.values().toArray(
                new NodeDescriptor[nodeDescriptors.size()]);
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
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
            AssertionDescriptor descriptor = (AssertionDescriptor) assertionDescriptors
                    .get("regularExpression");
            if (descriptor != null) {
                expression = (String) descriptor.getPropertyMap()
                        .getProperties().get("expression").getValue();
            }
        } else {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.UnsupportedOperation,
                    new Object[] { "getMinValue", getType() });
        }

        return expression;
    }

    /**
     * @return Returns the typeName.
     */
    public String getType() {
        return type;
    }

    /**
     * This will return the node value for the supplied {@link IMObject}. If
     * the node is derived then it will return the derived value. If the node is
     * not dervied then it will use the path to return the value.
     * 
     * @param context
     *            the context object to work from
     * @return Object the returned object
     */
    public Object getValue(IMObject context) {
        if (isDerived()) {
            return JXPathContext.newContext(context)
                    .getValue(getDerivedValue());
        } else {
            return JXPathContext.newContext(context).getValue(getPath());
        }
    }

    /**
     * Check whether this node is a boolean type.
     * 
     * @return boolean
     */
    public boolean isBoolean() {
        Class aclass = getClazz();
        if ((Boolean.class == aclass) || (boolean.class == aclass)) {
            return true;
        }

        return false;
    }

    /**
     * Check whether this node is a collection
     * 
     * @return boolean
     */
    public boolean isCollection() {
        try {
            Class aclass = Thread.currentThread().getContextClassLoader()
                    .loadClass(getType());

            return Collection.class.isAssignableFrom(aclass)
                    || Map.class.isAssignableFrom(aclass)
                    || PropertyCollection.class.isAssignableFrom(aclass);
        } catch (Exception ignore) {
            return false;
        }
    }

    /**
     * Indicates if this node is acomplex node. If the node has an
     * archetypeRange assertion or the node has a cardinality > 1 then the node
     * is deemed to be a complex node
     * 
     * @return boolean true if complex
     */
    public boolean isComplexNode() {
        return (getMaxCardinality() == NodeDescriptor.UNBOUNDED)
                || (getMaxCardinality() > 1)
                || (containsAssertionType("archetypeRange"));
    }

    /**
     * Check whether this node a date type.
     * 
     * @return boolean
     */
    public boolean isDate() {
        Class aclass = getClazz();
        if ((Date.class == aclass) || (DvDateTime.class == aclass)) {
            return true;
        }

        return false;
    }

    /**
     * @return Returns the isDerived.
     */
    public boolean isDerived() {
        return isDerived;
    }

    /**
     * @return Returns the isHidden.
     */
    public boolean isHidden() {
        return isHidden;
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
     * Check whether this node is a numeric type.
     * 
     * @return boolean
     */
    public boolean isNumeric() {

        Class aclass = getClazz();
        if ((Number.class.isAssignableFrom(aclass)) || (byte.class == aclass)
                || (short.class == aclass) || (int.class == aclass)
                || (long.class == aclass) || (float.class == aclass)
                || (double.class == aclass)) {
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
        if (IMObject.class.isAssignableFrom(getClazz())) {
            return true;
        }

        return false;
    }

    /**
     * This is a convenience method that checks whether there is a parent child
     * relationship within this node. A parent child relationship only
     * applicable for node descriptors that reference a collection.
     * 
     * @return boolean
     */
    public boolean isParentChild() {
        return isCollection() && isParentChild;
    }

    /**
     * This method indicates that this node descriptor is readOnly and a call to
     * {@link #setValue(IMObject, Object) will throw an exception.
     * 
     * @return boolean
     */
    public boolean isReadOnly() {
        return isReadOnly;
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
     * Check whether this node is a string
     * 
     * @return boolean
     */
    public boolean isString() {
        Class aclass = getClazz();
        if (String.class == aclass) {
            return true;
        }

        return false;
    }

    /**
     * Delete the specified assertion descriptor
     * 
     * @param assertion
     *            the assertion to delete
     */
    public void removeAssertionDescriptor(AssertionDescriptor descriptor) {
        assertionDescriptors.remove(descriptor.getName());
    }

    /**
     * Delete the assertion descriptor with the specified type
     * 
     * @param type
     *            the type name
     */
    public void removeAssertionDescriptor(String type) {
        assertionDescriptors.remove(type);
    }

    /**
     * Remove the specified child object from the collection defined by this
     * node descriptor using the nominated {@link IMObject} as the root context.
     * <p>
     * If this node descriptor is not of type collection, or the context object
     * is null it will raise an exception.
     * 
     * @param context
     *            the root context object
     * @param child
     *            the child element to remove
     * @thorws DescriptorException if it fails to complete this request
     */
    public void removeChildFromCollection(IMObject context, Object child) {
        if (context == null) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToRemoveChildElement,
                    new Object[] { getName() });
        }

        if (!isCollection()) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToRemoveChildElement,
                    new Object[] { getName() });
        }

        try {
            if (StringUtils.isEmpty(baseName)) {
                // no base name specified look at the type to determine
                // what method to call
                Class tClass = getClazz();
                if (Collection.class.isAssignableFrom(tClass)) {
                    MethodUtils
                            .invokeMethod(getValue(context), "remove", child);
                } else if (Map.class.isAssignableFrom(tClass)) {
                    MethodUtils
                            .invokeMethod(getValue(context), "remove", child);
                } else {
                    throw new DescriptorException(
                            DescriptorException.ErrorCode.FailedToRemoveChildElement,
                            new Object[] { getName() });
                }
            } else {
                // if a baseName has been specified then prepend 'add' to the
                // base name and excute the derived method on contxt object
                String methodName = "remove" + StringUtils.capitalize(baseName);

                // TODO This is a tempoaray fix until we resolve the discrepency
                // with collections.
                if (getValue(context) instanceof IMObject) {
                    MethodUtils.invokeMethod(getValue(context), methodName,
                            child);
                } else {
                    MethodUtils.invokeMethod(context, methodName, child);
                }

            }
        } catch (Exception exception) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToRemoveChildElement,
                    new Object[] { getName() }, exception);
        }
    }

    /**
     * @param assertionDescriptors
     *            The assertionDescriptors to set.
     */
    public void setAssertionDescriptors(
            Map<String, AssertionDescriptor> assertionDescriptors) {
        this.assertionDescriptors = assertionDescriptors;
    }

    /**
     * @param assertionDescriptors
     *            The assertionDescriptors to set.
     */
    public void setAssertionDescriptorsAsArray(
            AssertionDescriptor[] assertionDescriptors) {
        this.assertionDescriptors = new LinkedHashMap<String, AssertionDescriptor>();
        for (AssertionDescriptor descriptor : assertionDescriptors) {
            this.assertionDescriptors.put(descriptor.getName(), descriptor);
        }
    }

    /**
     * @param baseName
     *            The baseName to set.
     */
    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /**
     * @param defaultValue
     *            The defaultValue to set.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @param isDerived
     *            The isDerived to set.
     */
    public void setDerived(boolean isDerived) {
        this.isDerived = isDerived;
    }

    /**
     * @param derivedValue
     *            The derivedValue to set.
     */
    public void setDerivedValue(String derivedValue) {
        this.derivedValue = derivedValue;
    }

    /**
     * @param displayName
     *            The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @param isHidden
     *            The isHidden to set.
     */
    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    /**
     * @param index
     *            The index to set.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @param maxCardinality
     *            The maxCardinality to set.
     */
    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    /**
     * This setter enabled the user to specify an unbounded maximum collection
     * using '*'.
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
     * @param maxLength
     *            The maxLength to set.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @param minCardinality
     *            The minCardinality to set.
     */
    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    /**
     * @param minLength
     *            The minLength to set.
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * @param nodeDescriptors
     *            The nodeDescriptors to set.
     */
    public void setNodeDescriptors(Map<String, NodeDescriptor> nodeDescriptors) {
        this.nodeDescriptors = nodeDescriptors;
        checkArchetypeId();
    }

    /**
     * @param nodeDescriptors
     *            The nodeDescriptors to set.
     */
    public void setNodeDescriptorsAsArray(NodeDescriptor[] nodes) {
        this.nodeDescriptors = new LinkedHashMap<String, NodeDescriptor>();
        int index = 0;
        for (NodeDescriptor node : nodes) {
            node.setIndex(index++);
            addNodeDescriptor(node);
        }
    }

    /**
     * @param parentChild
     *            The parentChild to set.
     */
    public void setParentChild(boolean parentChild) {
        this.isParentChild = parentChild;
    }

    /**
     * @param path
     *            The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Set the value of the readOnly attribute
     * 
     * @param value
     */
    public void setReadOnly(boolean value) {
        this.isReadOnly = value;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Set the node value for the specified {@link IMObject}.
     * 
     * @param context
     *            the context object, which will be the target of the set
     * @param value
     *            the value to set
     * @throws DescriptorException
     *             if it cannot set the value
     */
    public void setValue(IMObject context, Object value) {
        if (isReadOnly()) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.CannotSetValueForReadOnlyNode,
                    new Object[] { getName() });
        }
        
        if (context == null) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.NullContextForSetValue,
                    new Object[] { getName() });
        }

        try {
            JXPathContext.newContext(context).setValue(getPath(), value);
        } catch (Exception exception) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToSetValue,
                    new Object[] { getName() }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", getName()).append(
                "displayName", displayName).append("isHidden", isHidden)
                .append("isDerived", isDerived).append("derivedValue",
                        derivedValue).append("path", path).append("type", type)
                .append("defaultValue", defaultValue).append("minCardinality",
                        minCardinality)
                .append("maxCardinality", maxCardinality).append("minLength",
                        minLength).append("maxLength", maxLength).append(
                        "baseName", baseName).append("isParentChild",
                        isParentChild).append("clazz", clazz).append("index",
                        index).append("assertionDescriptors",
                        assertionDescriptors).append("nodeDescriptors",
                        nodeDescriptors).toString();
    }

    /**
     * This method will uncamel case the speified string and return it to the
     * caller
     * 
     * @param name
     *            the camel cased strig
     * @return String
     */
    private String unCamelCase(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }

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
