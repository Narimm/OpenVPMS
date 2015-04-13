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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.tools.archetype.comparator;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Tracks changes to an archetype descriptor.
 *
 * @author Tim Anderson
 */
public class ArchetypeChange extends DescriptorChange<ArchetypeDescriptor> {

    /**
     * The changed fields.
     */
    private final List<FieldChange> fieldChanges;

    /**
     * The changed nodes.
     */
    private final List<NodeChange> nodeChanges;

    /**
     * Constructs an {@link ArchetypeChange}.
     *
     * @param oldVersion the prior version of the archetype descriptor. May be {@code null}
     * @param newVersion the current version of the archetype descriptor. May be {@code null}
     */
    public ArchetypeChange(ArchetypeDescriptor oldVersion, ArchetypeDescriptor newVersion) {
        this(oldVersion, newVersion, Collections.<FieldChange>emptyList(), Collections.<NodeChange>emptyList());
    }

    /**
     * Constructs an {@link ArchetypeChange}.
     *
     * @param oldVersion   the prior version of the archetype descriptor. May be {@code null}
     * @param newVersion   the current version of the archetype descriptor. May be {@code null}
     * @param fieldChanges the field changes
     * @param nodeChanges  the node changes
     */
    public ArchetypeChange(ArchetypeDescriptor oldVersion, ArchetypeDescriptor newVersion,
                           List<FieldChange> fieldChanges, List<NodeChange> nodeChanges) {
        super(oldVersion, newVersion);
        this.fieldChanges = fieldChanges;
        this.nodeChanges = nodeChanges;
    }

    /**
     * Returns the archetype short name.
     *
     * @return the archetype short name
     */
    public String getShortName() {
        return getOldVersion() != null ? getOldVersion().getShortName() : getNewVersion().getShortName();
    }

    /**
     * Returns the field changes.
     *
     * @return the field changes
     */
    public List<FieldChange> getFieldChanges() {
        return fieldChanges;
    }

    /**
     * Returns the node changes.
     *
     * @return the node changes
     */
    public List<NodeChange> getNodeChanges() {
        return nodeChanges;
    }

    /**
     * Determines if the archetype has derived nodes that have been changed
     * since the prior version. Ony applicable if this is an update change.
     *
     * @return {@code true} if the update changed derived nodes
     */
    public boolean hasChangedDerivedNodes() {
        if (isUpdate()) {
            Map<String, NodeDescriptor> oldNodes = getDerivedNodes(getOldVersion());
            Map<String, NodeDescriptor> newNodes = getDerivedNodes(getNewVersion());
            if (oldNodes.size() != newNodes.size()) {
                return true;
            }
            for (NodeDescriptor oldNode : oldNodes.values()) {
                NodeDescriptor newNode = newNodes.get(oldNode.getName());
                if (newNode == null) {
                    return true;
                }
                if (!ObjectUtils.equals(oldNode.getPath(), newNode.getPath())
                    || !ObjectUtils.equals(oldNode.getDerivedValue(), newNode.getDerivedValue())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * Determines if the archetype has nodes that have assertions that have been added since the prior version.
     * Only applicable if this is an update change.
     *
     * @param assertions if specified, then only the named assertions are considered
     * @return {@code true} if the update changed node assertions
     */
    public boolean hasAddedAssertions(String... assertions) {
        if (isUpdate()) {
            for (NodeDescriptor oldNode : getOldVersion().getAllNodeDescriptors()) {
                String name = oldNode.getName();
                NodeDescriptor newNode = getNewVersion().getNodeDescriptor(name);
                if (newNode != null) {
                    if (hasAddedAssertions(oldNode, newNode, assertions)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the archetype has nodes that have assertions added since the prior
     * version. Only applicable if this is an update change.
     *
     * @param assertions if specified, then only the named assertions are considered
     * @return a list of nodes that have different assertions
     */
    public List<String> getNodesWithAddedAssertions(String... assertions) {
        List<String> result = new ArrayList<String>();
        if (isUpdate()) {
            for (NodeDescriptor oldNode : getOldVersion().getAllNodeDescriptors()) {
                String name = oldNode.getName();
                NodeDescriptor newNode = getNewVersion().getNodeDescriptor(name);
                if (newNode != null) {
                    if (hasAddedAssertions(oldNode, newNode, assertions)) {
                        result.add(name);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Determines if a node has added assertions.
     *
     * @param oldNode    the old version of the node descriptor
     * @param newNode    the new version of the node descriptor
     * @param assertions if specified, then only the named assertions are considered
     * @return {@code true} if the node has changed assertions; otherwise {@code false}
     */
    private boolean hasAddedAssertions(NodeDescriptor oldNode, NodeDescriptor newNode, String... assertions) {
        Map<String, AssertionDescriptor> oldAssertions = oldNode.getAssertionDescriptors();
        Map<String, AssertionDescriptor> newAssertions = newNode.getAssertionDescriptors();
        if (assertions.length == 0) {
            return !oldAssertions.keySet().equals(newAssertions.keySet());
        }
        for (String assertion : assertions) {
            boolean foundNew = newAssertions.containsKey(assertion);
            boolean foundOld = oldAssertions.containsKey(assertion);
            if (foundNew && !foundOld) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all of the derived nodes for an archetype.
     *
     * @param descriptor the archetype descriptor
     * @return the derived nodes, keyed on name
     */
    private Map<String, NodeDescriptor> getDerivedNodes(ArchetypeDescriptor descriptor) {
        Map<String, NodeDescriptor> result
                = new HashMap<String, NodeDescriptor>();
        for (NodeDescriptor node : descriptor.getNodeDescriptorsAsArray()) {
            if (node.isDerived()) {
                result.put(node.getName(), node);
            }
        }
        return result;
    }

}
