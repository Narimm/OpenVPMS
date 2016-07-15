/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.list;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


/**
 * Archetype short name list model.
 *
 * @author Tim Anderson
 */
public class ShortNameListModel extends AllNoneListModel {

    /**
     * The short names. This excludes All/None.
     */
    private final String[] shortNames;

    /**
     * The short names. The first column is the short name, the second the
     * corresponding display name.
     */
    private final String[][] shortNameMap;

    /**
     * Constructs a {@link ShortNameListModel}.
     *
     * @param shortNames the short names to populate the list with
     */
    public ShortNameListModel(String[] shortNames) {
        this(shortNames, false);
    }

    /**
     * Constructs a {@link ShortNameListModel}.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if {@code true}, add a localised "All"
     */
    public ShortNameListModel(String[] shortNames, boolean all) {
        this(shortNames, all, true);
    }

    /**
     * Constructs a {@link ShortNameListModel}.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if {@code true}, add a localised "All"
     */
    public ShortNameListModel(List<String> shortNames, boolean all) {
        this(shortNames, all, true);
    }

    /**
     * Constructs a {@link ShortNameListModel}.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if {@code true}, add a localised "All"
     * @param sort       if {@code true}, sort the list alphabetically
     */
    public ShortNameListModel(List<String> shortNames, boolean all, boolean sort) {
        this(shortNames, all, false, sort);
    }

    /**
     * Constructs a {@link ShortNameListModel}.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if {@code true}, add a localised "All"
     * @param none       if {@code true}, add a localised "None"
     * @param sort       if {@code true}, sort the list alphabetically
     */
    public ShortNameListModel(List<String> shortNames, boolean all, boolean none, boolean sort) {
        this(shortNames.toArray(new String[shortNames.size()]), all, none, sort);
    }

    /**
     * Constructs a {@link ShortNameListModel}.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if {@code true} add a localised "All"
     * @param sort       if {@code true}, sort the list alphabetically
     */
    public ShortNameListModel(String[] shortNames, boolean all, boolean sort) {
        this(shortNames, all, false, sort);
    }

    /**
     * Constructs a {@link ShortNameListModel}.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if {@code true}, add a localised "All"
     * @param none       if {@code true}, add a localised "None"
     * @param sort       if {@code true}, sort the list alphabetically on display name
     */
    public ShortNameListModel(String[] shortNames, boolean all, boolean none, boolean sort) {
        if (sort) {
            Arrays.sort(shortNames);
        }
        this.shortNames = shortNames;
        String[][] map = new String[shortNames.length][2];
        for (int i = 0; i < shortNames.length; ++i) {
            String shortName = shortNames[i];
            map[i][0] = shortName;
            String displayName = DescriptorHelper.getDisplayName(shortName);
            if (StringUtils.isEmpty(displayName)) {
                displayName = shortName;
            }
            map[i][1] = displayName;
        }
        if (sort) {
            // sort the map on display name
            Arrays.sort(map, new Comparator<String[]>() {
                public int compare(String[] o1, String[] o2) {
                    return o1[1].compareTo(o2[1]);
                }
            });
        }

        int size = shortNames.length;
        int index = 0;
        if (all) {
            ++size;
        }
        if (none) {
            ++size;
        }
        this.shortNameMap = new String[size][2];
        if (all) {
            setAll(index++);
        }
        if (none) {
            setNone(index++);
        }
        // copy the map, but leave room at the start for the All/None elements
        System.arraycopy(map, 0, this.shortNameMap, index, map.length);
    }

    /**
     * Returns the value at the specified index in the list.
     * This implementation returns the short name.
     *
     * @param index the index
     * @return the value, or {@code null} if the index represents 'All' or 'None'
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
        return shortNameMap.length;
    }

    /**
     * Returns the short name at the specified index in the list.
     *
     * @param index the index
     * @return the short name or {@code null} if the index represents 'All' or 'None'
     */
    public String getShortName(int index) {
        return shortNameMap[index][0];
    }

    /**
     * Returns the display name at the specified index in the list.
     *
     * @param index the index
     * @return the display name
     */
    public Object getDisplayName(int index) {
        return shortNameMap[index][1];
    }

    /**
     * Returns the available short names.
     * <p/>
     * This excludes any 'All' or 'None'.
     *
     * @return the short names
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * Returns the index of the specified short name.
     *
     * @param shortName the short name
     * @return the index of {@code shortName}, or {@code -1} if it doesn't exist
     */
    public int indexOf(String shortName) {
        int result = -1;
        for (int i = 0; i < shortNameMap.length; ++i) {
            if (shortNameMap[i][0].equals(shortName)) {
                result = i;
                break;
            }
        }
        return result;
    }

}
