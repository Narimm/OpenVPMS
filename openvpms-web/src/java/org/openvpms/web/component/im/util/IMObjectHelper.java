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

package org.openvpms.web.component.im.util;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.spring.ServiceHelper;

import org.openvpms.archetype.util.TypeHelper;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.Collection;


/**
 * {@link IMObject} helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectHelper {

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(IMObjectHelper.class);


    /**
     * Returns an object given its reference.
     *
     * @param reference the object reference May be <code>null</code>
     * @return the object corresponding to <code>reference</code> or
     *         <code>null</code> if none exists
     */
    public static IMObject getObject(IMObjectReference reference) {
        IMObject result = null;
        if (reference != null) {
            result = Context.getInstance().getObject(reference);
            if (result == null) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                try {
                    result = ArchetypeQueryHelper.getByObjectReference(
                            service, reference);
                } catch (OpenVPMSException error) {
                    _log.error(error, error);
                }
            }
        }
        return result;
    }

    /**
     * Sets a value on an object.
     *
     * @param object the object
     * @param node   the node name
     * @param value  the object value
     * @throws DescriptorException if the value can't be set
     */
    public static void setValue(IMObject object, String node, Object value) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object);
        setValue(object, archetype, node, value);
    }

    /**
     * Sets a value on an object.
     *
     * @param object    the object
     * @param archetype the archetype descriptor
     * @param node      the node name
     * @param value     the object value
     * @throws DescriptorException if the value can't be set
     */
    public static void setValue(IMObject object,
                                ArchetypeDescriptor archetype, String node,
                                Object value) {
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        if (descriptor != null) {
            descriptor.setValue(object, value);
        }
    }

    /**
     * Returns a value from an object, given the value's node descriptor name.
     *
     * @param object the object
     * @param node   the node name
     * @return the value corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static Object getValue(IMObject object, String node) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object);
        return getValue(object, archetype, node);
    }

    /**
     * Returns a value from an object, given the value's node descriptor name.
     *
     * @param object    the object
     * @param archetype the archetype descriptor
     * @param node      the node name
     * @return the value corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static Object getValue(IMObject object,
                                  ArchetypeDescriptor archetype, String node) {
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        if (descriptor != null) {
            return descriptor.getValue(object);
        }
        return null;
    }

    /**
     * Returns a string value from an object, given the value's node descriptor
     * name.
     *
     * @param object the object
     * @param node   the node name
     * @return the value corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static String getString(IMObject object, String node) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object);
        return getString(object, archetype, node);
    }

    /**
     * Returns a string value from an object, given the value's node descriptor
     * name.
     *
     * @param object    the object
     * @param archetype the archetype descriptor
     * @param node      the node name
     * @return the value corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static String getString(IMObject object,
                                   ArchetypeDescriptor archetype,
                                   String node) {
        String result = null;
        Object value = getValue(object, archetype, node);
        if (value != null) {
            result = value.toString();
        }
        return result;
    }

    /**
     * Returns a numeric value from an object, given the value's node descriptor
     * name.
     *
     * @param object the object
     * @param node   the node name
     * @return the value corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static BigDecimal getNumber(IMObject object, String node) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object);
        return getNumber(object, archetype, node);
    }

    /**
     * Returns a numeric value from an object, given the value's node descriptor
     * name.
     *
     * @param object    the object
     * @param archetype the archetype descriptor
     * @param node      the node name
     * @return the value corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static BigDecimal getNumber(IMObject object,
                                       ArchetypeDescriptor archetype,
                                       String node) {
        BigDecimal result = null;
        Object value = getValue(object, archetype, node);
        if (value instanceof BigDecimal) {
            result = (BigDecimal) value;
        } else if (value instanceof Number) {
            Number number = (Number) value;
            if (number instanceof Float || number instanceof Double) {
                result = new BigDecimal(number.doubleValue());
            } else {
                result = new BigDecimal(number.longValue());
            }
        }
        return result;
    }

    /**
     * Returns a collection from an object, given the collection's node
     * descriptor name.
     *
     * @param object the object
     * @param node   the node name
     * @return the collection corresponding to <code>node</code>. May be
     *         <code>null</code>
     */
    public static Collection getValues(IMObject object, String node) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(object);
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        if (descriptor != null) {
            return descriptor.getChildren(object);
        }
        return null;
    }

    /**
     * Returns the first object instance from a collection with matching short
     * name.
     *
     * @param shortName the short name
     * @param objects   the objects to search
     * @return the first object from the collection with matching short name, or
     *         <code>null</code> if none exists.
     */
    public static <T extends IMObject> T
            getObject(String shortName, Collection<T> objects) {
        T result = null;
        for (T object : objects) {
            if (TypeHelper.isA(object, shortName)) {
                result = object;
                break;
            }
        }
        return result;
    }

}

