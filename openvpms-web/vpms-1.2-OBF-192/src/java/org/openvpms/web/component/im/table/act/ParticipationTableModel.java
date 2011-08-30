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

package org.openvpms.web.component.im.table.act;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Table model for {@link Participation}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ParticipationTableModel extends BaseIMObjectTableModel<IMObject> {

    /**
     * Construct a new <code>ParticipationTableModel</code>.
     */
    public ParticipationTableModel() {
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     * @return the value at the specified coordinate
     */
    @Override
    protected Object getValue(IMObject object, TableColumn column, int row) {
        Participation p = (Participation) object;
        Entity entity = (Entity) IMObjectHelper.getObject(p.getEntity());
        Object result = null;
        if (entity != null) {
            result = super.getValue(entity, column, row);
        } else if (column.getModelIndex() == NAME_INDEX) {
            Label label = LabelFactory.create();
            label.setText(Messages.get("imobject.none"));
            result = label;
        }
        return result;
    }

}
