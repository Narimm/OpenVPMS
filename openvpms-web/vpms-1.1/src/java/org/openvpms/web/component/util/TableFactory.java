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

package org.openvpms.web.component.util;

import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableModel;


/**
 * Factory for {@link Table}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class TableFactory extends ComponentFactory {

    /**
     * Create a new table.
     *
     * @param columns the initial column count
     * @param rows    the initial row count
     * @return a new table
     */
    public static Table create(int columns, int rows) {
        Table table = new Table(columns, rows);
        setDefaultStyle(table);
        return table;
    }

    /**
     * Create a new table.
     *
     * @param model the table model
     * @return a new table
     */
    public static Table create(TableModel model) {
        Table table = new Table(model);
        setDefaultStyle(table);
        return table;
    }

}
