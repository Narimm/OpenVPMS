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

package org.openvpms.web.component.im.list;

import nextapp.echo2.app.list.AbstractListModel;


/**
 * List model that optionally contains items for 'All' or 'None'.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AllNoneListModel extends AbstractListModel {


    /**
     * The index of 'All', or <tt>-1</tt> if it is not present.
     */
    private int allIndex = -1;

    /**
     * The index of 'None', or <tt>-1</tt> if it is not present.
     */
    private int noneIndex = -1;


    /**
     * Returns the index of 'All' in the list.
     *
     * @return the index of 'All', or <tt>-1</tt> if it isn't present.
     */
    public int getAllIndex() {
        return allIndex;
    }

    /**
     * Returns the index of 'None' in the list.
     *
     * @return the index of 'None', or <tt>-1</tt> if it isn't present.
     */
    public int getNoneIndex() {
        return noneIndex;
    }

    /**
     * Determines if the list contains 'All'.
     *
     * @return <tt>true</tt> if the list contains 'All'.
     */
    /**
     * Determines if the specified index indicates 'All'.
     *
     * @param index the index
     * @return <tt>true</tt> if the index indicates 'All'
     */
    public boolean isAll(int index) {
        return index == allIndex;
    }

    /**
     * Determines if the specified index indicates 'None'.
     *
     * @param index the index
     * @return <tt>true</tt> if the index indicates 'None'
     */
    public boolean isNone(int index) {
        return index == noneIndex;
    }

    /**
     * Sets the index of the 'All'.
     *
     * @param index the index of 'All', or <tt>-1</tt> if it is not present
     */
    protected void setAll(int index) {
        allIndex = index;
    }

    /**
     * Sets the index of the 'None'.
     *
     * @param index the index of 'None', or <tt>-1</tt> if it is not present
     */
    protected void setNone(int index) {
        noneIndex = index;
    }


}
