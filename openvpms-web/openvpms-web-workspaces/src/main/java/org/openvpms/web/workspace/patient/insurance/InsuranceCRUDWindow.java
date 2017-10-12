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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.insurance;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.insurance.internal.InsuranceArchetypes;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.workspace.AbstractViewCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;

/**
 * CRUD window for patient insurance.
 *
 * @author Tim Anderson
 */
public class InsuranceCRUDWindow extends AbstractViewCRUDWindow<Act> {

    /**
     * Claim button identifier.
     */
    private static final String CLAIM_ID = "button.claim";

    /**
     * Constructs an {@link InsuranceCRUDWindow}.
     *
     * @param context the context
     * @param help    the help context
     */
    public InsuranceCRUDWindow(Context context, HelpContext help) {
        super(Archetypes.create(InsuranceArchetypes.POLICY, Act.class), ActActions.edit(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(ButtonFactory.create(CLAIM_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onClaim();
            }
        }));
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(CLAIM_ID, enable);
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        if (TypeHelper.isA(editor.getObject(), InsuranceArchetypes.POLICY)) {
            super.onEditCompleted(editor, isNew);
        } else {
            onRefresh(getObject());
        }
    }

    /**
     * Invoked when the 'Claim' button is impressed.
     */
    private void onClaim() {
        Act act = (Act) IMObjectCreator.create(InsuranceArchetypes.CLAIM);
        if (act != null) {
            ActBean bean = new ActBean(act);
            Act policy = getObject();
            bean.addNodeRelationship("policy", policy);
            edit(act, null);
        }
    }
}
