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

package org.openvpms.component.business.service.archetype.helper.lookup;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;
import org.openvpms.component.business.service.archetype.query.NodeSet;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Lookup assertion denoting the target in a lookup relationship.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class TargetLookup extends AbstractLookupAssertion {

    /**
     * The lookup type.
     */
    public static final String TYPE = "targetLookup";

    /**
     * The lookup relationship short name.
     */
    private final String relationship;

    /**
     * The jxpath to the lookup code.
     */
    private final String value;


    /**
     * Constructs a new <code>TargetLookup</code>.
     *
     * @param descriptor the assertion descriptor
     * @param service    the archetype service
     */
    public TargetLookup(AssertionDescriptor descriptor,
                        IArchetypeService service) {
        super(descriptor, TYPE, service);
        relationship = getProperty("relationship");
        value = getProperty("value");
        if (StringUtils.isEmpty(relationship) || StringUtils.isEmpty(value)) {
            throw new LookupHelperException(
                    LookupHelperException.ErrorCode.InvalidTargetLookupSpec);
        }
    }

    /**
     * Returns the lookups for this assertion.
     *
     * @param context the context
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public List<Lookup> getLookups(IMObject context) {
        List<Lookup> lookups;
        Lookup lookup = getSourceLookup(context);
        if (lookup != null) {
            String[] target = getArchetypeShortNames(relationship, "target");
            lookups = LookupHelper.getTargetLookups(
                    getArchetypeService(), lookup, target);
        } else {
            lookups = Collections.emptyList();
        }
        return lookups;
    }

    /**
     * Returns partially populated lookups for this assertion.
     *
     * @param context the context.
     * @param nodes   the nodes to populate
     * @return a list of lookups
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public List<Lookup> getLookups(IMObject context, Collection<String> nodes) {
        List<Lookup> result;
        IMObjectReference lookup = getSourceLookupRef(context);
        if (lookup != null) {
            String[] target = getArchetypeShortNames(relationship, "target");
            ArchetypeQuery query = new ArchetypeQuery(
                    new ArchetypeShortNameConstraint(
                            target, false, false))
                    .add(new CollectionNodeConstraint("target", false)
                            .add(new ObjectRefNodeConstraint("source", lookup)))
                    .add(new NodeSortConstraint("name", true))
                    .setActiveOnly(true);
            result = getLookups(nodes, query);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns the lookup with the specified code.
     *
     * @param context the context
     * @return the lookup matching <code>code</code>, or <code>null</code> if
     *         none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Lookup getLookup(IMObject context, String code) {
        Lookup result = null;

        IMObjectReference lookup = getSourceLookupRef(context);
        if (lookup != null) {
            ArchetypeQuery query = createQuery(lookup, code);
            List<Lookup> lookups = getLookups(query);
            if (!lookups.isEmpty()) {
                result = lookups.get(0);
            }
        }
        return result;
    }

    /**
     * Returns the name of the lookup with the specified code.
     *
     * @param context the context. May be <code>null</code>
     * @return the name of the lookup matching <code>code</code>, or
     *         <code>null</code> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public String getName(IMObject context, String code) {
        String result = null;
        IMObjectReference lookup = getSourceLookupRef(context);
        if (lookup != null) {
            ArchetypeQuery query = createQuery(lookup, code);
            List<String> nodes = Arrays.asList("name");
            IArchetypeService service = getArchetypeService();
            List<NodeSet> rows = service.getNodes(nodes, query).getRows();
            if (!rows.isEmpty()) {
                result = (String) rows.get(0).get("name");
            }
        }
        return result;
    }

    /**
     * Creates a query.
     *
     * @param lookup the source lookup
     * @param code   the lookup code
     * @return a new query
     */
    private ArchetypeQuery createQuery(IMObjectReference lookup, String code) {
        String[] target = getArchetypeShortNames(relationship, "target");
        return new ArchetypeQuery(
                new ArchetypeShortNameConstraint(
                        target, false, false))
                .add(new CollectionNodeConstraint("target", false)
                        .add(new ObjectRefNodeConstraint("source", lookup)))
                .add(new NodeConstraint("code", code))
                .setActiveOnly(true);
    }

    /**
     * Returns the source lookup.
     *
     * @param context the context
     * @return the source lookup, or <code>null</code> if none can be found
     */
    private Lookup getSourceLookup(IMObject context) {
        return getLookup(context, value, relationship, "source");
    }

    /**
     * Returns a reference to the target lookup.
     *
     * @param context the context object
     * @return a reference to the target lookup or <code>null</code> if it
     *         can't be found
     */
    private IMObjectReference getSourceLookupRef(IMObject context) {
        String srcVal = getPathValue(context, value);
        String[] source = getArchetypeShortNames(relationship, "source");
        List<String> names = Collections.emptyList();
        NodeSet nodes = getLookupNodes(source, srcVal, names);
        return (nodes != null) ? nodes.getObjectReference() : null;
    }
}
