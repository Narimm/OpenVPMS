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

package org.openvpms.web.component.im.list;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.util.Arrays;
import java.util.List;


/**
 * Archetype short name list model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ShortNameListModel extends AllNoneListModel {

    /**
     * The short names. The first column is the short name, the second the
     * corresponding display name.
     */
    private final String[][] shortNames;

    /**
     * Construct a new <tt>ShortNameListModel</tt>.
     *
     * @param shortNames the short names to populate the list with
     */
    public ShortNameListModel(String[] shortNames) {
        this(shortNames, false);
    }

    /**
     * Construct a new <tt>ShortNameListModel</tt>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <tt>true</tt>, add a localised "All"
     */
    public ShortNameListModel(String[] shortNames, boolean all) {
        this(shortNames, all, true);
    }

    /**
     * Construct a new <tt>LookupListModel</tt>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <tt>true</tt>, add a localised "All"
     */
    public ShortNameListModel(List<String> shortNames, boolean all) {
        this(shortNames.toArray(new String[0]), all, true);
    }

    /**
     * Construct a new <tt>ShortNameListModel</tt>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <tt>true</tt>, add a localised "All"
     * @param sort       if <tt>true</tt>, sort the list alphabetically
     */
    public ShortNameListModel(List<String> shortNames, boolean all,
                              boolean sort) {
        this(shortNames.toArray(new String[0]), all, sort);
    }

    /**
     * Construct a new <tt>ShortNameListModel</tt>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <tt>true</tt> add a localised "All"
     * @param sort       if <tt>true</tt>, sort the list alphabetically
     */
    public ShortNameListModel(String[] shortNames, boolean all, boolean sort) {
        this(shortNames, all, false, sort);
    }

    /**
     * Construct a new <tt>ShortNameListModel</tt>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <tt>true</tt> add a localised "All"
     * @param none       if <tt>true</tt>, add a localised "None"
     * @param sort       if <tt>true</tt>, sort the list alphabetically
     */
    public ShortNameListModel(String[] shortNames, boolean all, boolean none,
                              boolean sort) {
        if (sort) {
            Arrays.sort(shortNames);
        }
        int size = shortNames.length;
        int index = 0;
        if (all) {
            ++size;
        }
        if (none) {
            ++size;
        }
        this.shortNames = new String[size][2];
        if (all) {
            setAll(index++);
        }
        if (none) {
            setNone(index++);
        }
        for (int i = 0; i < shortNames.length; ++i, ++index) {
            String shortName = shortNames[i];
            this.shortNames[index][0] = shortName;
            String displayName = DescriptorHelper.getDisplayName(shortName);
            if (StringUtils.isEmpty(displayName)) {
                displayName = shortName;
            }
            this.shortNames[index][1] = displayName;
        }
    }

    /**
     * Returns the value at the specified index in the list.
     * This implementation returns the short name.
     *
     * @param index the index
     * @return the value, or <tt>null</tt> if the index represents 'All' or
     *         'None'
     */
    public Object get(int index) {
        return getShortName(index);
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    public int size() {
        return shortNames.length;
    }

    /**
     * Returns the short name at the specified index in the list.
     *
     * @param index the index
     * @return the short name or <tt>null</tt> if the index represents 'All' or
     *         'None'
     */
    public String getShortName(int index) {
        return shortNames[index][0];
    }

    /**
     * Returns the display name at the specified index in the list.
     *
     * @param index the index
     * @return the display name
     */
    public Object getDisplayName(int index) {
        return shortNames[index][1];
    }

    /**
     * Returns the index of the specified short name.
     *
     * @param shortName the short name
     * @return the index of <tt>shortName</tt>, or <tt>-1</tt> if it
     *         doesn't exist
     */
    public int indexOf(String shortName) {
        int result = -1;
        for (int i = 0; i < shortNames.length; ++i) {
            if (shortNames[i][0].equals(shortName)) {
                result = i;
                break;
            }
        }
        return result;
    }

}
