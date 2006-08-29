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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.StringUtilities;
import org.openvpms.web.component.app.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.PatternSyntaxException;


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
     * @param reference the object reference. May be <code>null</code>
     * @return the object corresponding to <code>reference</code> or
     *         <code>null</code> if none exists
     */
    public static IMObject getObject(IMObjectReference reference) {
        IMObject result = null;
        if (reference != null) {
            result = Context.getInstance().getObject(reference);
            if (result == null) {
                try {
                    IArchetypeService service
                            = ArchetypeServiceHelper.getArchetypeService();
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
     * Returns an object given its reference and descriptor. If the reference is
     * null, determines if the descriptor matches that of the current object
     * being viewed/edited and returns that instead.
     *
     * @param reference  the object reference. May be <code>null</code>
     * @param descriptor the node descriptor
     * @return the object matching <code>reference</code>, or
     *         <code>descriptor</code>, or <code>null</code> if there is no
     *         matches
     */
    public static IMObject getObject(IMObjectReference reference,
                                     NodeDescriptor descriptor) {
        IMObject result;
        if (reference == null) {
            result = match(descriptor);
        } else {
            result = getObject(reference);
        }
        return result;
    }

    /**
     * Reloads an object.
     *
     * @param object the object to reload. May be <code>null</code>
     * @return the object, or <code>null</code> if it couldn't be reloaded
     */
    public static IMObject reload(IMObject object) {
        IMObject result = null;
        if (object != null) {
            try {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                result = ArchetypeQueryHelper.getByObjectReference(
                        service, object.getObjectReference());
            } catch (OpenVPMSException error) {
                _log.error(error, error);
            }
        }
        return result;
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
        IMObjectBean bean = new IMObjectBean(object);
        return (bean.hasNode(node)) ? bean.getValue(node) : null;
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

    /**
     * Returns a list of objects with matching name.
     * Names are treated as case-insensitive.
     *
     * @param name    the name. May contain wildcards. If null or empty,
     *                indicates no filtering.
     * @param objects the objects to filter
     */
    public static <T extends IMObject> List<T> findByName(
            String name, Collection<T> objects) {
        List<T> result = new ArrayList<T>();
        if (StringUtils.isEmpty(name)) {
            result.addAll(objects);
        } else {
            try {
                String regex = StringUtilities.toRegEx(name.toLowerCase());
                for (T object : objects) {
                    String value = object.getName();
                    if (value != null && value.toLowerCase().matches(regex)) {
                        result.add(object);
                    }
                }
            } catch (PatternSyntaxException exception) {
                _log.warn(exception);
            }
        }
        return result;
    }

    /**
     * Returns a list of entities with matching name, code or identity.
     * All strings are treated as case-insensitive.
     *
     * @param name    the name. May contain wildcards. If null or empty,
     *                indicates no filtering.
     * @param objects the objects to filter
     */
    public static <T extends Entity> List<T> findEntityByName(
            String name, Collection<T> objects) {
        List<T> result = new ArrayList<T>();
        if (StringUtils.isEmpty(name)) {
            result.addAll(objects);
        } else {
            try {
                String regex = StringUtilities.toRegEx(name.toLowerCase());
                for (T object : objects) {
                    if (matches(object.getName(), regex)
                            || matches(object.getCode(), regex)
                            || identityMatches(object, regex)) {
                        result.add(object);
                    }
                }
            } catch (PatternSyntaxException exception) {
                _log.warn(exception);
            }
        }
        return result;
    }

    /**
     * Determines if the current object being edited matches archetype range of
     * the specified descriptor.
     *
     * @param descriptor the node descriptor
     * @return the current object being edited, or <code>null</code> if its type
     *         doesn't matches the specified descriptor's archetype range
     */
    private static IMObject match(NodeDescriptor descriptor) {
        IMObject result = null;
        IMObject object = Context.getInstance().getCurrent();
        if (object != null) {
            for (String shortName : descriptor.getArchetypeRange()) {
                if (TypeHelper.matches(object.getArchetypeId(), shortName)) {
                    result = object;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines if an entity has any identities that match a regular
     * expression.
     *
     * @param object the entity
     * @param regex  the regular expression
     * @return <code>true</code> if there is at least one match;
     *         otherwise <code>false</code>
     * @throws PatternSyntaxException if the expression is invalid
     */
    private static boolean identityMatches(Entity object, String regex) {
        for (EntityIdentity identityh : object.getIdentities()) {
            if (matches(identityh.getIdentity(), regex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if string matches a regular expression.
     *
     * @param value  the value. May be <code>null</code>
     * @param regexp the regular expression
     * @return <code>true</code> if it matches; otherwise <code>false</code>
     * @throws PatternSyntaxException if the expression is invalid
     */
    private static boolean matches(String value, String regexp) {
        return (value != null && value.toLowerCase().matches(regexp));
    }

}

