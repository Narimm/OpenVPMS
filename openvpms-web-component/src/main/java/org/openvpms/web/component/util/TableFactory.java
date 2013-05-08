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
import org.openvpms.web.echo.table.KeyTable;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.echo.style.Styles;


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
     * @param model the table model
     * @return a new table
     */
    public static Table create(TableModel model) {
        return create(model, Styles.DEFAULT);
    }

    /**
     * Create a new table.
     *
     * @param model     the table model
     * @param styleName the table style name
     * @return a new table
     */
    public static Table create(TableModel model, String styleName) {
        Table table = new KeyTable();
        table.setModel(model);
        table.setStyleName(styleName);
        return table;
    }

}
