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

import java.math.BigDecimal;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.table.act.ActAmountTableModel;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.NumberFormatter;


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
        super(false, true);
    }
}
