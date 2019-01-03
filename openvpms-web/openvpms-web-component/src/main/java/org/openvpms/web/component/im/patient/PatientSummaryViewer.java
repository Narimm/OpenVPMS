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

package org.openvpms.web.component.im.patient;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.model.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;

import java.util.function.Supplier;

/**
 * Views a patient summary in a dialog.
 *
 * @author Tim Anderson
 */
public class PatientSummaryViewer {

    /**
     * Helper to create a button to launch the summary viewer.
     * <p/>
     * This has focus traversal disabled.
     *
     * @param patient a function to return the patient
     * @return a new button
     */
    public static Button createButton(LayoutContext context, Supplier<Party> patient) {
        Button button = ButtonFactory.create(null, "button.info", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                Party entity = patient.get();
                if (entity != null) {
                    show(entity, context);
                }
            }
        });
        button.setFocusTraversalParticipant(false);
        return button;

    }

    /**
     * Shows the summary for a patient.
     *
     * @param patient the patient
     * @param context the context
     */
    public static void show(Party patient, LayoutContext context) {
        PatientSummaryFactory factory = ServiceHelper.getBean(PatientSummaryFactory.class);
        Component summary = factory.getSummary(patient, context);
        PopupDialog dialog = new ModalDialog("Information for " + patient.getName(), "SummaryDialog",
                                             PopupDialog.CLOSE) {
            @Override
            protected void doLayout() {
                getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, summary));
            }
        };
        dialog.show();
    }

}
