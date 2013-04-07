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

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is a helper class for retrieving lookups reference data.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupHelper {

    /**
     * Return a list of lookups for the specified {@link NodeDescriptor}.
     *
     * @param service    a reference to the archetype service
     * @param descriptor the node descriptor
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static List<Lookup> get(IArchetypeService service,
                                   NodeDescriptor descriptor) {
        LookupAssertion assertion = LookupAssertionFactory.create(
                descriptor, service, LookupServiceHelper.getLookupService());
        return assertion.getLookups();
    }

    /**
     * Return a list of lookups for the specified {@link NodeDescriptor}.
     * This method returns partially populated lookups based on the names
     * of the nodes supplied, and can be used to increase performance where
     * only a few lookup details are required.
     *
     * @param service    a reference to the archetype service
     * @param descriptor the node descriptor
     * @param nodes      the nodes to populate
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     * @deprecated use {@link LookupHelper#get(IArchetypeService, NodeDescriptor)}
     */
    @Deprecated
    public static List<Lookup> get(IArchetypeService service,
                                   NodeDescriptor descriptor,
                                   Collection<String> nodes) {
        LookupAssertion assertion = LookupAssertionFactory.create(
                descriptor, service, LookupServiceHelper.getLookupService());
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
    public static Collection<Lookup> get(IArchetypeService service,
                                         NodeDescriptor descriptor,
                                         IMObject object) {
        LookupAssertion assertion = LookupAssertionFactory.create(
                descriptor, service, LookupServiceHelper.getLookupService());
        return assertion.getLookups(object);
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
     * @deprecated use {@link LookupHelper#get(IArchetypeService, NodeDescriptor, IMObject)}
     */
    @Deprecated
    public static Collection<Lookup> get(IArchetypeService service,
                                         NodeDescriptor descriptor,
                                         IMObject object,
                                         Collection<String> nodes) {
        LookupAssertion assertion = LookupAssertionFactory.create(
                descriptor, service, LookupServiceHelper.getLookupService());
        return assertion.getLookups(object);
    }

    /**
     * Helper method that returns a single lookup for the specified node.
     *
     * @param service    the archetype service
     * @param descriptor the node descriptor
     * @param object     the object
     * @return the lookup, or <code>null</code> if none is found
     * @throws ArchetypeServiceException if the request cannot complete
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Lookup getLookup(IArchetypeService service,
                                   NodeDescriptor descriptor, IMObject object) {
        Lookup result = null;
        if (!descriptor.isLookup()) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.InvalidLookupAssertion,
                    new Object[]{descriptor.getName()});
        }
        Object value = descriptor.getValue(object);
        if (value != null) {
            LookupAssertion assertion = LookupAssertionFactory.create(
                    descriptor, service,
                    LookupServiceHelper.getLookupService());
            result = assertion.getLookup(object, (String) value);
        }
        return result;
    }

    /**
     * Returns the name of a lookup referred to by an object's node.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     * @param object        the object
     * @param node          the node
     * @return the lookup name, or <code>null</code> if none is found
     * @throws PropertyResolverException if the node doesn't exist
     * @throws ArchetypeServiceException if the request cannot complete
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static String getName(IArchetypeService service, ILookupService lookupService, IMObject object,
                                 String node) {
        NodeResolver resolver = new NodeResolver(object, service);
        NodeResolver.State state = resolver.resolve(node);
        NodeDescriptor descriptor = state.getLeafNode();
        if (descriptor == null) {
            throw new PropertyResolverException(
                    PropertyResolverException.ErrorCode.InvalidProperty, node);
        }
        return getName(service, lookupService, descriptor, state.getParent());
    }

    /**
     * Helper method that returns the name of a lookup referred to by a node.
     *
     * @param service    the archetype service
     * @param descriptor the node descriptor
     * @param object     the object
     * @return the lookup name, or <code>null</code> if none is found
     * @throws ArchetypeServiceException if the request cannot complete
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static String getName(IArchetypeService service,
                                 NodeDescriptor descriptor, IMObject object) {
        return getName(service, LookupServiceHelper.getLookupService(), descriptor, object);
    }

    /**
     * Helper method that returns the name of a lookup referred to by a node.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     * @param descriptor    the node descriptor
     * @param object        the object
     * @return the lookup name, or <code>null</code> if none is found
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
     * @param service   the archetype service
     * @param shortName the archetype short name
     * @param node      the node name
     * @return a map of lookup codes to lookup names
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Map<String, String> getNames(IArchetypeService service,
                                               String shortName,
                                               String node) {
        return getNames(service, LookupServiceHelper.getLookupService(), shortName, node);
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
     * @param service    the archetype service
     * @param descriptor the node descriptor
     * @return a map of lookup codes to lookup names
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Map<String, String> getNames(IArchetypeService service, NodeDescriptor descriptor) {
        return getNames(service, LookupServiceHelper.getLookupService(), descriptor);
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
        Map<String, String> result = new HashMap<String, String>();
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
     * Helper method that returns a single lookup with the specified
     * archetype short name and code.
     *
     * @param service   the archetype sevice
     * @param shortName the archetype short name
     * @param code      the lookup code
     * @return the lookup, or <code>null</code> if none can be found
     * @throws ArchetypeServiceException if the request cannot complete
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static Lookup getLookup(IArchetypeService service, String shortName,
                                   String code) {
        ILookupService lookupService = LookupServiceHelper.getLookupService();
        return lookupService.getLookup(shortName, code);
    }

    /**
     * Helper method that returns a single lookup matching one of the specified
     * archetype short names and code.
     *
     * @param service    the archetype sevice
     * @param shortNames the archetype short names to search on
     * @param code       the lookup code
     * @return the lookup, or <code>null</code> if none can be found
     * @throws ArchetypeServiceException if the request cannot complete
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static Lookup getLookup(IArchetypeService service,
                                   String[] shortNames,
                                   String code) {
        ILookupService lookupService = LookupServiceHelper.getLookupService();
        for (String shortName : shortNames) {
            Lookup lookup = lookupService.getLookup(shortName, code);
            if (lookup != null) {
                return lookup;
            }
        }
        return null;
    }

    /**
     * Helper method that returns a specific lookup given a short name and a
     * value
     *
     * @param service   the archetype sevice
     * @param shortName the archetype short name
     * @param firstRow  the first row to retriev
     * @param numOfRows the num of rows to retieve
     * @return IPage<Lookup>
     * @throws ArchetypeServiceException if the request cannot complete
     * @deprecated use {@link ILookupService#getLookups(String)}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static List<Lookup> getLookups(IArchetypeService service,
                                          String shortName,
                                          int firstRow, int numOfRows) {
        Collection<Lookup> lookups
                = LookupServiceHelper.getLookupService().getLookups(shortName);
        return new ArrayList<Lookup>(lookups);
    }

    /**
     * Helper method to return a list of target {@link Lookup} instances
     * give a reference source {@link Lookup}.
     *
     * @param service a reference to the archetype service
     * @param source  the source lookup
     * @param target  the archetype shortNames of the target
     * @return a list of lookup objects
     * @throws ArchetypeServiceException for any archetype service error
     * @deprecated use {@link ILookupService#getTargetLookups(Lookup)}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static List<Lookup> getTargetLookups(IArchetypeService service,
                                                Lookup source,
                                                String[] target) {
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint(
                        target, false, false))
                .add(new CollectionNodeConstraint("target", false)
                             .add(new ObjectRefNodeConstraint("source",
                                                              source.getObjectReference())))
                .add(new NodeSortConstraint("name", true))
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .setActiveOnly(true);

        return new ArrayList<Lookup>((List) service.get(query).getResults());
    }

    /**
     * Helper to return a list of source {@link Lookup} instances given a
     * reference target{@link Lookup}.
     * <p/>
     * Note this will work if the source lookups have a 'source' collection
     * node of lookup relationships, and each relationship has a 'target' node.
     *
     * @param service a reference to the archetype service
     * @param target  the target lookup
     * @param source  the list of shortnames for the source
     * @return a list of lookup objects
     * @throws ArchetypeServiceException if the request cannot complete
     * @deprecated use {@link ILookupService#getSourceLookups(Lookup)}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static List<Lookup> getSourceLookups(IArchetypeService service,
                                                Lookup target,
                                                String[] source) {
        ArchetypeQuery query = new ArchetypeQuery(
                new ShortNameConstraint(
                        source, false, false))
                .add(new CollectionNodeConstraint("source", false)
                             .add(new ObjectRefNodeConstraint("target",
                                                              target.getObjectReference())))
                .add(new NodeSortConstraint("name", true))
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .setActiveOnly(true);

        return new ArrayList<Lookup>((List) service.get(query).getResults());
    }

    /**
     * Return the default lookup object for the specified archetype short name.
     * It assumes that the archetype short name refers to a {@link Lookup}.
     * <p/>
     * This method also assumes that the specified archetype short name has a
     * node called 'defaultLookup' defined, which is of type boolean.
     *
     * @param service the archetype service
     * @param lookup  the archetype short name of the lookup.
     * @return IMObject
     *         the object of null if one does not exist
     * @throws ArchetypeServiceException if the request cannot complete
     * @deprecated use {@link ILookupService#getDefaultLookup(String)}
     */
    @Deprecated
    public static Lookup getDefaultLookup(IArchetypeService service,
                                          String lookup) {
        return LookupServiceHelper.getLookupService().getDefaultLookup(lookup);
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
