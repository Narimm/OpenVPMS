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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.workspace.product.stock;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;


/**
 * Editor for <em>act.stockTransfer</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockTransferEditor extends ActEditor {

    /**
     * Construct a new <tt>StockTransferEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    public StockTransferEditor(Act act, IMObject parent,
                               LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        final ParticipationEditor from
            = (ParticipationEditor) getEditor("stockLocation");
        final ParticipationEditor to
            = (ParticipationEditor) getEditor("to");
        from.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                transferFromChanged((Party) from.getEntity());
            }
        });

        to.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                transferToChanged((Party) to.getEntity());
            }
        });
    }

    /**
     * Invoked when the 'transfer-from' location changes.
     *
     * @param location the location. May be <tt>null</tt>
     */
    private void transferFromChanged(Party location) {
        for (IMObjectEditor itemEditor : getItems().getCurrentEditors()) {
            StockTransferItemEditor editor = (StockTransferItemEditor) itemEditor;
            editor.setTransferFrom(location);
        }
    }

    /**
     * Invoked when the 'transfer-to' location changes.
     *
     * @param location the location. May be <tt>null</tt>
     */
    private void transferToChanged(Party location) {
        for (IMObjectEditor itemEditor : getItems().getCurrentEditors()) {
            StockTransferItemEditor editor = (StockTransferItemEditor) itemEditor;
            editor.setTransferTo(location);
        }
    }
}
