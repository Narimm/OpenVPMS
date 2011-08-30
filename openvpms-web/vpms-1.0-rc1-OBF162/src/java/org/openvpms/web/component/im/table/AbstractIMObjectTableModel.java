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

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Abstract {@link IMObject} table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectTableModel<T extends IMObject>
        extends AbstractIMTableModel<T> implements IMObjectTableModel<T> {

    /**
     * Construct a new <code>AbstractIMObjectTableModel</code>.
     * The column model must be set using {@link #setTableColumnModel}.
     */
    public AbstractIMObjectTableModel() {
    }

    /**
     * Construct a new <code>AbstractIMObjectTableModel</code>, specifying
     * the column model. If null it must be set using using
     * {@link #setTableColumnModel}.
     *
     * @param model the table column model. May be <code>null</code>
     */
    public AbstractIMObjectTableModel(TableColumnModel model) {
        this.model = model;
    }

}
