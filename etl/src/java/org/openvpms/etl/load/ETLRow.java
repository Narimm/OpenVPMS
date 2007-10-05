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

package org.openvpms.etl.load;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents a database row from a legacy system.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLRow {

    /**
     * The legacy row identifier.
     */
    private final String rowId;

    /**
     * The values, keyed on name.
     */
    private final Map<String, Object> values = new HashMap<String, Object>();


    /**
     * Constructs a new <tt>ETLRow</tt>.
     *
     * @param rowId the legacy row identifier
     */
    public ETLRow(String rowId) {
        this.rowId = rowId;
    }

    /**
     * Add a column value.
     *
     * @param name  the column name
     * @param value the column value
     */
    public void add(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Returns the legacy row identifier.
     *
     * @return the legacy row identifier
     */
    public String getRowId() {
        return rowId;
    }

    /**
     * Returns the value for the named column.
     *
     * @param name the column name
     * @return the column value. May be <tt>null</tt>
     */
    public Object get(String name) {
        return values.get(name);
    }

    /**
     * Determines if a column exists.
     *
     * @param name the column name
     * @return <tt>true</tt> if the column exists, otherwise <tt>false</tt>
     */
    public boolean exists(String name) {
        return values.containsKey(name);
    }
}
