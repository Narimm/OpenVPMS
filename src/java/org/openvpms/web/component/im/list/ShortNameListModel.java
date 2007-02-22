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

import nextapp.echo2.app.list.AbstractListModel;
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
public class ShortNameListModel extends AbstractListModel {

    /**
     * Short name indicating that all values apply.
     */
    public static final String ALL = "all";

    /**
     * The short names. The first column is the short name, the second the
     * corresponding display name.
     */
    private final String[][] shortNames;

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     */
    public ShortNameListModel(String[] shortNames) {
        this(shortNames, false);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code>, add a localised "All"
     */
    public ShortNameListModel(String[] shortNames, boolean all) {
        this(shortNames, all, true);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code>, add a localised "All"
     */
    public ShortNameListModel(List<String> shortNames, boolean all) {
        this(shortNames.toArray(new String[0]), all, true);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code>, add a localised "All"
     * @param sort       if <code>true</code>, sort the list alphabetically
     */
    public ShortNameListModel(List<String> shortNames, boolean all,
                              boolean sort) {
        this(shortNames.toArray(new String[0]), all, sort);
    }

    /**
     * Construct a new <code>LookupListModel</code>.
     *
     * @param shortNames the short names to populate the list with
     * @param all        if <code>true</code> add a localised "All"
     * @param sort       if <code>true</code>, sort the list alphabetically
     */
    public ShortNameListModel(String[] shortNames, boolean all, boolean sort) {
        if (sort) {
            Arrays.sort(shortNames);
        }
        int size = shortNames.length;
        int index = 0;
        if (all) {
            ++size;
        }
        this.shortNames = new String[size][2];
        if (all) {
            this.shortNames[index][0] = ALL;
            ++index;
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
     * @return the value
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
     * @return the short name
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
     * @return the index of <code>shortName</code>, or <code>-1</code> if it
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
