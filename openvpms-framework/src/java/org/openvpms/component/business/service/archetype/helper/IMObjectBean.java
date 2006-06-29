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

import org.apache.commons.jxpath.util.TypeConverter;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import static org.openvpms.component.business.service.archetype.helper.IMObjectBeanException.ErrorCode.NodeDescriptorNotFound;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


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
    private final IMObject _object;

    /**
     * The archetype.
     */
    private final ArchetypeDescriptor _archetype;

    /**
     * Used to convert node values to a particular type.
     */
    private static final TypeConverter CONVERTER = new OpenVPMSTypeConverter();


    /**
     * Constructs a new <code>IMObjectBean</code>.
     *
     * @param object the object
     */
    public IMObjectBean(IMObject object) {
        _archetype = DescriptorHelper.getArchetypeDescriptor(object);
        _object = object;
    }

    /**
     * Returns the underlying object.
     *
     * @return the object
     */
    public IMObject getObject() {
        return _object;
    }

    /**
     * Determines if a node exists.
     *
     * @param name the node name
     * @return <code>true</code> if the node exists, otherwise
     *         <code>false</code>
     */
    public boolean hasNode(String name) {
        return (getDescriptor(name) != null);
    }

    /**
     * Returns the named node's descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to <code>name</code> or
     *         <code>null</code> if none exists.
     */
    public NodeDescriptor getDescriptor(String name) {
        return _archetype.getNodeDescriptor(name);
    }

    /**
     * Returns the archetype display name.
     *
     * @return the archetype display name, or its short name if none is present.
     */
    public String getDisplayName() {
        return _archetype.getDisplayName();
    }

    /**
     * Returns the display name of a node.
     *
     * @param name the node name
     * @return the node display name
     */
    public String getDisplayName(String name) {
        NodeDescriptor node = getNode(name);
        return node.getDisplayName();
    }

    /**
     * Returns the boolean value of a node.
     *
     * @param name the node name
     * @return the value of the node
     */
    public boolean getBoolean(String name) {
        return (Boolean) getValue(name, boolean.class);
    }

    /**
     * Returns the integer value of a node.
     *
     * @param name the node name
     * @return the value of the node
     */
    public int getInt(String name) {
        return (Integer) getValue(name, int.class);
    }

    /**
     * Returns the long value of a node.
     *
     * @param name the node name
     * @return the value of the node
     */
    public long getLong(String name) {
        return (Long) getValue(name, long.class);
    }

    /**
     * Returns the string value of a node.
     *
     * @param name the node name
     * @return the value of the node.
     */
    public String getString(String name) {
        return (String) getValue(name, String.class);
    }

    /**
     * Returns the <code>BigDecimal</code> value of a node.
     *
     * @param name the node name
     * @return the value of the node
     */
    public BigDecimal getBigDecimal(String name) {
        return (BigDecimal) getValue(name, BigDecimal.class);
    }

    /**
     * Returns the <code>Date</code> value of a node.
     *
     * @param name the node name
     * @return the value of the node
     */
    public Date getDate(String name) {
        return (Date) getValue(name, Date.class);
    }

    /**
     * Returns the value of a node.
     *
     * @param name the node name
     * @return the value of the node
     */
    public Object getValue(String name) {
        NodeDescriptor node = getNode(name);
        return node.getValue(_object);
    }

    /**
     * Returns the values of a collection node.
     *
     * @param name the node name
     * @return the collection corresponding to the node
     */
    public List<IMObject> getValues(String name) {
        NodeDescriptor node = getNode(name);
        return node.getChildren(_object);
    }

    /**
     * Sets the value of a node.
     *
     * @param name  the node name
     * @param value the new node value
     */
    public void setValue(String name, Object value) {
        NodeDescriptor node = getNode(name);
        node.setValue(_object, value);
    }

    /**
     * Adds a value to a collection.
     *
     * @param name  the node name
     * @param value the value to add
     */
    public void addValue(String name, IMObject value) {
        NodeDescriptor node = getNode(name);
        node.addChildToCollection(_object, value);
    }

    /**
     * Removes a value from a collection.
     *
     * @param name  the node name
     * @param value the value to remove
     */
    public void removeValue(String name, IMObject value) {
        NodeDescriptor node = getNode(name);
        node.removeChildFromCollection(_object, value);
    }

    /**
     * Converts a value to a particular type.
     *
     * @param name the node name
     * @param type the type to convert to
     * @return the value of the node as an instance of <code>type</code>
     */
    protected Object getValue(String name, Class type) {
        Object value = getValue(name);
        return CONVERTER.convert(value, type);
    }

    /**
     * Returns a node descriptor.
     *
     * @param name the node name
     * @return the descriptor corresponding to <code>name</code>
     * @throws IMObjectBeanException if the descriptor does't exist
     */
    private NodeDescriptor getNode(String name) {
        NodeDescriptor node = _archetype.getNodeDescriptor(name);
        if (node == null) {
            String shortName = _object.getArchetypeId().getShortName();
            throw new IMObjectBeanException(NodeDescriptorNotFound, name,
                                            shortName);
        }
        return node;
    }

}
