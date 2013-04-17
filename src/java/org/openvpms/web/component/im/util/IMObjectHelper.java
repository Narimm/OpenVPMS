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
 */

package org.openvpms.web.component.im.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.util.StringUtilities;
import org.openvpms.web.component.app.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;


/**
 * {@link IMObject} helper methods.
 *
 * @author Tim Anderson
 */
public class IMObjectHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectHelper.class);


    /**
     * Returns an object given its reference.
     * This checks the specified context first. If not found in the context,
     * tries to retrieve it from the archetype service.
     * <p/>
     * Note that if the object in the context is only partially populated,
     * (as indicated by a <tt>version &lt 0</tt>), the actual object will
     * be retrieved from the archetype service.
     *
     * @param reference the object reference. May be <tt>null</tt>
     * @param context   the context to use. If <tt>null</tt> accesses the
     *                  archetype service
     * @return the object corresponding to <tt>reference</tt> or <tt>null</tt>
     *         if none exists
     */
    public static IMObject getObject(IMObjectReference reference,
                                     Context context) {
        IMObject result = null;
        if (reference != null) {
            if (context != null) {
                result = context.getObject(reference);
            }
            if (result == null || result.getVersion() < 0) {
                try {
                    IArchetypeService service
                            = ArchetypeServiceHelper.getArchetypeService();
                    result = service.get(reference);
                } catch (OpenVPMSException error) {
                    log.error(error, error);
                }
            }
        }
        return result;
    }

    /**
     * Returns nodes for an object, given its reference.
     *
     * @param reference the object reference. May be <tt>null</tt>
     * @param nodes     the nodes to return
     * @return the nodes of the object corresponding to <tt>reference</tt>
     *         or <tt>null</tt> if none exists
     */
    public static NodeSet getNodes(IMObjectReference reference,
                                   String... nodes) {
        NodeSet result = null;
        if (reference != null) {
            try {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                ArchetypeQuery query = new ArchetypeQuery(reference);
                query.setCountResults(false);
                IPage<NodeSet> page = service.getNodes(query,
                                                       Arrays.asList(nodes));
                if (page.getResults().size() == 1) {
                    result = page.getResults().get(0);
                }
            } catch (OpenVPMSException error) {
                log.error(error, error);
            }
        }
        return result;
    }

    /**
     * Returns the name of an object, given its reference.
     *
     * @param reference the object reference. May be <tt>null</tt>
     * @return the name or <tt>null</tt> if none exists
     */
    public static String getName(IMObjectReference reference) {
        if (reference != null) {
            try {
                ObjectRefConstraint constraint
                        = new ObjectRefConstraint("o", reference);
                ArchetypeQuery query = new ArchetypeQuery(constraint);
                query.add(new NodeSelectConstraint("o.name"));
                query.setMaxResults(1);
                Iterator<ObjectSet> iter = new ObjectSetQueryIterator(query);
                if (iter.hasNext()) {
                    ObjectSet set = iter.next();
                    return set.getString("o.name");
                }
            } catch (OpenVPMSException error) {
                log.error(error, error);
            }
        }
        return null;
    }

    /**
     * Determines if an object associated with a reference is active.
     *
     * @param reference the object reference. May be <tt>null</tt>
     * @return <tt>true</tt> if the object is active, otherwise <tt>false</tt>
     */
    public static boolean isActive(IMObjectReference reference) {
        if (reference != null) {
            try {
                ObjectRefConstraint constraint
                        = new ObjectRefConstraint("o", reference);
                ArchetypeQuery query = new ArchetypeQuery(constraint);
                query.add(new NodeSelectConstraint("o.active"));
                query.setMaxResults(1);
                Iterator<ObjectSet> iter = new ObjectSetQueryIterator(query);
                if (iter.hasNext()) {
                    ObjectSet set = iter.next();
                    return set.getBoolean("o.active");
                }
            } catch (OpenVPMSException error) {
                log.error(error, error);
            }
        }
        return false;
    }

    /**
     * Returns an object given its reference and descriptor. If the reference is
     * null, determines if the specified archetype range matches that of the
     * current object being viewed/edited and returns that instead.
     *
     * @param reference  the object reference. May be <tt>null</tt>
     * @param shortNames the archetype range
     * @param context    the context
     * @return the object matching <tt>reference</tt>, or <tt>shortNames</tt>,
     *         or <tt>null</tt> if there are no matches
     */
    public static IMObject getObject(IMObjectReference reference,
                                     String[] shortNames, Context context) {
        IMObject result;
        if (reference == null) {
            result = match(shortNames, context);
        } else {
            result = getObject(reference, context);
        }
        return result;
    }

    /**
     * Reloads an object.
     *
     * @param object the object to reload. May be <tt>null</tt>
     * @return the object, or <tt>null</tt> if it couldn't be reloaded
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> T reload(T object) {
        T result = null;
        if (object != null) {
            try {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                result = (T) service.get(object.getObjectReference());
            } catch (OpenVPMSException error) {
                log.error(error, error);
            }
        }
        return result;
    }

    /**
     * Returns a value from an object, given the value's node descriptor name.
     *
     * @param object the object
     * @param node   the node name
     * @return the value corresponding to <tt>node</tt>. May be
     *         <tt>null</tt>
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
     *         <tt>null</tt> if none exists.
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
                log.warn(exception);
            }
        }
        return result;
    }

    /**
     * Returns the nearest common superclass for a set of archetype short names.
     *
     * @param shortNames the archetype short names
     * @return the common implementation class type
     */
    public static Class getType(String[] shortNames) {
        Class result = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Set<Class> classes = new HashSet<Class>();
        for (String shortName : shortNames) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(shortName);
            try {
                Class clazz = loader.loadClass(archetype.getClassName());
                classes.add(clazz);
            } catch (ClassNotFoundException exception) {
                log.error(exception, exception);
            }
        }
        for (Class clazz : classes) {
            if (result == null) {
                result = clazz;
            } else {
                while (!result.isAssignableFrom(clazz)) {
                    result = result.getSuperclass();
                }
            }
            if (result == Object.class) {
                // shouldn't be the case, default to something sensible
                result = IMObject.class;
                break;
            }
        }
        return (result == null) ? IMObject.class : result;
    }

    /**
     * Determines if the current object being edited matches the specified
     * archetype range.
     *
     * @param shortNames the archetype range
     * @param context    the context
     * @return the current object being edited, or <tt>null</tt> if its type
     *         doesn't matches the specified descriptor's archetype range
     */
    private static IMObject match(String[] shortNames, Context context) {
        IMObject result = null;
        IMObject object = context.getCurrent();
        if (object != null) {
            for (String shortName : shortNames) {
                if (TypeHelper.matches(object.getArchetypeId(), shortName)) {
                    result = object;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines if string matches a regular expression.
     *
     * @param value  the value. May be <tt>null</tt>
     * @param regexp the regular expression
     * @return <tt>true</tt> if it matches; otherwise <tt>false</tt>
     * @throws PatternSyntaxException if the expression is invalid
     */
    private static boolean matches(String value, String regexp) {
        return (value != null && value.toLowerCase().matches(regexp));
    }

    /**
     * Determines if an object has the same object reference and version
     * as another.
     *
     * @param object the object. May be <tt>null</tt>
     * @param other  the object. May be <tt>null</tt>
     * @return <tt>true</tt> if the objects have the same object references
     *         and version, otherwise <tt>false</tt>
     */
    public static boolean isSame(IMObject object, IMObject other) {
        if (object != null && other != null) {
            if (object.getObjectReference().equals(other.getObjectReference())
                    && object.getVersion() == other.getVersion()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a newer version of an object exists.
     *
     * @param object the object
     * @return <tt>true<tt> if a newer version exists,
     *         otherwise <tt>false</tt>. If <tt>object == null</tt>
     *         also returns <tt>false</tt>
     */
    public static boolean hasNewerVersion(IMObject object) {
        boolean result = false;
        if (object != null) {
            try {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                IMObject o = service.get(object.getObjectReference());
                if (o != null && o.getVersion() > object.getVersion()) {
                    result = true;
                }
            } catch (OpenVPMSException exception) {
                log.error(exception, exception);
            }
        }
        return result;
    }
}

