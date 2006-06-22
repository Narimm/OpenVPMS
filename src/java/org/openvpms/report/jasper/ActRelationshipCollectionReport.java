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

package org.openvpms.report.jasper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;


/**
 * Default report generator for a collection of <code>ActRelationship</code>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActRelationshipCollectionReport
        extends AbstractIMObjectCollectionReport {

    /**
     * The node descriptors.
     */
    private LinkedHashMap<NodeDescriptor, String> _descriptors =
            new LinkedHashMap<NodeDescriptor, String>();


    /**
     * Construct a new <code>DefaultIMObjectCollectionReport</code>.
     *
     * @param descriptor the collection node descriptor
     * @param service    the archetype service
     */
    public ActRelationshipCollectionReport(NodeDescriptor descriptor,
                                           IArchetypeService service) {
        super(descriptor, service);
        ArchetypeDescriptor items = getArchetype();
        NodeDescriptor target = items.getNodeDescriptor("target");
        List<NodeDescriptor> nodes = null;
        for (ArchetypeDescriptor archetype : getArchetypes(target)) {
            List<NodeDescriptor> all = archetype.getAllNodeDescriptors();
            if (nodes == null) {
                nodes = all;
            } else {
                nodes = getIntersection(nodes, all);
            }
        }

        assert nodes != null;
        nodes = filter(nodes);

        for (NodeDescriptor node : nodes) {
            String field = "target." + node.getName();
            if (node.isCollection() && node.getMaxCardinality() == 1) {
                String[] shortNames = ReportHelper.getShortNames(
                        getArchetypeService(), node);
                if (ReportHelper.matches(shortNames, "participation.*")) {
                    field = "target." + node.getName() + ".entity.name";
                    node = getNodeDescriptor(shortNames[0], "entity");
                }
            }
            _descriptors.put(node, field);
        }
    }

    /**
     * Returns a node descriptor for the specified archetype.
     *
     * @param shortName the archetype short name
     * @param name the node name
     * @return the node descriptor or <code>null</code> if it doesn't exist
     */
    private NodeDescriptor getNodeDescriptor(String shortName, String name) {
        IArchetypeService service = getArchetypeService();
        ArchetypeDescriptor archetype
                = service.getArchetypeDescriptor(shortName);
        if (archetype != null) {
            return archetype.getNodeDescriptor(name);
        }
        return null;
    }

    /**
     * Returns the descriptors of the nodes to display.
     *
     * @return the descriptors of the nodes to display
     */
    protected NodeDescriptor[] getDescriptors() {
        return _descriptors.keySet().toArray(new NodeDescriptor[0]);
    }


    /**
     * Filters out hidden nodes.
     *
     * @param nodes the nodes to filter
     * @return the filtered nodes
     */
    protected List<NodeDescriptor> filter(List<NodeDescriptor> nodes) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        for (NodeDescriptor node : nodes) {
            if (!node.isHidden()) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Returns the node name to be used in a field expression.
     *
     * @param descriptor the node descriptor
     * @return the node name
     */
    protected String getFieldName(NodeDescriptor descriptor) {
        return _descriptors.get(descriptor);
    }

    /**
     * Helper to return the intersection of two lists of node descriptors.
     *
     * @param first  the first list of nodes
     * @param second the second list of nodes
     * @return the intersection of the two lists
     */
    private List<NodeDescriptor> getIntersection(
            List<NodeDescriptor> first, List<NodeDescriptor> second) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        for (NodeDescriptor a : first) {
            for (NodeDescriptor b : second) {
                if (a.getName().equals(b.getName())) {
                    result.add(a);
                    break;
                }
            }
        }
        return result;
    }

}
