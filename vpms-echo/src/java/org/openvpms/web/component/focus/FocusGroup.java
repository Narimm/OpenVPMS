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
 * Represents a sequential group of focus traversal indexes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface FocusGroup {

    /**
     * Returns a symbolic name for the group.
     *
     * @return a symbolic name for the group. May be <code>null</code>
     */
    String getName();

    /**
     * Sets the parent group.
     *
     * @param group the parent group. May be <code>null</code>
     */
    void setParent(FocusGroup group);

    /**
     * Returns the parent group.
     *
     * @return the parent group. May be <code>null</code>
     */
    FocusGroup getParent();

    /**
     * Returns the first focus traversal index.
     *
     * @return the first focus traversal index, or <code>-1</code> if there are
     *         no indexes
     */
    int getFirst();

    /**
     * Returns the last focus traversal index.
     *
     * @return the last focus traversal index, or <code>-1</code> if there are
     *         no indexes
     */
    int getLast();

    /**
     * Resets the focus traversal indexes.
     */
    void reindex();

    /**
     * Resets the focus traversal indexes.
     *
     * @param index the first index
     * @return the last index
     */
    int reindex(int index);

    /**
     * Determines if reindexing is required.
     *
     * @return <Code>true</code> if reindexing is required, otherwise
     *         <code>false</code>
     */
    boolean isDirty();

    /**
     * Sets the dirty flag.
     *
     * @param dirty if <code>true</code>  indicates reindexing is required
     */
    void setDirty(boolean dirty);
}
