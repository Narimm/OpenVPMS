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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.spring.ServiceHelper;


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
     * Determines if an object is an instance of a particular archetype.
     *
     * @param object    the object. May be <code>null</code>
     * @param shortName the archetype short name. May contain wildcards
     * @return <code>true</code> if object is an instance of
     *         <code>shortName</code>
     */
    public static boolean isA(IMObject object, String shortName) {
        if (object != null) {
            return DescriptorHelper.matches(object.getArchetypeId(), shortName);
        }
        return false;
    }

    /**
     * Determines if an object is one of a set of archetypes.
     *
     * @param object     the object. May be <code>null</code>
     * @param shortNames the archetype short names. May contain wildcards
     * @return <code>true</code> if object is one of <code>shortNames</code>
     */
    public static boolean isA(IMObject object, String ... shortNames) {
        if (object != null) {
            for (String shortName : shortNames) {
                if (isA(object, shortName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if an object reference refers to an instance of a particular
     * archetype.
     *
     * @param reference the object. May be <code>null</code>
     * @param shortName the archetype short name. May contain wildcards
     * @return <code>true</code> if the reference refers to an instance of
     *         <code>shortName</code>
     */
    public static boolean isA(IMObjectReference reference, String shortName) {
        if (reference != null) {
            return DescriptorHelper.matches(reference.getArchetypeId(),
                                            shortName);
        }
        return false;
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
            if (isA(object, shortName)) {
                result = object;
                break;
            }
        }
        return result;
    }

}

