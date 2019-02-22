/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.stock;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Editor for <em>act.stockTransfer</em> acts.
 * <p/>
 * NOTE: users may transfer from any stock location linked to one of their locations to any other location.
 * <p/>
 * This is deliberate; it means users don't need to be granted access to other locations that they are not
 * responsible for, in order to transfer stock.
 *
 *
 * @author Tim Anderson
 */
public class StockTransferEditor extends ActEditor {

    /**
     * From node name.
     */
    private static final String FROM = "stockLocation";

    /**
     * To node names.
     */
    private static final String TO = "to";

    /**
     * Constructs a {@link StockTransferEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context. May be {@code null}
     */
    public StockTransferEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        ParticipationEditor from = getParticipationEditor(FROM, false);
        ParticipationEditor to = getParticipationEditor(TO, false);
        from.addModifiableListener(modifiable -> transferFromChanged((Party) from.getEntity()));
        to.addModifiableListener(modifiable -> transferToChanged((Party) to.getEntity()));
    }

    /**
     * Validates the object.
     * <p>
     * This extends validation by ensuring that the start time is less than the end time, if non-null.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && checkLocations(validator);
    }

    /**
     * Verifies that the locations are different.
     *
     * @param validator the validator
     * @return {@code true} if the locations are different
     */
    private boolean checkLocations(Validator validator) {
        boolean result = false;
        Party from = (Party) getParticipant(FROM);
        Party to = (Party) getParticipant(TO);
        if (!ObjectUtils.equals(from, to)) {
            result = true;
        } else {
            validator.add(this, new ValidatorError(Messages.get("product.stock.same")));
        }
        return result;
    }

    /**
     * Invoked when the 'transfer-from' location changes.
     *
     * @param location the location. May be {@code null}
     */
    private void transferFromChanged(Party location) {
        for (IMObjectEditor itemEditor : getItems().getEditors()) {
            StockTransferItemEditor editor = (StockTransferItemEditor) itemEditor;
            editor.setTransferFrom(location);
        }
    }

    /**
     * Invoked when the 'transfer-to' location changes.
     *
     * @param location the location. May be {@code null}
     */
    private void transferToChanged(Party location) {
        for (IMObjectEditor itemEditor : getItems().getEditors()) {
            StockTransferItemEditor editor = (StockTransferItemEditor) itemEditor;
            editor.setTransferTo(location);
        }
    }
}
