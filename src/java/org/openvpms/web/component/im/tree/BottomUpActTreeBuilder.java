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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.tree;

import org.openvpms.web.component.im.util.IMObjectHelper;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.SortConstraint;

import echopointng.tree.DefaultMutableTreeNode;
import echopointng.tree.MutableTreeNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Tree builder for acts. The supplied acts are treated as leaf nodes,
 * with the tree being built bottom up.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-19 07:20:38Z $
 */
public class BottomUpActTreeBuilder extends AbstractTreeBuilder<Act> {

    /**
     * Parent act short names to exclude when building the tree.
     */
    private final Set<String> _parentExcludes;

    /**
     * The set of known acts, and their corresponding nodes.
     */
    private Map<IMObjectReference, IMObjectTreeNode> _acts;

    /**
     * Create a new <code>BottomUpActTreeBuilder</code>.
     */
    public BottomUpActTreeBuilder() {
        this(null);
    }

    /**
     * Create a new <code>BottomUpActTreeBuilder</code>.
     *
     * @param parentExcludes parent act short names to exclude when building the
     *                       tree. May be <code>null</code>
     */
    public BottomUpActTreeBuilder(String[] parentExcludes) {
        if (parentExcludes != null && parentExcludes.length != 0) {
            _parentExcludes = new HashSet<String>(
                    Arrays.asList(parentExcludes));
        } else {
            _parentExcludes = null;
        }
    }

    /**
     * Start a new tree.
     *
     * @param sort node sort criteria. May be <code>null</code>
     */
    @Override
    public void create(SortConstraint[] sort) {
        super.create(sort);
        _acts = new HashMap<IMObjectReference, IMObjectTreeNode>();
    }

    /**
     * Adds a node into the tree.
     *
     * @param object the object to add
     */
    public void add(Act object) {
        addBottomUp(object, getRoot(), true);
    }

    /**
     * Returns the created tree.
     *
     * @return the created tree
     */
    @Override
    public MutableTreeNode getTree() {
        _acts.clear();
        return super.getTree();
    }

    /**
     * Adds a leaf node, building up the parents if they don't exist.
     *
     * @param act  the act
     * @param root the root node
     * @param leaf if <code>true</code> create a leaf node
     * @return the new node
     */
    protected IMObjectTreeNode addBottomUp(Act act,
                                           DefaultMutableTreeNode root,
                                           boolean leaf) {
        DefaultMutableTreeNode parentNode = null;
        Set<ActRelationship> relationships = act.getTargetActRelationships();
        if (!relationships.isEmpty()) {
            ActRelationship[] acts = relationships.toArray(
                    new ActRelationship[0]);
            for (ActRelationship relationship : acts) {
                IMObjectReference source = relationship.getSource();
                if (!excluded(source)) {
                    parentNode = _acts.get(source);
                    if (parentNode != null) {
                        break;
                    } else {
                        Act parent = (Act) IMObjectHelper.getObject(source);
                        if (parent == null) {
                            parentNode = root;
                        } else {
                            parentNode = addBottomUp(parent, root, false);
                        }
                        if (parentNode != null) {
                            break;
                        }
                    }
                }
            }
        }
        if (parentNode == null) {
            parentNode = root;
        }
        IMObjectTreeNode<Act> child = create(act, leaf);
        parentNode.setAllowsChildren(true);
        parentNode.add(child);
        _acts.put(act.getObjectReference(), child);
        return child;
    }

    /**
     * Determines if an act has been excluded.
     *
     * @param act the act reference
     * @return <code>true</code> if the act is excluded; otherwise
     *         <code>false</code>
     */
    private boolean excluded(IMObjectReference act) {
        if (_parentExcludes != null) {
            ArchetypeId id = act.getArchetypeId();
            return _parentExcludes.contains(id.getShortName());
        }
        return false;
    }

}
