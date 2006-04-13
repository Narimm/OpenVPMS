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


/**
 * Abstract implementation of the {@link FocusGroup} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractFocusGroup implements FocusGroup {

    /**
     * The symbolic name for the group. May be <code>null</code>.
     */
    private final String _name;

    /**
     * The parent. May be <code>null</code>.
     */
    private FocusGroup _parent;

    /**
     * Dirty flag used to indicate that re-indexing is required.
     */
    private boolean _dirty = true;

    /**
     * Construct a new <code>AbstractFocusGroup</code>.
     *
     * @param name a symbolic name for the group
     */
    public AbstractFocusGroup(String name) {
        _name = name;
    }

    /**
     * Returns a symbolic name for the group.
     *
     * @return a symbolic name for the group. May be <code>null</code>
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the parent group.
     *
     * @param group the parent group. May be <code>null</code>
     */
    public void setParent(FocusGroup group) {
        _parent = group;
        setDirty(true);
    }

    /**
     * Returns the parent group.
     *
     * @return the parent group. May be <code>null</code>
     */
    public FocusGroup getParent() {
        return _parent;
    }

    /**
     * Determines if reindexing is required.
     *
     * @return <Code>true</code> if reindexing is required, otherwise
     *         <code>false</code>
     */
    public boolean isDirty() {
        return _dirty;
    }

    /**
     * Sets the dirty flag.
     *
     * @param dirty if <code>true</code>  indicates reindexing is required
     */
    public void setDirty(boolean dirty) {
        _dirty = dirty;
    }

    /**
     * Returns a stringified form of this.
     *
     * @return a stringified form of this
     */
    public String toString() {
        return "[" + getFirst() + "," + getLast() + "] - " + getName();
    }

    /**
     * Notify the parent that it needs to reindex.
     */
    protected void notifyParent() {
        if (_parent != null) {
            _parent.setDirty(true);
            _parent.reindex();
        }
    }

}
