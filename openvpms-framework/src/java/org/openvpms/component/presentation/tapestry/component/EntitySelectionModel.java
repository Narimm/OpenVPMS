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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.presentation.tapestry.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry.form.IPropertySelectionModel;

/**
 * This class is used as a property selection model to select a primary key. We
 * assume that the primary keys are integers, which makes it easy to translate
 * between the various representations.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntitySelectionModel implements IPropertySelectionModel {
    private static class Entry {
        Integer primaryKey;

        String label;

        Entry(Integer primaryKey, String label) {
            this.primaryKey = primaryKey;
            this.label = label;
        }
    }

    private List entries = new ArrayList();

    /**
     * @param key
     * @param label
     */
    public void add(Integer key, String label) {
        Entry entry;

        entry = new Entry(key, label);
        entries.add(entry);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#getOptionCount()
     */
    public int getOptionCount() {
        return entries.size();
    }

    /**
     * @param index
     * @return
     */
    private Entry get(int index) {
        return (Entry) entries.get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#getOption(int)
     */
    public Object getOption(int index) {
        return get(index).primaryKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#getLabel(int)
     */
    public String getLabel(int index) {
        return get(index).label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#getValue(int)
     */
    public String getValue(int index) {
        Integer primaryKey;

        primaryKey = get(index).primaryKey;

        if (primaryKey == null)
            return "";

        return primaryKey.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#translateValue(java.lang.String)
     */
    public Object translateValue(String value) {
        if (value.equals(""))
            return null;

        try {
            return new Integer(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Could not convert '" + value
                    + "' to an Integer.", e);
        }
    }
}