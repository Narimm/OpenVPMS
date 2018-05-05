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

package org.openvpms.web.component.im.lookup;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Implementation of {@link LookupQuery} that sources lookups associated with an {@link IMObject IMObject} node.
 *
 * @author Tim Anderson
 */
public class NodeLookupQuery extends AbstractLookupQuery {

    /**
     * The archetype short name, or {@code null} if an object was specified
     */
    private String shortName;

    /**
     * The archetype node, or {@code null} if an object was specified.
     */
    private String node;

    /**
     * The object, or {@code null} if a short name was specified.
     */
    private IMObject object;

    /**
     * The node descriptor.
     */
    private NodeDescriptor descriptor;

    /**
     * The codes to include. If null or empty, indicates all codes.
     */
    private final String[] codes;

    /**
     * Constructs a {@link NodeLookupQuery} for an archetype and node.
     *
     * @param shortName the archetype short name
     * @param node      the node name
     */
    public NodeLookupQuery(String shortName, String node) {
        this(shortName, node, (String[]) null);
    }

    /**
     * Constructs a {@link NodeLookupQuery} for an archetype and node.
     *
     * @param shortName the archetype short name
     * @param node      the node name
     */
    public NodeLookupQuery(String shortName, String node, String... codes) {
        this.shortName = shortName;
        this.node = node;
        this.codes = codes;
    }

    /**
     * Constructs a {@link NodeLookupQuery for an object and property.
     *
     * @param object   the object
     * @param property the property
     */
    public NodeLookupQuery(IMObject object, Property property) {
        this(object, property.getDescriptor());
    }

    /**
     * Constructs a {@link NodeLookupQuery} for an object and node descriptor.
     *
     * @param object     the object
     * @param descriptor the node descriptor
     */
    public NodeLookupQuery(IMObject object, NodeDescriptor descriptor) {
        this.object = object;
        this.descriptor = descriptor;
        this.codes = null;
    }

    /**
     * Returns the lookups.
     *
     * @return the lookups
     */
    public List<Lookup> getLookups() {
        List<Lookup> result = Collections.emptyList();
        try {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            ILookupService lookupService = ServiceHelper.getLookupService();
            NodeDescriptor node = getDescriptor();
            if (node != null) {
                LookupAssertion assertion = LookupAssertionFactory.create(node, service, lookupService);
                List<Lookup> lookups;
                if (object != null) {
                    lookups = new ArrayList<>(assertion.getLookups(object));
                } else {
                    lookups = assertion.getLookups();
                }
                if (codes != null && codes.length > 0) {
                    result = new ArrayList<>();
                    for (String code : codes) {
                        Lookup lookup = getLookup(code, lookups);
                        if (lookup != null) {
                            result.add(lookup);
                        }
                    }
                } else {
                    result = lookups;
                }
            }
        } catch (OpenVPMSException error) {
            ErrorHelper.show(error);
        }
        sort(result);
        return result;
    }

    /**
     * Returns the default lookup.
     *
     * @return the default lookup, or {@code null} if none is defined
     */
    @Override
    public Lookup getDefault() {
        Lookup result = null;
        NodeDescriptor node = getDescriptor();
        if (node != null) {
            List<Lookup> lookups = getLookups();
            String code = node.getDefaultValue();
            if (code != null) {
                // defaultValue is an xpath expression. Rather than evaluating
                // it, just support the simple case of a quoted string.
                code = StringUtils.strip(code, "'");
                result = getLookup(code, lookups);
            }
            if (result == null) {
                result = getDefault(lookups);
            }
        }
        return result;
    }

    /**
     * Returns the node descriptor.
     *
     * @return the node descriptor, or {@code null} on error
     */
    private NodeDescriptor getDescriptor() {
        if (descriptor == null) {
            if (shortName != null) {
                IArchetypeService archetypeService = ArchetypeServiceHelper.getArchetypeService();
                ArchetypeDescriptor archetype = archetypeService.getArchetypeDescriptor(shortName);
                if (archetype != null) {
                    descriptor = archetype.getNodeDescriptor(node);
                }
            }
        }
        return descriptor;
    }
}
