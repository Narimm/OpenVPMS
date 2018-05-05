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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.archetype.AssertionDescriptor;
import org.openvpms.component.system.common.util.PropertyState;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is a helper class for retrieving lookups reference data.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class LookupHelper {

    /**
     * Return a list of lookups for the specified {@link NodeDescriptor}.
     *
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param descriptor the node descriptor
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static List<Lookup> get(IArchetypeService service, ILookupService lookups, NodeDescriptor descriptor) {
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor, service, lookups);
        return assertion.getLookups();
    }

    /**
     * Return a list of {@link Lookup} instances given the specified
     * {@link NodeDescriptor} and {@link IMObject}.
     * <p/>
     * This method should be used if you want to constrain a lookup
     * search based on a source or target relationship.
     *
     * @param service    a reference to the archetype service
     * @param descriptor the node descriptor
     * @param object     the object to use
     * @return List<Lookup>
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Collection<Lookup> get(IArchetypeService service, ILookupService lookups, NodeDescriptor descriptor,
                                         IMObject object) {
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor, service, lookups);
        return assertion.getLookups(object);
    }

    /**
     * Returns the name of a lookup referred to by an object's node.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     * @param object        the object
     * @param node          the node
     * @return the lookup name, or {@code null} if none is found
     * @throws PropertyResolverException if the node doesn't exist
     * @throws ArchetypeServiceException if the request cannot complete
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static String getName(IArchetypeService service, ILookupService lookupService, IMObject object,
                                 String node) {
        NodeResolver resolver = new NodeResolver(object, service, lookupService);
        PropertyState state = resolver.resolve(node);
        NodeDescriptor descriptor = state.getNode();
        if (descriptor == null) {
            throw new PropertyResolverException(
                    PropertyResolverException.ErrorCode.InvalidProperty, node);
        }
        return getName(service, lookupService, descriptor, state.getParent());
    }

    /**
     * Helper method that returns the name of a lookup referred to by a node.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     * @param descriptor    the node descriptor
     * @param object        the object
     * @return the lookup name, or {@code null} if none is found
     * @throws ArchetypeServiceException if the request cannot complete
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static String getName(IArchetypeService service, ILookupService lookupService,
                                 NodeDescriptor descriptor, IMObject object) {
        String result = null;
        if (!descriptor.isLookup()) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.InvalidLookupAssertion,
                    new Object[]{descriptor.getName()});
        }
        Object value = descriptor.getValue(object);
        if (value != null) {
            LookupAssertion assertion = LookupAssertionFactory.create(descriptor, service, lookupService);
            result = assertion.getName(object, (String) value);
        }
        return result;
    }

    /**
     * Returns a list of lookups for the specified archetype short name and node
     * name.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     * @param shortName     the archetype short name
     * @param node          the node name
     * @return a map of lookup codes to lookup names
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Map<String, String> getNames(IArchetypeService service, ILookupService lookupService,
                                               String shortName, String node) {
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(shortName, service);
        if (archetype != null) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor != null) {
                return getNames(service, lookupService, descriptor);
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Returns a map of lookup codes to lookup names for the specified
     * {@link NodeDescriptor}.
     *
     * @param service       the archetype service
     * @param lookupService lookup service
     * @param descriptor    the node descriptor
     * @return a map of lookup codes to lookup names
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Map<String, String> getNames(IArchetypeService service, ILookupService lookupService,
                                               NodeDescriptor descriptor) {
        Map<String, String> result = new HashMap<>();
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor, service, lookupService);
        List<Lookup> lookups = assertion.getLookups();
        for (Lookup lookup : lookups) {
            if (lookup.isActive()) {
                result.put(lookup.getCode(), lookup.getName());
            }
        }
        return result;
    }

    /**
     * Return the unspecified lookup value give the following
     * {@link NodeDescriptor}. If the  specified descriptor does not contain
     * any lookup assertions then return null.
     *
     * @param ndesc the node descriptor
     * @return String
     *         the unspecified value or null
     */
    public static String getUnspecifiedValue(NodeDescriptor ndesc) {
        String value = null;

        if (ndesc != null) {
            AssertionDescriptor assertion
                    = ndesc.getAssertionDescriptor("lookup");
            if (assertion != null) {
                value = LookupAssertionHelper.getValue(assertion,
                                                       "unspecified");
            }
        }

        return value;
    }

}
