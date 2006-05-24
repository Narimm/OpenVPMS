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

package org.openvpms.web.component.focus;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
 * An hierarchy of {@link FocusGroup}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class FocusTree extends AbstractFocusGroup {

    /**
     * The list of focus groups.
     */
    private List<FocusGroup> _groups = new ArrayList<FocusGroup>();

    /**
     * The first index in the tree.
     */
    private int _first = -1;

    /**
     * Construct a new <code>FocusTree</code>.
     *
     * @param name a symbolic name for the tree
     */
    public FocusTree(String name) {
        super(name);
    }

    /**
     * Returns the first focus traversal index.
     *
     * @return the first focus traversal index, or <code>-1</code> if there are
     *         no indexes
     */
    public int getFirst() {
        return _first;
    }

    /**
     * Returns the last focus traversal index.
     *
     * @return the last focus traversal index, or <code>-1</code> if there are
     *         no indexes
     */
    public int getLast() {
        if (_groups.isEmpty()) {
            return _first;
        }
        return _groups.get(_groups.size() - 1).getLast();
    }

    /**
     * Resets the focus traversal indexes.
     */
    public void reindex() {
        int first = (_first <= 0) ? 1 : _first;
        int last = getLast();
        reindex(first);
        if (last != getLast()) {
            notifyParent();
        }
    }

    /**
     * Resets the focus traversal indexes.
     *
     * @param index the first index
     * @return the last index
     */
    public int reindex(int index) {
        if (_first != index || isDirty()) {
            _first = index;
            int next = index;
            for (FocusGroup group : _groups) {
                if (group.isDirty() || next > group.getFirst()) {
                    index = group.reindex(next);
                } else {
                    index = group.getLast();
                }
                next = index + 1;
            }
            setDirty(false);
        } else {
            index = getLast();
        }
        return index;
    }

    /**
     * Adds a new tab group.
     *
     * @param group the group to add
     */
    public void add(FocusGroup group) {
        add(_groups.size(), group);
    }

    /**
     * Insert a new focus group.
     *
     * @param index index at which the set is to be inserted
     * @param group the group to insert
     */
    public void add(int index, FocusGroup group) {
        _groups.add(index, group);
        group.setParent(this);
        setDirty(true);
        reindex();
    }

    /**
     * Returns the index of a focus group.
     *
     * @param group the group
     * @return the index of the group, or <code>-1</code> if it doesn't exist
     */
    public int indexOf(FocusGroup group) {
        return _groups.indexOf(group);
    }

    /**
     * Remove a focus group.
     *
     * @param group the group to remove
     */
    public void remove(FocusGroup group) {
        if (_groups.remove(group)) {
            group.setParent(null);
        }
    }

    /**
     * Returns the number of immediate children in the tree.
     *
     * @return the number of immediate children in the tree
     */
    public int size() {
        return _groups.size();
    }

    public void dump(PrintStream stream) {
        for (FocusGroup group : _groups) {
            dump(group, 0, stream);
        }
    }

    protected void dump(FocusGroup group, int depth, PrintStream stream) {
        for (int i = 0; i < depth; ++i) {
            stream.print(".");
        }
        System.out.println(group);
        if (group instanceof FocusTree) {
            for (FocusGroup child : ((FocusTree) group)._groups) {
                dump(child, depth + 1, stream);
            }
        }
    }

}
