/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolverException;
import org.openvpms.component.business.service.archetype.helper.PropertySetResolver;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.List;


/**
 * This class provides a list of helper functions for using the archetype service.
 * <p/>
 * These functions can then be used as JXPath extension functions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 */
public class ArchetypeServiceFunctions {

    /**
     * This will take a list of {@link IMObjectReference} instances and return
     * a list of the corresponding {@link IMObject} instances.
     *
     * @param references a list of references
     * @return List<IMObject>
     */
    public static List<IMObject> resolveRefs(List<IMObjectReference> references) {
        List<IMObject> objects = new ArrayList<IMObject>();
        for (IMObjectReference ref : references) {
            objects.add(ArchetypeServiceHelper.getArchetypeService().get(ref));
        }

        return objects;
    }

    /**
     * Return the {@link IMObject} given a reference.
     *
     * @param reference the reference
     * @return IMObject
     */
    public static IMObject resolve(IMObjectReference reference) {
        return ArchetypeServiceHelper.getArchetypeService().get(reference);
    }

    /**
     * Resolves the value at the specified property.
     *
     * @param object the expression context. May be {@code null}
     * @param name   the property name
     * @return the value at the specified property. May be {@code null}
     * @throws PropertyResolverException if the name is invalid
     * @see NodeResolver
     */
    public static Object get(Object object, String name) {
        Object result;
        object = unwrap(object);
        if (object == null) {
            result = null;
        } else if (object instanceof IMObject) {
            result = getNode((IMObject) object, name);
        } else {
            result = getProperty((PropertySet) object, name);
        }
        return result;
    }

    /**
     * Resolves the value at the specified node.
     * If the node doesn't exist returns the specified default value.     *
     *
     * @param object       the expression context. May be {@code null}
     * @param name         the node name
     * @param defaultValue the value to return if node does not exist
     * @return the value at the specified node or defaultValue if {@code object} is {@code null} or {@code node} does
     *         not exist
     * @see NodeResolver
     */
    public static Object get(Object object, String name, Object defaultValue) {
        Object result;
        object = unwrap(object);
        if (object == null) {
            result = defaultValue;
        } else if (object instanceof IMObject) {
            result = getNode((IMObject) object, name, defaultValue);
        } else {
            result = getProperty((PropertySet) object, name, defaultValue);
        }
        return result;
    }

    /**
     * Updates the value of the specified node and saves the object.
     *
     * @param object the object
     * @param node   the node
     * @param value  the value to set
     * @throws OpenVPMSException if the node cannot be updated or the object cannot be saved
     */
    public static void set(IMObject object, String node, Object value) {
        IMObjectBean bean = new IMObjectBean(object, ArchetypeServiceHelper.getArchetypeService());
        bean.setValue(node, value);
        bean.save();
    }

    /**
     * Resolves the name of a lookup, given its node.
     *
     * @param object the object. May be {@code null}
     * @param node   the node name
     * @return the lookup's name or {@code null} if {@code object} is {@code null}
     * @throws PropertyResolverException if the node is invalid
     * @throws OpenVPMSException         if the call cannot complete
     * @see NodeResolver
     */
    public static String lookup(IMObject object, String node) {
        if (object == null) {
            return null;
        }
        return LookupHelper.getName(ArchetypeServiceHelper.getArchetypeService(),
                                    LookupServiceHelper.getLookupService(), object, node);
    }

    /**
     * Resolves the name of a lookup, given its node.
     * If the node doesn't exist returns the specified default value.
     *
     * @param object       the object
     * @param node         the node name
     * @param defaultValue the default value
     * @return the lookup's name, or {@code defaultValue} if the lookup node cannot be found
     * @throws OpenVPMSException if the call cannot complete
     * @see NodeResolver
     */
    public static String lookup(IMObject object, String node, String defaultValue) {
        String result;
        if (object != null) {
            try {
                result = lookup(object, node);
            } catch (PropertyResolverException exception) {
                result = defaultValue;
            }
        } else {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Returns the default lookup for a node.
     *
     * @param object the object. May be {@code null}
     * @param node   the node name
     * @return the default lookup, or {@code null} if there is no default lookup
     * @throws OpenVPMSException if the call cannot complete
     */
    public static Object defaultLookup(IMObject object, String node) {
        Lookup lookup = null;
        if (object != null) {
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            NodeResolver resolver = new NodeResolver(object, service);
            NodeResolver.State state = resolver.resolve(node);
            NodeDescriptor descriptor = state.getLeafNode();
            if (descriptor == null) {
                throw new PropertyResolverException(PropertyResolverException.ErrorCode.InvalidProperty, node);
            }
            ILookupService lookups = LookupServiceHelper.getLookupService();
            if (descriptor.isLookup()) {
                LookupAssertion assertion = LookupAssertionFactory.create(descriptor, service, lookups);
                lookup = assertion.getDefault();
            } else {
                String[] shortNames = DescriptorHelper.getShortNames(descriptor, service);
                if (!TypeHelper.matches(shortNames, "lookup.*")) {
                    throw new PropertyResolverException(PropertyResolverException.ErrorCode.InvalidProperty, node);
                }
                for (String shortName : shortNames) {
                    lookup = lookups.getDefaultLookup(shortName);
                    if (lookup != null) {
                        break;
                    }
                }
            }
        }
        return lookup;
    }

    /**
     * Resolves the value of the specified node.
     *
     * @param object the expression context
     * @param node   the node name
     * @return the value at the specified node. May be {@code null}
     * @throws PropertyResolverException if the name is invalid
     * @see NodeResolver
     */
    private static Object getNode(IMObject object, String node) {
        NodeResolver resolver = new NodeResolver(object, ArchetypeServiceHelper.getArchetypeService());
        return resolver.getObject(node);
    }

    /**
     * Resolves the value at the specified node.
     * If the node doesn't exist returns the specified default value.     *
     *
     * @param object       the expression context
     * @param node         the node name
     * @param defaultValue the value to return if node does not exist
     * @return the value at the specified node or defaultValue or {@code node} if does not exist
     * @see NodeResolver
     */
    private static Object getNode(IMObject object, String node, Object defaultValue) {
        try {
            return getNode(object, node);
        } catch (PropertyResolverException exception) {
            return defaultValue;
        }
    }

    /**
     * Resolves the value of the specified property.
     *
     * @param set      the expression context
     * @param property the property name
     * @return the value at the specified node
     * @throws PropertyResolverException if the name is invalid
     * @see NodeResolver
     */
    private static Object getProperty(PropertySet set, String property) {
        PropertyResolver resolver = new PropertySetResolver(set, ArchetypeServiceHelper.getArchetypeService());
        return resolver.getObject(property);
    }

    /**
     * Resolves the value of the specified property.
     * If the property doesn't exist returns the specified default value.     *
     *
     * @param set          the expression context
     * @param property     the property name
     * @param defaultValue the value to return if node does not exist
     * @return the value at the specified node or defaultValue if node does not exist
     * @see NodeResolver
     */
    private static Object getProperty(PropertySet set, String property, Object defaultValue) {
        try {
            return getProperty(set, property);
        } catch (PropertyResolverException exception) {
            return defaultValue;
        }
    }

    /**
     * Helper to get access to the actual object supplied by JXPath.
     * <p/>
     * Ideally, ArchetypeServiceFunctions would define overloaded methods for each of the get() methods, except
     * that JXPath can't handle the case where a null is supplied for IMObject or PropertySet argument.
     * To get round this, two get() methods are used each taking an Object argument, and the appropriate method is
     * delegated to called. JXPath passes a list containing the actual object in this case, so it needs to be unwrapped.
     * Note that if ExpressionContext is used instead of Object, JXPath invokes the wrong get() method.
     *
     * @param object the object to unwrap
     * @return the unwrapped object. May be {@code null}
     */
    private static Object unwrap(Object object) {
        if (object instanceof List) {
            List values = (List) object;
            object = !values.isEmpty() ? values.get(0) : null;
        }
        return object;
    }

}
