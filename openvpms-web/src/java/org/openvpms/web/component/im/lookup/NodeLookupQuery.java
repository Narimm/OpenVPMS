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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.lookup;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.component.util.ErrorHelper;

import java.util.Collections;
import java.util.List;


/**
 * Implementation of {@link LookupQuery} that sources lookups associated
 * with an {@link IMObject IMObject} node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeLookupQuery extends AbstractLookupQuery {

    /**
     * The archetype short name, or <tt>null</tt> if an object was specified
     */
    private String shortName;

    /**
     * The archetype node, or <tt>null</tt> if an object was specified.
     */
    private String node;

    /**
     * The object, or <tt>null</tt> if a short name was specified.
     */
    private IMObject object;

    /**
     * The node descriptor, or <tt<null</tt> if a short name was specified.
     */
    private NodeDescriptor descriptor;


    /**
     * Creates a new <tt>NodeLookupQuery</tt> for an archetype and node.
     *
     * @param shortName the archetype short name
     * @param node      the node name
     */
    public NodeLookupQuery(String shortName, String node) {
        this.shortName = shortName;
        this.node = node;

    }

    /**
     * Creates a new <tt>NodeLookupQuery</tt> for an object and node
     * descriptor.
     *
     * @param object     the object
     * @param descriptor the node descriptor
     */
    public NodeLookupQuery(IMObject object, NodeDescriptor descriptor) {
        this.object = object;
        this.descriptor = descriptor;
    }

    /**
     * Returns the lookups.
     *
     * @return the lookups
     */
    public List<Lookup> getLookups() {
        try {
            return (shortName != null)
                    ? FastLookupHelper.getLookups(shortName, node)
                    : FastLookupHelper.getLookups(descriptor, object);
        } catch (OpenVPMSException error) {
            ErrorHelper.show(error);
            return Collections.emptyList();
        }
    }

    /**
     * Returns the default lookup.
     *
     * @return the default lookup, or <tt>null</tt> if none is defined
     */
    @Override
    public Lookup getDefault() {
        Lookup result = null;
        NodeDescriptor nodeDesc = getDescriptor();
        if (nodeDesc != null) {
            List<Lookup> lookups = getLookups();
            String code = nodeDesc.getDefaultValue();
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
     * @return the node descriptor, or <tt>null</tt> on error
     */
    private NodeDescriptor getDescriptor() {
        NodeDescriptor result = null;
        try {
            if (shortName != null) {
                ArchetypeDescriptor archetype
                        = DescriptorHelper.getArchetypeDescriptor(shortName);
                if (archetype != null) {
                    result = archetype.getNodeDescriptor(node);
                }
            } else {
                result = descriptor;
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return result;
    }
}
