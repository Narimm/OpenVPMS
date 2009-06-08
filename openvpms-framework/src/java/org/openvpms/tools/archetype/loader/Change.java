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
 *
 *  $Id$
 */

package org.openvpms.tools.archetype.loader;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Tracks changes to an archetype descriptor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Change {

    /**
     * The previous version.
     */
    private ArchetypeDescriptor oldVersion;

    /**
     * The current version.
     */
    private ArchetypeDescriptor newVersion;


    /**
     * Creates a new <tt>Change</tt>.
     */
    public Change() {
    }

    /**
     * Creates a new <tt>Change</tt>.
     *
     * @param newVersion the current version of the archetype descriptor.
     *                   May be <tt>null</tt>
     */
    public Change(ArchetypeDescriptor newVersion) {
        setNewVersion(newVersion);
    }

    /**
     * Creates a new <tt>Change</tt>.
     *
     * @param oldVersion the prior version of the archetype descriptor.
     *                   May be <tt>null</tt>
     * @param newVersion the current version of the archetype descriptor.
     *                   May be <tt>null</tt>
     */
    public Change(ArchetypeDescriptor oldVersion,
                  ArchetypeDescriptor newVersion) {
        setOldVersion(oldVersion);
        setNewVersion(newVersion);
    }

    /**
     * Returns the prior version of the archetype descriptor.
     *
     * @return the prior version of the archetype descriptor, or <tt>null</tt>
     *         if there was no prior version
     */
    public ArchetypeDescriptor getOldVersion() {
        return oldVersion;
    }

    /**
     * Sets the prior version of the archetype descriptor.
     *
     * @param oldVersion the prior version. May be <tt>null</tt>
     */
    public void setOldVersion(ArchetypeDescriptor oldVersion) {
        this.oldVersion = oldVersion;
    }

    /**
     * Returns the current version of the archetype descriptor.
     *
     * @return the current version of the archetype descriptor, or <tt>null</tt>
     *         if there is no current version
     */
    public ArchetypeDescriptor getNewVersion() {
        return newVersion;
    }

    /**
     * Sets the current version of the archetype descriptor.
     *
     * @param newVersion the current version. May be <tt>null</tt>
     */
    public void setNewVersion(ArchetypeDescriptor newVersion) {
        this.newVersion = newVersion;
    }

    /**
     * Determines if the change is an addition.
     *
     * @return <tt>true</tt> if the archetype has been added, <tt>false</tt> if
     *         it has been updated/deleted.
     */
    public boolean isAdd() {
        return oldVersion == null && newVersion != null;
    }

    /**
     * Determines if the change is a deletion.
     *
     * @return <tt>true</tt> if the archetype has been removed or <tt>false</tt>
     *         if it has been added/updated
     */
    public boolean isDelete() {
        return oldVersion != null && newVersion == null;
    }

    /**
     * Determines if the change is an update.
     *
     * @return <tt>true</tt> if the archetype has been updated or <tt>false</tt>
     *         if it has been added/removed.
     */
    public boolean isUpdate() {
        return oldVersion != null && newVersion != null;
    }

    /**
     * Determines if the archetype has derived nodes that have been changed
     * since the prior version. Ony applicable if this is an update change.
     *
     * @return <tt>true</tt> if the update changed derived nodes
     */
    public boolean hasChangedDerivedNodes() {
        if (isUpdate()) {
            Map<String, NodeDescriptor> oldNodes = getDerivedNodes(oldVersion);
            Map<String, NodeDescriptor> newNodes = getDerivedNodes(newVersion);
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
     * Determines if the archetype has nodes that have assertions that have been changed since the prior version.
     * Only applicable if this is an update change.
     *
     * @param assertions if specified, then only the named assertions are considered
     * @return <tt>true</tt> if the update changed node assertions
     */
    public boolean hasChangedAssertions(String... assertions) {
        if (isUpdate()) {
            for (NodeDescriptor oldNode : oldVersion.getAllNodeDescriptors()) {
                String name = oldNode.getName();
                NodeDescriptor newNode = newVersion.getNodeDescriptor(name);
                if (newNode != null) {
                    if (hasChangedAssertions(oldNode, newNode, assertions)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the archetype has nodes that have different assertions since the prior
     * version. Only applicable if this is an update change.
     *
     * @param assertions if specified, then only the named assertions are considered
     * @return a list of nodes that have different assertions
     */
    public List<String> getNodesWithChangedAssertions(String... assertions) {
        List<String> result = new ArrayList<String>();
        if (isUpdate()) {
            for (NodeDescriptor oldNode : oldVersion.getAllNodeDescriptors()) {
                String name = oldNode.getName();
                NodeDescriptor newNode = newVersion.getNodeDescriptor(name);
                if (newNode != null) {
                    if (hasChangedAssertions(oldNode, newNode, assertions)) {
                        result.add(name);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Determines if a node has changed assertions.
     *
     * @param oldNode    the old version of the node descriptor
     * @param newNode    the new version of the node descriptor
     * @param assertions if specified, then only the named assertions are considered
     * @return <tt>true</tt> if the node has changed assertions; otherwise <tt>false</tt>
     */
    private boolean hasChangedAssertions(NodeDescriptor oldNode, NodeDescriptor newNode, String... assertions) {
        Map<String, AssertionDescriptor> oldAssertions = oldNode.getAssertionDescriptors();
        Map<String, AssertionDescriptor> newAssertions = newNode.getAssertionDescriptors();
        if (assertions.length == 0) {
            return oldAssertions.keySet() != newAssertions.keySet();
        }
        for (String assertion : assertions) {
            boolean foundNew = newAssertions.containsKey(assertion);
            boolean foundOld = oldAssertions.containsKey(assertion);
            if ((foundOld && !foundNew) || (foundNew && !foundOld)) {
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
