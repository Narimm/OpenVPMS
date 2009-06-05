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

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathTypeConversionException;
import org.apache.commons.jxpath.util.TypeConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyCollection;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;
import org.openvpms.component.system.common.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NodeDescriptor extends Descriptor {

    /**
     * Serialization version identifier.
     */
    private static final long serialVersionUID = 2L;

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
    public static final String IDENTIFIER_NODE_NAME = "id";

    /**
     * This is used to identify a max cardinality that is unbounded.
     */
    public static final int UNBOUNDED = -1;

    /**
     * Representation of max cardinality as a string
     */
    public static final String UNBOUNDED_AS_STRING = "*";

    /**
     * Type converter.
     */
    private static final TypeConverter CONVERTER = new OpenVPMSTypeConverter();

    /**
     * Contains a list of {@link AssertionDescriptor} instances
     */
    private Map<String, AssertionDescriptor> assertionDescriptors =
            new LinkedHashMap<String, AssertionDescriptor>();

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
    private transient Class clazz;

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
     * Indicates whether the node value represents an array
     */
    private boolean isArray = false;

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
     * The filter is only valid for collections and defines the subset of
     * the collection that this node refers too.  The filter is an archetype
     * shortName, which can also be in the form of a regular expression
     * <p/>
     * The modeFilter is a regex compliant filter
     */
    private String filter;
    private String modFilter;

    /**
     * The parent node descriptor. May be <code>null</code>.
     */
    private NodeDescriptor parent;

    /**
     * The archetype that this descriptor belongs to. May be <code>null</code>.
     */
    private ArchetypeDescriptor archetype;

    private static final ArchetypeId NODE = new ArchetypeId(
            "descriptor.node.1.0");

    private static final ArchetypeId COLLECTION_NODE = new ArchetypeId(
            "descriptor.collectionNode");

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(NodeDescriptor.class);


    /**
     * Default constructor.
     */
    public NodeDescriptor() {
    }

    /**
     * Returns the archetype Id. For nodes that have child nodes, returns
     * <em>descriptor.collectionNode.1.0</em>, otherwise returns
     * <em>descriptor.node.1.0</em>.
     *
     * @return the archetype Id.
     */
    @Override
    public ArchetypeId getArchetypeId() {
        return (nodeDescriptors == null || nodeDescriptors.isEmpty()) ? NODE : COLLECTION_NODE;
    }

    /**
     * Adds an assertion descriptor to this node.
     *
     * @param descriptor the assertion descriptor to add
     */
    public void addAssertionDescriptor(AssertionDescriptor descriptor) {
        assertionDescriptors.put(descriptor.getName(), descriptor);
    }

    /**
     * Add a child object to this node descriptor using the specified
     * {@link IMObject} as the context. If this node descriptor is not of type
     * collection, or the context object is null it will raise an exception.
     *
     * @param context the context object, which will be the target of the add
     * @param child   the child element to add
     * @throws DescriptorException if it fails to complete this request
     */
    public void addChildToCollection(IMObject context, Object child) {
        if (context == null) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToAddChildElement,
                    getName());
        }

        if (!isCollection()) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToAddChildElement,
                    getName());
        }

        // retrieve the value at that node
        Object obj = JXPathHelper.newContext(context).getValue(getPath());

        try {
            if (StringUtils.isEmpty(baseName)) {
                // no base name specified look at the type to determine
                // what method to call
                Class tClass = getClazz();
                if (Collection.class.isAssignableFrom(tClass)) {
                    MethodUtils.invokeMethod(obj, "add", child);
                } else if (Map.class.isAssignableFrom(tClass)) {
                    MethodUtils.invokeMethod(obj, "put",
                                             new Object[]{child, child});
                } else {
                    throw new DescriptorException(
                            DescriptorException.ErrorCode.FailedToAddChildElement,
                            getName());
                }
            } else {
                // if a baseName has been specified then prepend 'add' to the
                // base name and excute the derived method on context object
                String methodName = "add" + StringUtils.capitalize(baseName);

                // TODO This is a tempoaray fix until we resolve the discrepency
                // with collections.
                if (obj instanceof IMObject) {
                    MethodUtils.invokeMethod(obj, methodName, child);
                } else {
                    MethodUtils.invokeMethod(context, methodName, child);
                }
            }
        } catch (Exception exception) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToAddChildElement,
                    exception, getName());
        }
    }

    /**
     * Add a child node descriptor
     *
     * @param child the child node descriptor to add
     * @throws DescriptorException if the node is a duplicate
     */
    public void addNodeDescriptor(NodeDescriptor child) {
        if (nodeDescriptors.containsKey(child.getName())) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.DuplicateNodeDescriptor,
                    child.getName(), getName());
        }
        nodeDescriptors.put(child.getName(), child);
        child.setParent(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.Descriptor#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        NodeDescriptor copy = (NodeDescriptor) super.clone();
        copy.assertionDescriptors = new LinkedHashMap<String, AssertionDescriptor>(
                this.assertionDescriptors);
        copy.nodeDescriptors = new LinkedHashMap<String, NodeDescriptor>(
                this.nodeDescriptors);
        return copy;
    }

    /**
     * Check whether this assertion type is defined for this node
     *
     * @param type the assertion type
     * @return boolean
     */
    public boolean containsAssertionType(String type) {
        return assertionDescriptors.containsKey(type);
    }

    /**
     * Derive the node value for the specified {@link NodeDescriptor}. If the
     * node does not support derived value or the value cannot be derived then
     * raise an exception.
     *
     * @param imobj the {@link IMObject}
     * @throws FailedToDeriveValueException if the node is not declared to support dervied value or
     *                                      the value cannot be derived correctly.
     */
    public void deriveValue(IMObject imobj) {
        if (!isDerived()) {
            throw new FailedToDeriveValueException(
                    FailedToDeriveValueException.ErrorCode.DerivedValueUnsupported,
                    new Object[]{getName()});
        }

        // attempt to derive the value
        try {
            JXPathContext context = JXPathHelper.newContext(imobj);
            Object value = context.getValue(this.getDerivedValue());
            context.getPointer(this.getPath()).setValue(value);
        } catch (Exception exception) {
            throw new FailedToDeriveValueException(
                    FailedToDeriveValueException.ErrorCode.FailedToDeriveValue,
                    new Object[]{getName(), getPath(), getDerivedValue()},
                    exception);
        }
    }

    /**
     * Return the archetype names associated with a particular object reference
     * or collection.
     *
     * @return String pattern
     * @deprecated no replacement
     */
    @Deprecated
    public String[] getArchetypeNames() {
        ArrayList<String> result = new ArrayList<String>();

        AssertionDescriptor desc = assertionDescriptors.get("validArchetypes");
        if (desc != null) {
            PropertyList archetypes = (PropertyList) desc.getPropertyMap()
                    .getProperties().get("archetypes");
            for (NamedProperty archetype : archetypes.getProperties()) {
                AssertionProperty shortName = (AssertionProperty) ((PropertyMap) archetype)
                        .getProperties().get("shortName");
                result.add(shortName.getValue());
            }
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Return an array of short names or short name regular expression that are
     * associated with the archetypeRange assertion. If the node does not have
     * such an assertion then return a zero length string array
     * <p/>
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

            return range.toArray(new String[range.size()]);
        } else {
            return new String[0];
        }
    }

    /**
     * Retrieve the assertion descriptor with the specified type or null if one
     * does not exist.
     *
     * @param type the type of the assertion descriptor
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
     * Return the assertion descriptors in index order.
     *
     * @return the assertion descriptors, ordered on index
     */
    public List<AssertionDescriptor> getAssertionDescriptorsInIndexOrder() {
        List<AssertionDescriptor> adescs =
                new ArrayList<AssertionDescriptor>(
                        assertionDescriptors.values());
        Collections.sort(adescs, new AssertionDescriptorIndexComparator());

        return adescs;
    }

    /**
     * Return the assertion descriptors as an array.
     *
     * @return the assertion descriptors
     */
    public AssertionDescriptor[] getAssertionDescriptorsAsArray() {
        return assertionDescriptors.values().toArray(
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
     * @param context the context object
     * @return List a list of candiate children, which can also be an empty list
     * @deprecated unused, no replacement. Will be removed post 1.0
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public List<IMObject> getCandidateChildren(IMObject context) {
        List<IMObject> result = null;
        AssertionDescriptor descriptor = assertionDescriptors
                .get("candidateChildren");

        if ((descriptor == null) ||
                (!descriptor.getPropertyMap().getProperties().containsKey(
                        "path"))) {
            return result;
        }

        String path = (String) descriptor.getPropertyMap().getProperties().get(
                "path").getValue();
        try {
            Object obj = JXPathHelper.newContext(context).getValue(path);
            if (obj == null) {
                result = new ArrayList<IMObject>();
            } else if (obj instanceof Collection) {
                result = new ArrayList<IMObject>((Collection) obj);
            } else {
                log.warn("getCandidateChildren for path " + path
                        + " returned object of type "
                        + obj.getClass().getName());
            }
        } catch (Exception exception) {
            log.warn("Failed in getCandidateChildren for path " + path,
                     exception);
        }

        return result;
    }

    /**
     * Return the children {@link IMObject} instances that are part of
     * a collection. If the NodeDescriptor does not denote a collection then
     * a null list is returned.
     * <p/>
     * Furthermore if this is a collection and the filter attribute has been
     * specified then return a subset of the children; those matching the
     * filter.
     *
     * @param target the target object.
     * @return List<IMObject>
     *         the list of children, an empty list or  null
     */
    @SuppressWarnings("unchecked")
    public List<IMObject> getChildren(IMObject target) {
        List<IMObject> children = null;
        if (isCollection()) {
            try {
                Object obj = JXPathHelper.newContext(target).getValue(
                        getPath());
                if (obj == null) {
                    children = new ArrayList<IMObject>();
                } else if (obj instanceof Collection) {
                    children = new ArrayList<IMObject>((Collection) obj);
                } else if (obj instanceof PropertyCollection) {
                    children = new ArrayList<IMObject>(
                            ((PropertyCollection) obj).values());
                } else if (obj instanceof Map) {
                    children = new ArrayList<IMObject>(((Map) obj).values());
                }

                // filter the children
                children = filterChildren(children);
            } catch (Exception exception) {
                throw new DescriptorException(
                        DescriptorException.ErrorCode.FailedToGetChildren,
                        exception, target.getName(), getName(), getPath());
            }

            return children;

        }

        return children;
    }

    /**
     * Filter the children in the list and return only those that comply with
     * the filter term
     *
     * @param children the initial list of children
     * @return List<IMObject>
     *         the filtered set
     */
    private List<IMObject> filterChildren(List<IMObject> children) {
        // if no filter was specified return the complete list
        if (StringUtils.isEmpty(filter)) {
            return children;
        }

        // otherwise filter on
        List<IMObject> filteredSet = new ArrayList<IMObject>();
        for (IMObject obj : children) {
            if (obj.getArchetypeId().getShortName().matches(modFilter)) {
                filteredSet.add(obj);
            }
        }

        return filteredSet;
    }

    /**
     * Returns the class for the specified type.
     *
     * @return the class, or <tt>null</tt> if {@link #getType()} returns
     *         empty/null
     * @throws DescriptorException if the class can't be loaded
     */
    public Class getClazz() {
        if (clazz == null) {
            synchronized (this) {
                clazz = getClass(type);
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
        return StringUtils.isEmpty(displayName) ? StringUtilities.unCamelCase(getName())
                : displayName;
    }

    /**
     * @return Returns the filter.
     */
    public String getFilter() {
        return filter;
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
     * The getter that returns the max cardinality as a string
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
     * @throws DescriptorException a runtim exception
     */
    public Number getMaxValue() {
        Number number = null;
        if (isNumeric()) {
            AssertionDescriptor descriptor
                    = assertionDescriptors.get("numericRange");
            if (descriptor != null) {
                number = NumberUtils.createNumber((String) descriptor
                        .getPropertyMap().getProperties().get("maxValue")
                        .getValue());
            }
        } else {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.UnsupportedOperation,
                    "getMaxValue", getType());
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
     * @throws DescriptorException a runtim exception
     */
    public Number getMinValue() {
        Number number = null;
        if (isNumeric()) {
            AssertionDescriptor descriptor = assertionDescriptors
                    .get("numericRange");
            if (descriptor != null) {
                number = NumberUtils.createNumber((String) descriptor
                        .getPropertyMap().getProperties().get("minValue")
                        .getValue());
            }
        } else {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.UnsupportedOperation,
                    "getMinValue", getType());
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
        return nodeDescriptors.values().toArray(
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
            AssertionDescriptor descriptor = assertionDescriptors
                    .get("regularExpression");
            if (descriptor != null) {
                expression = (String) descriptor.getPropertyMap()
                        .getProperties().get("expression").getValue();
            }
        } else {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.UnsupportedOperation,
                    "getMinValue", getType());
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
     * @param context the context object to work from
     * @return Object the returned object
     */
    public Object getValue(IMObject context) {
        Object value;
        if (isDerived()) {
            value = JXPathHelper.newContext(context).getValue(
                    getDerivedValue());
        } else {
            if (isCollection()) {
                value = getChildren(context);
            } else {
                value = JXPathHelper.newContext(context).getValue(getPath());
            }
        }

        return transform(value);
    }

    /**
     * Returns the value of this node given the specified context.
     *
     * @param context the context to use
     * @return Object
     *         the returned object
     */
    public Object getValue(JXPathContext context) {
        Object value = null;
        if (context != null) {
            if (isDerived()) {
                value = context.getValue(getDerivedValue());
            } else {
                if (isCollection()) {
                    value = getChildren((IMObject) context.getContextBean());
                } else {
                    value = context.getValue(getPath());
                }
            }
        }

        return value;
    }

    /**
     * Check whether this node is a boolean type.
     *
     * @return boolean
     */
    public boolean isBoolean() {
        Class aclass = getClazz();
        return (Boolean.class == aclass) || (boolean.class == aclass);

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
     * Cast the input value as a collection. If this can't be done then
     * throw and exception
     *
     * @param object the object to cast
     * @return Collection
     *         the returned collection object
     * @throws DescriptorException if the cast cannot be made
     */
    public Collection toCollection(Object object) {
        Collection collection = null;

        if (object == null) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.CannotCastToCollection);
        } else if (object instanceof Collection) {
            collection = (Collection) object;
        } else if (object instanceof PropertyCollection) {
            collection = ((PropertyCollection) object).values();
        } else if (object instanceof Map) {
            collection = ((Map) object).values();
        }

        return collection;
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
        return Date.class == aclass;

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
     * Check whether this ia a money type
     *
     * @return boolean
     */
    public boolean isMoney() {
        return getClazz() == Money.class;
    }

    /**
     * Check whether this node is a numeric type.
     *
     * @return boolean
     */
    public boolean isNumeric() {

        Class aclass = getClazz();
        return (Number.class.isAssignableFrom(aclass)) || (byte.class == aclass)
                || (short.class == aclass) || (int.class == aclass)
                || (long.class == aclass) || (float.class == aclass)
                || (double.class == aclass);
    }

    /**
     * Check whether this node is an object reference. An object reference is a
     * node that references another oject subclassed from IMObject.
     *
     * @return boolean
     */
    public boolean isObjectReference() {
        return IMObjectReference.class.isAssignableFrom(getClazz());
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
     * This method indicates that this node descriptor is read-only.
     *
     * @return <tt>true</tt> if the node descriptor is read only
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
        return String.class == aclass;
    }

    /**
     * A helper method that checks to see if the specified {@link IMObject}
     * matches the specified filter. This is only  relevant for collection
     * nodes
     *
     * @param imobj the object to check
     * @return boolean
     *         true if it matches the filter
     */
    public boolean matchesFilter(IMObject imobj) {
        boolean matches = false;

        if (isCollection()) {
            if (StringUtils.isEmpty(filter)) {
                matches = true;
            } else {
                String shortName = imobj.getArchetypeId().getShortName();
                matches = shortName.matches(modFilter);
            }
        }

        return matches;
    }

    /**
     * Delete the specified assertion descriptor
     *
     * @param descriptor the assertion to delete
     */
    public void removeAssertionDescriptor(AssertionDescriptor descriptor) {
        assertionDescriptors.remove(descriptor.getName());
    }

    /**
     * Delete the assertion descriptor with the specified type
     *
     * @param type the type name
     */
    public void removeAssertionDescriptor(String type) {
        assertionDescriptors.remove(type);
    }

    /**                 a
     * Remove the specified child object from the collection defined by this
     * node descriptor using the nominated {@link IMObject} as the root context.
     * <p/>
     * If this node descriptor is not of type collection, or the context object
     * is null it will raise an exception.
     *
     * @param context the root context object
     * @param child   the child element to remove
     * @throws DescriptorException if it fails to complete this request
     */
    public void removeChildFromCollection(IMObject context, Object child) {
        if (context == null) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToRemoveChildElement,
                    getName());
        }

        if (!isCollection()) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToRemoveChildElement,
                    getName());
        }

        Object obj = JXPathHelper.newContext(context).getValue(getPath());

        try {
            if (StringUtils.isEmpty(baseName)) {
                // no base name specified look at the type to determine
                // what method to call
                Class tClass = getClazz();
                if (Collection.class.isAssignableFrom(tClass)) {
                    MethodUtils.invokeMethod(obj, "remove", child);
                } else if (Map.class.isAssignableFrom(tClass)) {
                    MethodUtils.invokeMethod(obj, "remove", child);
                } else {
                    throw new DescriptorException(
                            DescriptorException.ErrorCode.FailedToRemoveChildElement,
                            getName());
                }
            } else {
                // if a baseName has been specified then prepend 'add' to the
                // base name and excute the derived method on contxt object
                String methodName = "remove" + StringUtils.capitalize(baseName);

                if (obj instanceof IMObject) {
                    MethodUtils.invokeMethod(obj, methodName, child);
                } else {
                    MethodUtils.invokeMethod(context, methodName, child);
                }

            }
        } catch (Exception exception) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToRemoveChildElement,
                    exception, getName());
        }
    }

    /**
     * @param assertionDescriptors The assertionDescriptors to set.
     * @deprecated use {@link #addAssertionDescriptor} instead. Will be removed
     *             post 1.x.
     */
    @Deprecated
    public void setAssertionDescriptors(
            Map<String, AssertionDescriptor> assertionDescriptors) {
        this.assertionDescriptors = assertionDescriptors;
    }

    /**
     * @param assertionDescriptors The assertionDescriptors to set.
     * @deprecated use {@link #addAssertionDescriptor} instead. Will be removed
     *             post 1.x.
     */
    @Deprecated
    public void setAssertionDescriptorsAsArray(
            AssertionDescriptor[] assertionDescriptors) {
        this.assertionDescriptors = new LinkedHashMap<String, AssertionDescriptor>();
        int index = 0;
        for (AssertionDescriptor descriptor : assertionDescriptors) {
            descriptor.setIndex(index++);
            addAssertionDescriptor(descriptor);
        }
    }

    /**
     * @param baseName The baseName to set.
     */
    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /**
     * @param defaultValue The defaultValue to set.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @param isDerived The isDerived to set.
     */
    public void setDerived(boolean isDerived) {
        this.isDerived = isDerived;
    }

    /**
     * @param derivedValue The derivedValue to set.
     */
    public void setDerivedValue(String derivedValue) {
        this.derivedValue = derivedValue;
    }

    /**
     * @param displayName The displayName to set.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(String filter) {
        this.filter = filter;
        if (!StringUtils.isEmpty(filter)) {
            this.modFilter = filter.replace("*", ".*");
        }
    }

    /**
     * @param isHidden The isHidden to set.
     */
    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    /**
     * @param index The index to set.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @param maxCardinality The maxCardinality to set.
     */
    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    /**
     * This setter enabled the user to specify an unbounded maximum collection
     * using '*'.
     *
     * @param maxCardinality The maxCardinality to set.
     */
    public void setMaxCardinalityAsString(String maxCardinality) {
        if (maxCardinality.equals(UNBOUNDED_AS_STRING)) {
            setMaxCardinality(UNBOUNDED);
        } else {
            setMaxCardinality(Integer.parseInt(maxCardinality));
        }
    }

    /**
     * @param maxLength The maxLength to set.
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @param minCardinality The minCardinality to set.
     */
    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    /**
     * @param minLength The minLength to set.
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * @param nodeDescriptors The nodeDescriptors to set.
     * @deprecated use {@link #addNodeDescriptor} instead.
     */
    @Deprecated
    public void setNodeDescriptors(
            Map<String, NodeDescriptor> nodeDescriptors) {
        this.nodeDescriptors = nodeDescriptors;
    }

    /**
     * @param nodes The nodeDescriptors to set.
     * @deprecated use {@link #addNodeDescriptor} instead.
     */
    @Deprecated
    public void setNodeDescriptorsAsArray(NodeDescriptor[] nodes) {
        this.nodeDescriptors = new LinkedHashMap<String, NodeDescriptor>();
        int index = 0;
        for (NodeDescriptor node : nodes) {
            node.setIndex(index++);
            addNodeDescriptor(node);
        }
    }

    /**
     * @param parentChild The parentChild to set.
     */
    public void setParentChild(boolean parentChild) {
        this.isParentChild = parentChild;
    }

    /**
     * @param path The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Set the value of the readOnly attribute
     *
     * @param value if <tt>true</tt> marks the node descriptor read-only
     */
    public void setReadOnly(boolean value) {
        this.isReadOnly = value;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        if (StringUtils.isEmpty(type)) {
            this.type = null;
        } else {
            if (type.endsWith("[]")) {
                this.isArray = true;
                this.type = type.substring(0, type.indexOf("[]"));
            } else {
                this.type = type;
            }
        }
    }

    /**
     * Set the node value for the specified {@link IMObject}.
     *
     * @param context the context object, which will be the target of the set
     * @param value   the value to set
     * @throws DescriptorException if it cannot set the value
     */
    public void setValue(IMObject context, Object value) {
        // Removed readOnly check for temporary OBF-115 fix
//        if (isReadOnly()) {
//            throw new DescriptorException(
//                    DescriptorException.ErrorCode.CannotSetValueForReadOnlyNode,
//                    new Object[] { getName() });
//        }

        if (context == null) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.NullContextForSetValue,
                    getName());
        }

        try {
            for (AssertionDescriptor descriptor: getAssertionDescriptorsAsArray()) {
                value = descriptor.set(value, context, this);
            }
            if (isArray) {
                JXPathHelper.newContext(context).setValue(getPath(), value);
            } else {
                JXPathHelper.newContext(context).setValue(getPath(),
                                                          transform(value));
            }
        } catch (Exception exception) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToSetValue,
                    exception, getName());
        }
    }

    /**
     * This is a helper method that will attempt to convert a string to the
     * type specified by this node descriptor. If the node descriptor is of
     * type string then it will simply return the same string otherwise it
     * will search for a constructor of that type that takes a string and
     * return the transformed object.
     *
     * @param value the string value
     * @return Object
     *         the transformed object
     */
    private Object transform(Object value) {
        if ((value == null) ||
                (this.isCollection()) ||
                (value.getClass() == getClazz())) {
            return value;
        }

        try {
            return CONVERTER.convert(value, getClazz());
        } catch (JXPathTypeConversionException exception) {
            throw new DescriptorException(
                    DescriptorException.ErrorCode.FailedToCoerceValue,
                    value.getClass().getName(), getClazz().getName());
        }
    }

    /**
     * Returns the archetype descriptor that this is a node of.
     *
     * @return the archetype descriptor that this is a node of. May be
     *         <code>null</code>
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        return archetype;
    }

    /**
     * Returns the parent node descriptor.
     *
     * @return the parent node descriptor or <code>null</code>, if this node
     *         has no parent.
     */
    public NodeDescriptor getParent() {
        return parent;
    }

    /**
     * Sets the archetype descriptor.
     *
     * @param descriptor the archetype descriptor
     */
    public void setArchetypeDescriptor(ArchetypeDescriptor descriptor) {
        archetype = descriptor;
    }

    /**
     * Sets the parent node descriptor.
     *
     * @param parent the parent node descriptor, or <code>null</code> if this
     *               node has no parent
     */
    public void setParent(NodeDescriptor parent) {
        this.parent = parent;
    }

}

/**
 * This comparator is used to compare the indices of AssertionDescriptors
 */
class AssertionDescriptorIndexComparator
        implements Comparator<AssertionDescriptor> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(T, T)
     */
    public int compare(AssertionDescriptor no1, AssertionDescriptor no2) {
        if (no1 == no2) {
            return 0;
        }

        if (no1.getIndex() == no2.getIndex()) {
            return 0;
        } else if (no1.getIndex() > no2.getIndex()) {
            return 1;
        } else {
            return -1;
        }
    }
}
