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

package org.openvpms.web.app.financial.till;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.util.DateFormatter;

import java.util.Date;


/**
 * Displays acts associated with a till balance.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TillActTableModel extends ActAmountTableModel {

    /**
     * Construct a new <code>TillActTableModel</code>.
     */
    public TillActTableModel() {
        super(false, true, true);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object the object
     * @param column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int column, int row) {
        Object result = null;
        if (column == DATE_INDEX) {
            Act act = (Act) object;
            Date date = act.getActivityStartTime();
            if (date != null) {
                result = DateFormatter.formatDateTime(date, false);
            }
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }
}
