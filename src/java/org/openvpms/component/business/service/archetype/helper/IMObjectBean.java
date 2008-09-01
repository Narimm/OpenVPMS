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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.InvalidClassCast;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.NodeDescriptorNotFound;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.AbstractPropertySet;
import org.openvpms.component.system.common.util.PropertySet;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Helper to access an {@link IMObject}'s properties via their names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectBean {

    /**
     * The object.
     */
    private final IMObject object;

    /**
     * The archetype.
     */
    private ArchetypeDescriptor archetype;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * Used to convert node values to a particular type.
     */
    private PropertySet properties;


    /**
     * Constructs a new <tt>IMObjectBean</tt>.
     *
     * @param object the object
     */
    public IMObjectBean(IMObject object) {
        this(object, null);
    }

    /**
     * Constructs a new <tt>IMObjectBean</tt>.
     *
     * @param object  the object
     * @param service the archetype service. May be <tt>null</tt>
     */
    public IMObjectBean(IMObject object, IArchetypeService service) {
        this.object = object;
        this.service = service;
        this.properties = new NodePropertySet();
    }

    /**
     * Returns the underlying object.
     *
     * @return the object
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns a reference to the underlying object.
     *
     * @return the reference
     */
    public IMObjectReference getReference() {
        return object.getObjectReference();
    }

    /**
     * Determines if the object is one of a set of archetypes.
     *
     * @param shortNames the archetype short names. May contain wildcards
     * @return <tt>true</tt> if the object is one of <tt>shortNames</tt>
     */
    public boolean isA(String ... shortNames) {
        return TypeHelper.isA(object, shortNames);
    }

    /**
     * Determines if a node exists.
     *
     * @param name the node name
     * @return <tt>true</tt> if the node exists, otherwise
     *         <tt>false</tt>
     */
    public boolean hasNode(String name) {
        return (getDescriptor(name) != null);
    }

    /**
     * Returns the named node's descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to <tt>name</tt> or
     *         <tt>null</tt> if none exists.
     */
    public NodeDescriptor getDescriptor(String name) {
        return getArchetype().getNodeDescriptor(name);
    }

    /**
     * Returns the archetype display name.
     *
     * @return the archetype display name, or its short name if none is present.
     */
    public String getDisplayName() {
        return getArchetype().getDisplayName();
    }

    /**
     * Returns the display name of a node.
     *
     * @param name the node name
     * @return the node display name
     * @throws IMObjectBeanException if the node does't exist
     */
    public String getDisplayName(String name) {
        NodeDescriptor node = getNode(name);
        return node.getDisplayName();
    }

    /**
     * Returns the boolean value of a node.
     *
     * @param name the node name
     * @return the value of the node, or <tt>false</tt> if the node is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public boolean getBoolean(String name) {
        return properties.getBoolean(name);
    }

    /**
     * Returns the boolean value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or <tt>defaultValue</tt> if it
     *         is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        return properties.getBoolean(name, defaultValue);
    }

    /**
     * Returns the integer value of a node.
     *
     * @param name the node name
     * @return the value of the node, or <tt>0</tt> if the node is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public int getInt(String name) {
        return properties.getInt(name);
    }

    /**
     * Returns the integer value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or <tt>defaultValue</tt> if it
     *         is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public int getInt(String name, int defaultValue) {
        return properties.getInt(name, defaultValue);
    }

    /**
     * Returns the long value of a node.
     *
     * @param name the node name
     * @return the value of the node, or <tt>0</tt> if the node is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public long getLong(String name) {
        return properties.getLong(name);
    }

    /**
     * Returns the long value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or <tt>defaultValue</tt> if it
     *         is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public long getLong(String name, long defaultValue) {
        return properties.getLong(name, defaultValue);
    }

    /**
     * Returns the string value of a node.
     *
     * @param name the node name
     * @return the value of the node.
     * @throws IMObjectBeanException if the node does't exist
     */
    public String getString(String name) {
        return properties.getString(name);
    }

    /**
     * Returns the string value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or <tt>defaultValue</tt> if it
     *         is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public String getString(String name, String defaultValue) {
        return properties.getString(name, defaultValue);
    }

    /**
     * Returns the <tt>BigDecimal</tt> value of a node.
     *
     * @param name the node name
     * @return the value of the node. May be <tt>null</tt>
     * @throws IMObjectBeanException if the node does't exist
     */
    public BigDecimal getBigDecimal(String name) {
        return properties.getBigDecimal(name);
    }

    /**
     * Returns the <tt>BigDecimal</tt> value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or <tt>defaultValue</tt> if it
     *         is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public BigDecimal getBigDecimal(String name, BigDecimal defaultValue) {
        return properties.getBigDecimal(name, defaultValue);
    }

    /**
     * Returns the <tt>Money</tt> value of a node.
     *
     * @param name the node name
     * @return the value of the node. May be <tt>null</tt>
     * @throws IMObjectBeanException if the node does't exist
     */
    public Money getMoney(String name) {
        return properties.getMoney(name);
    }

    /**
     * Returns the <tt>BigDecimal</tt> value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or <tt>defaultValue</tt> if it
     *         is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public Money getMoney(String name, Money defaultValue) {
        return properties.getMoney(name, defaultValue);
    }

    /**
     * Returns the <tt>Date</tt> value of a node.
     *
     * @param name the node name
     * @return the value of the node
     * @throws IMObjectBeanException if the node does't exist
     */
    public Date getDate(String name) {
        return properties.getDate(name);
    }

    /**
     * Returns the <tt>Date</tt> value of a node.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is null
     * @return the value of the node, or <tt>defaultValue</tt> if it
     *         is null
     * @throws IMObjectBeanException if the node does't exist
     */
    public Date getDate(String name, Date defaultValue) {
        return properties.getDate(name, defaultValue);
    }

    /**
     * Returns the reference value of a node.
     *
     * @param node the node name
     * @return the node value
     */
    public IMObjectReference getReference(String node) {
        return properties.getReference(node);
    }

    /**
     * Returns the object at the specified node.
     * <p/>
     * If the named object is an {@link IMObjectReference}, it will be
     * resolved.
     *
     * @param node the node name
     * @return the node value
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject getObject(String node) {
        Object value = getValue(node);
        if (value instanceof IMObjectReference) {
            return resolve((IMObjectReference) value);
        }
        return (IMObject) value;
    }

    /**
     * Returns the value of a node.
     *
     * @param name the node name
     * @return the value of the node
     * @throws IMObjectBeanException if the node does't exist
     */
    public Object getValue(String name) {
        NodeDescriptor node = getNode(name);
        return node.getValue(object);
    }

    /**
     * Returns the values of a collection node.
     *
     * @param name the node name
     * @return the collection corresponding to the node
     * @throws IMObjectBeanException if the node does't exist
     */
    public List<IMObject> getValues(String name) {
        NodeDescriptor node = getNode(name);
        return node.getChildren(object);
    }

    /**
     * Returns the values of a collection node, converted to the supplied type.
     *
     * @param name the node name
     * @param type the class type
     * @return the collection corresponding to the node
     * @throws IMObjectBeanException if the node does't exist or an element
     *                               is of the wrong type
     */
    @SuppressWarnings("unchecked")
    public <T extends IMObject> List<T> getValues(String name, Class<T> type) {
        List<IMObject> values = getValues(name);
        for (IMObject value : values) {
            if (!type.isInstance(value)) {
                throw new IMObjectBeanException(
                        InvalidClassCast, type.getName(),
                        value.getClass().getName());
            }
        }
        return (List<T>) values;
    }

    /**
     * Sets the value of a node.
     *
     * @param name  the node name
     * @param value the new node value
     */
    public void setValue(String name, Object value) {
        properties.set(name, value);
    }

    /**
     * Adds a value to a collection.
     *
     * @param name  the node name
     * @param value the value to add
     * @throws IMObjectBeanException if the descriptor does't exist
     */
    public void addValue(String name, IMObject value) {
        NodeDescriptor node = getNode(name);
        node.addChildToCollection(object, value);
    }

    /**
     * Removes a value from a collection.
     *
     * @param name  the node name
     * @param value the value to remove
     */
    public void removeValue(String name, IMObject value) {
        NodeDescriptor node = getNode(name);
        node.removeChildFromCollection(object, value);
    }

    /**
     * Saves the object.
     * <p/>
     * Any derived nodes will have their values derived prior to the object
     * being saved.
     *
     * @throws ArchetypeServiceException if the object can't be saved
     */
    public void save() {
        IArchetypeService service = getArchetypeService();
        service.deriveValues(object);
        service.save(object);
    }

    /**
     * Helper to resolve a reference.
     *
     * @param ref the reference. May be <tt>null</tt>
     * @return the object corresponding to the reference or <tt>null</tt> if
     *         none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObject resolve(IMObjectReference ref) {
        IMObject result = null;
        if (ref != null) {
            return getArchetypeService().get(ref);
        }
        return result;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        if (service == null) {
            service = ArchetypeServiceHelper.getArchetypeService();
        }
        return service;
    }

    /**
     * Returns the archetype descriptor.
     *
     * @return the archetype descriptor
     */
    protected ArchetypeDescriptor getArchetype() {
        if (archetype == null) {
            archetype = DescriptorHelper.getArchetypeDescriptor(
                    object, getArchetypeService());
        }
        return archetype;
    }

    /**
     * Returns a node descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to <tt>name</tt>
     * @throws IMObjectBeanException if the descriptor does't exist
     */
    private NodeDescriptor getNode(String name) {
        NodeDescriptor node = getArchetype().getNodeDescriptor(name);
        if (node == null) {
            String shortName = object.getArchetypeId().getShortName();
            throw new IMObjectBeanException(NodeDescriptorNotFound, name,
                                            shortName);
        }
        return node;
    }

    /**
     * Implementation of {@link PropertySet} for nodes.
     */
    private class NodePropertySet extends AbstractPropertySet {

        /**
         * Returns the property names.
         *
         * @return the property names
         */
        public Set<String> getNames() {
            return getArchetype().getNodeDescriptors().keySet();
        }

        /**
         * Returns the value of a property.
         *
         * @param name the property name
         * @return the value of the property
         * @throws IMObjectBeanException if the descriptor does't exist
         */
        public Object get(String name) {
            NodeDescriptor node = getNode(name);
            return node.getValue(object);
        }

        /**
         * Sets the value of a property.
         *
         * @param name  the propery name
         * @param value the property value
         * @throws IMObjectBeanException if the descriptor does't exist
         * @throws OpenVPMSException     if the property cannot be set
         */
        public void set(String name, Object value) {
            NodeDescriptor node = getNode(name);
            node.setValue(object, value);
        }
    }

}
