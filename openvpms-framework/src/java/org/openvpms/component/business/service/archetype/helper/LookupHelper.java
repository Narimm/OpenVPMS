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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This is a helper class for retrieving lookups reference data.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupHelper  {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(LookupHelper.class);

    /**
     * Return a list of lookups for the specified {@link NodeDescriptor} or
     * an empty list if not applicable
     *
     * @param service    a reference to the archetype service
     * @param descriptor the node descriptor
     * @return List<Lookup>
     * @throws LookupHelperException
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> get(IArchetypeService service,
                                   NodeDescriptor descriptor) {
        List<Lookup> lookups;
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor);
        if (assertion instanceof OneWayLookup
                || assertion instanceof LocalLookup) {
            lookups = assertion.getLookups(null, service);
        } else {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.InvalidLookupType,
                    new Object[]{assertion.getType()});
        }
        return lookups;
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
     * @param object     the object to use.
     * @return List<Lookup>
     * @throws LookupHelperException
     */
    @SuppressWarnings("unchecked")
    public static List<Lookup> get(IArchetypeService service,
                                   NodeDescriptor descriptor, IMObject object) {
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor);
        return assertion.getLookups(object, service);
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
        if (descriptor.isLookup()) {
            Object value = descriptor.getValue(object);
            if (value != null) {
                LookupAssertion assertion
                        = LookupAssertionFactory.create(descriptor);
                result = assertion.getLookup(object, service, (String) value);
            }
        }
        return result;
    }

    /**
     * Helper method that returns a single lookup with the specified
     * archetype short name and value
     *
     * @param service
     *            the archetype sevice
     * @param shortName
     *            the archetype short name
     * @param value
     *            the value of the node
     * @return IPage<Lookup>
     * @throws ArchetypeServiceException
     *            if the request cannot complete
     */
    @SuppressWarnings("unchecked")
    public static Lookup getLookup(IArchetypeService service, String shortName,
                                   String value) {
        Lookup lookup = null;

        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true)
                .add(new NodeConstraint("code", value))
            .setFirstRow(0)
            .setNumOfRows(1);
        IPage<IMObject> page = service.get(query);
        if (page.getTotalNumOfRows() > 0) {
            lookup = (Lookup)page.getRows().iterator().next();
        }

        // warn if there is more than one lookup with the same value
        if (page.getTotalNumOfRows() > 1) {
            logger.warn("There are " + page.getTotalNumOfRows() +
                    "lookups with shortName: " + shortName +
                    " and code: " + value);
        }

        return lookup;
    }

    /**
     * Helper method that returns a single lookup with the specified
     * archetype short names and value
     *
     * @param service
     *            the archetype sevice
     * @param shortNames
     *            the archetype short names to search on
     * @param value
     *            the value of the node
     * @return IPage<Lookup>
     * @throws ArchetypeServiceException
     *            if the request cannot complete
     */
    @SuppressWarnings("unchecked")
    public static Lookup getLookup(IArchetypeService service, String[] shortNames,
                                   String value) {
        Lookup lookup = null;

        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true)
                .add(new NodeConstraint("code", value))
            .setFirstRow(0)
            .setNumOfRows(1);
        IPage<IMObject> page = service.get(query);
        if (page.getTotalNumOfRows() > 0) {
            lookup = (Lookup)page.getRows().iterator().next();
        }

        // warn if there is more than one lookup with the same value
        if (page.getTotalNumOfRows() > 1) {
            logger.warn("There are " + page.getTotalNumOfRows() +
                    "lookups with shortNames: " + shortNames +
                    " and code: " + value);
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
    public static List<Lookup> getLookups(IArchetypeService service, String shortName,
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
     * Return a list of short names given the relationship archetype and a node
     * name If the node does not exist for the specified archetype then raise an
     * exception. In addition, if there are no archetype range assertions defined
     * for the node then also throw an exception.
     *
     * @param service
     *            the archetype service
     * @param relationship
     *            the relationship archetype
     * @param node
     *            the node name
     * @return String[]
     *            an archetype of short names
     * @throws LookupHelperException
     */
    private static String[] getArchetypeShortNames(IArchetypeService service,
                                                   String relationship, String node) {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(relationship);
        if (adesc == null) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.LookupRelationshipArchetypeNotDefined,
                    new Object[] {relationship});
        }
        NodeDescriptor ndesc = adesc.getNodeDescriptor(node);
        if (ndesc == null) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.InvalidLookupRelationshipArchetypeDefinition,
                    new Object[] {relationship, node});
        }

        String[] types = ndesc.getArchetypeRange();
        if (types.length == 0) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.NoArchetypeRangeInLookupRelationship,
                    new Object[] {relationship, node});
        }

        return types;
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
                value = getValue(assertion, "unspecified");
            }
        }

        return value;
    }

    private interface LookupAssertion {

        String getType();

        List<Lookup> getLookups(IMObject context, IArchetypeService service);

        Lookup getLookup(IMObject context, IArchetypeService service, String code);
    }

    private static class LookupAssertionFactory {

        public static LookupAssertion create(NodeDescriptor descriptor) {
            LookupAssertion result;
            if (descriptor.containsAssertionType("lookup")) {
                AssertionDescriptor assertion
                        = descriptor.getAssertionDescriptor("lookup");
                String type = getValue(assertion, "type");
                if (StringUtils.isEmpty(type)) {
                    throw new LookupHelperException(
                            LookupHelperException.ErrorCode.TypeNotSpecified,
                            new Object[]{assertion.getName()});
                }
                if (OneWayLookup.TYPE.equals(type)) {
                    result = new OneWayLookup(assertion);
                } else if (TargetLookup.TYPE.equals(type)) {
                    result = new TargetLookup(assertion);
                } else if (SourceLookup.TYPE.equals(type)) {
                    result = new SourceLookup(assertion);
                } else {
                    throw new LookupHelperException(
                            LookupHelperException.ErrorCode.InvalidLookupType,
                            new Object[]{type});
                }
            } else if (descriptor.containsAssertionType(LocalLookup.TYPE)) {
                AssertionDescriptor assertion
                        = descriptor.getAssertionDescriptor(LocalLookup.TYPE);
                result = new LocalLookup(assertion);
            } else
            if (descriptor.containsAssertionType(LookupAssertionType.TYPE)) {
                result = new LookupAssertionType();
            } else {
                throw new LookupHelperException(
                        LookupHelperException.ErrorCode.InvalidLookupAssertion,
                        new Object[]{descriptor.getName()});
            }
            return result;
        }
    }

    private static String getValue(AssertionDescriptor assertion, String name) {
        NamedProperty property = assertion.getProperty(name);
        return (property != null) ? (String) property.getValue() : null;

    }

    private static class OneWayLookup implements LookupAssertion {

        public static final String TYPE = "lookup";

        private String source;

        public OneWayLookup(AssertionDescriptor assertion) {
            source = getValue(assertion, "source");
            if (StringUtils.isEmpty(source)) {
                throw new LookupHelperException(
                        LookupHelperException.ErrorCode.SourceNotSpecified,
                        new Object[]{assertion.getName(), "lookup"});
            }
        }

        public String getType() {
            return TYPE;
        }

        public List<Lookup> getLookups(IMObject context,
                                       IArchetypeService service) {
            return LookupHelper.getLookups(service, source, 0, -1);
        }

        public Lookup getLookup(IMObject context, IArchetypeService service,
                                String code) {
            return LookupHelper.getLookup(service, source, code);
        }

    }

    private static class TargetLookup implements LookupAssertion {

        public static final String TYPE = "targetLookup";

        private String relationship;
        private String value;

        public TargetLookup(AssertionDescriptor assertion) {
            relationship = getValue(assertion, "relationship");
            value = getValue(assertion, "value");
            if (StringUtils.isEmpty(relationship)
                    || StringUtils.isEmpty(value)) {
                throw new LookupHelperException(
                        LookupHelperException.ErrorCode.InvalidTargetLookupSpec);
            }

        }

        public String getType() {
            return TYPE;
        }

        public List<Lookup> getLookups(IMObject context,
                                       IArchetypeService service) {
            List<Lookup> lookups;

            String srcVal = (String) JXPathContext.newContext(context).getValue(
                    value);
            String[] source = getArchetypeShortNames(service, relationship,
                                                     "source");
            String[] target = getArchetypeShortNames(service, relationship,
                                                     "target");

            Lookup lookup = LookupHelper.getLookup(service, source, srcVal);
            if (lookup != null) {
                lookups = getTargetLookups(service, lookup, target);
            }  else {
                lookups = Collections.emptyList();
            }
            return lookups;
        }

        public Lookup getLookup(IMObject context, IArchetypeService service,
                                String code) {
            Lookup result = null;

            String srcVal = (String) JXPathContext.newContext(context).getValue(
                    value);
            String[] source = getArchetypeShortNames(service, relationship,
                                                     "source");
            String[] target = getArchetypeShortNames(service, relationship,
                                                     "target");
            Lookup lookup = LookupHelper.getLookup(service, source, srcVal);
            if (lookup != null) {
                ArchetypeQuery query = new ArchetypeQuery(
                        new ArchetypeShortNameConstraint(
                                target, false, false))
                        .add(new CollectionNodeConstraint("target", false)
                                .add(new ObjectRefNodeConstraint("source",
                                                                 lookup.getObjectReference())))
                        .add(new NodeConstraint("code", code))
                        .setActiveOnly(true);
                List<IMObject> rows = service.get(query).getRows();
                if (!rows.isEmpty()) {
                   result = (Lookup) rows.get(0);
                }
            }
            return result;
        }
    }

    private static class SourceLookup implements LookupAssertion {

        public static final String TYPE = "sourceLookup";

        private final String relationship;
        private final String value;

        public SourceLookup(AssertionDescriptor assertion) {
            relationship = getValue(assertion, "relationship");
            value = getValue(assertion, "value");
            if (StringUtils.isEmpty(relationship)
                    || StringUtils.isEmpty(value)) {
                throw new LookupHelperException(
                        LookupHelperException.ErrorCode.InvalidSourceLookupSpec);
            }
        }

        public String getType() {
            return TYPE;
        }

        public List<Lookup> getLookups(IMObject context,
                                       IArchetypeService service) {
            List<Lookup> lookups = null;
            String tarVal = (String) JXPathContext.newContext(context).getValue(
                    value);
            String[] source = getArchetypeShortNames(service, relationship,
                                                     "source");
            String[] target = getArchetypeShortNames(service, relationship,
                                                     "target");

            Lookup lookup = LookupHelper.getLookup(service, target, tarVal);
            if (lookup != null) {
                lookups = LookupHelper.getSourceLookups(service, lookup,
                                                        source);
            }
            return lookups;
        }

        public Lookup getLookup(IMObject context, IArchetypeService service,
                                String code) {
            Lookup result = null;
            String tarVal = (String) JXPathContext.newContext(context).getValue(
                    value);
            String[] source = getArchetypeShortNames(service, relationship,
                                                     "source");
            String[] target = getArchetypeShortNames(service, relationship,
                                                     "target");

            Lookup lookup = LookupHelper.getLookup(service, target, tarVal);
            if (lookup != null) {
                ArchetypeQuery query = new ArchetypeQuery(
                        new ArchetypeShortNameConstraint(
                                source, false, false))
                        .add(new CollectionNodeConstraint("source", false)
                                .add(new ObjectRefNodeConstraint("target",
                                                                 lookup.getObjectReference())))
                        .add(new NodeConstraint("code", code))
                        .setActiveOnly(true);
                List<IMObject> rows = service.get(query).getRows();
                if (!rows.isEmpty()) {
                   result = (Lookup) rows.get(0);
                }
            }
            return result;
        }
    }

    private static class LocalLookup implements LookupAssertion {

        public static final String TYPE = "lookup.local";

        private List<Lookup> lookups = new ArrayList<Lookup>();

        public LocalLookup(AssertionDescriptor assertion) {
            PropertyList list = (PropertyList) assertion.getPropertyMap()
                    .getProperties().get("entries");
            for (NamedProperty prop : list.getProperties()) {
                AssertionProperty aprop = (AssertionProperty) prop;
                lookups.add(new Lookup(ArchetypeId.LocalLookupId,
                                       aprop.getName(), aprop.getValue()));
            }

        }

        public String getType() {
            return TYPE;
        }

        public List<Lookup> getLookups(IMObject context,
                                       IArchetypeService service) {
            return lookups;
        }

        public Lookup getLookup(IMObject context, IArchetypeService service,
                                String code) {
            for (Lookup lookup : lookups) {
                if (lookup.getCode().equals(code)) {
                    return lookup;
                }
            }
            return null;
        }
    }

    private static class LookupAssertionType implements LookupAssertion {

        public static final String TYPE = "lookup.assertionType";

        public String getType() {
            return TYPE;
        }

        public List<Lookup> getLookups(IMObject context,
                                       IArchetypeService service) {
            List<Lookup> lookups = new ArrayList<Lookup>();
            List<AssertionTypeDescriptor> descs =
                    service.getAssertionTypeDescriptors();
            for (AssertionTypeDescriptor adesc : descs) {
                lookups.add(new Lookup(ArchetypeId.LocalLookupId,
                                       adesc.getName(), adesc.getName()));
            }
            return lookups;
        }

        public Lookup getLookup(IMObject context, IArchetypeService service,
                                String code) {
            List<AssertionTypeDescriptor> descs =
                    service.getAssertionTypeDescriptors();
            for (AssertionTypeDescriptor adesc : descs) {
                if (adesc.getName().equals(code)) {
                    return new Lookup(ArchetypeId.LocalLookupId,
                                       adesc.getName(), adesc.getName());
                }
            }
            return null;
        }
    }
}
