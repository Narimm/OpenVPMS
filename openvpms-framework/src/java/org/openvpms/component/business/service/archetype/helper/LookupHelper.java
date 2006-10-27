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

import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * This is a helper class for retrieving lookups reference data.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupHelper  {

    /**
     * Define a logger for this class.
     */
    private static final Logger logger = Logger.getLogger(LookupHelper.class);


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
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor,
                                                                  service);
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
     * @param nodes
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static List<Lookup> get(IArchetypeService service,
                                   NodeDescriptor descriptor,
                                   Collection<String> nodes) {
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor,
                                                                  service);
        return assertion.getLookups(nodes);
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
    public static List<Lookup> get(IArchetypeService service,
                                   NodeDescriptor descriptor, IMObject object) {
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor,
                                                                  service);
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
     */
    public static List<Lookup> get(IArchetypeService service,
                                   NodeDescriptor descriptor, IMObject object,
                                   Collection<String> nodes) {
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor,
                                                                  service);
        return assertion.getLookups(object, nodes);
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
            LookupAssertion assertion
                    = LookupAssertionFactory.create(descriptor, service);
            result = assertion.getLookup(object, (String) value);
        }
        return result;
    }

    /**
     * Helper method that returns the name of a lookup referred to by a node.
     *
     * @param service the archetype service
     * @param descriptor the node descriptor
     * @param object the object
     * @return the lookup name, or <code>null</code> if none is found
     * @throws ArchetypeServiceException if the request cannot complete
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static String getName(IArchetypeService service,
                                 NodeDescriptor descriptor, IMObject object) {
        String result = null;
        if (!descriptor.isLookup()) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.InvalidLookupAssertion,
                    new Object[]{descriptor.getName()});
        }
        Object value = descriptor.getValue(object);
        if (value != null) {
            LookupAssertion assertion
                    = LookupAssertionFactory.create(descriptor, service);
            result = assertion.getName(object, (String) value);
        }
        return result;
    }

    /**
     * Helper method that returns a single lookup with the specified
     * archetype short name and code.
     *
     * @param service   the archetype sevice
     * @param shortName the archetype short name
     * @param code     the lookup code
     * @return the lookup, or <code>null</code> if none can be found
     * @throws ArchetypeServiceException if the request cannot complete
     */
    @SuppressWarnings("unchecked")
    public static Lookup getLookup(IArchetypeService service, String shortName,
                                   String code) {
        return getLookup(service, new String[]{shortName}, code);
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
    @SuppressWarnings("unchecked")
    public static Lookup getLookup(IArchetypeService service,
                                   String[] shortNames,
                                   String code) {
        Lookup lookup = null;

        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true)
                .add(new NodeConstraint("code", code))
                .setFirstRow(0)
                .setNumOfRows(1);
        IPage<IMObject> page = service.get(query);
        if (page.getTotalNumOfRows() > 0) {
            lookup = (Lookup) page.getRows().get(0);
        }

        // warn if there is more than one lookup with the same value
        if (page.getTotalNumOfRows() > 1) {
            logger.warn("There are " + page.getTotalNumOfRows() +
                    "lookups with shortNames: " + shortNames +
                    " and code: " + code);
        }

        return lookup;
    }

    /**
     * Helper method that returns a specific lookup given a short name and a
     * value
     *
     * @param service
     *            the archetype sevice
     * @param shortName
     *            the archetype short name
     * @param firstRow
     *            the first row to retriev
     * @param numOfRows
     *            the num of rows to retieve
     * @return IPage<Lookup>
     * @throws ArchetypeServiceException
     *            if the request cannot complete
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> getLookups(IArchetypeService service,
                                          String shortName,
                                          int firstRow, int numOfRows) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true)
            .setFirstRow(firstRow)
            .setNumOfRows(numOfRows);

        return new ArrayList<Lookup>((List)service.get(query).getRows());
    }

    /**
     * Helper method to return a list of target {@link Lookup} instances
     * give a reference source {@link Lookup}.
     * <p/>
     * Note this will work if the target lookups have a 'target' collection
     * node of lookup relationships, and each relationship has a 'source' node.
     *
     * @param service a reference to the archetype service
     * @param source  the source lookup
     * @param target  the archetype shortNames of the target
     * @return a list of lookup objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> getTargetLookups(IArchetypeService service,
                                                Lookup source,
                                                String[] target) {
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeShortNameConstraint(
                        target, false, false))
                .add(new CollectionNodeConstraint("target", false)
                        .add(new ObjectRefNodeConstraint("source",
                                                         source.getObjectReference())))
                .add(new NodeSortConstraint("name", true))
                .setActiveOnly(true);

        return new ArrayList<Lookup>((List) service.get(query).getRows());
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
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> getSourceLookups(IArchetypeService service,
                                                Lookup target,
                                                String[] source) {
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeShortNameConstraint(
                        source, false, false))
                .add(new CollectionNodeConstraint("source", false)
                        .add(new ObjectRefNodeConstraint("target",
                                                         target.getObjectReference())))
                .add(new NodeSortConstraint("name", true))
                .setActiveOnly(true);

        return new ArrayList<Lookup>((List) service.get(query).getRows());
    }

    /**
     *  Return the default lookup object for the specified archetype short name.
     *  It assumes that the archetype short name refers to a {@link Lookup}.
     *  <p>
     *  This method also assumes that the specified archetype short name has a
     *  node called 'defaultLookup' defined, which is of type boolean.
     *
     * @param service
     *            the archetype service
     * @param lookup
     *            the archetype short name of the lookup.
     * @return IMObject
     *            the object of null if one does not exist
     */
    public static Lookup getDefaultLookup(IArchetypeService service, String lookup) {
        IPage<IMObject> results = service.get(new ArchetypeQuery(lookup, false, false)
                    .add(new NodeConstraint("defaultLookup", RelationalOp.EQ, true))
                    .setNumOfRows(1));

        return (results.getRows().size() == 1) ? (Lookup)results.getRows().get(0) : null;

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
