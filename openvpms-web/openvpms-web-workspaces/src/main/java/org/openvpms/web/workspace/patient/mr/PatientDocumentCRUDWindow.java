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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.workspace.DocumentActActions;
import org.openvpms.web.component.workspace.DocumentCRUDWindow;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.history.PatientHistoryActions;

/**
 * CRUD window for patient documents.
 *
 * @author Tim Anderson
 */
public class PatientDocumentCRUDWindow extends DocumentCRUDWindow {

    /**
     * Constructs a {@link PatientDocumentCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param help       the help context
     */
    public PatientDocumentCRUDWindow(Archetypes<DocumentAct> archetypes, Context context, HelpContext help) {
        super(archetypes, PatientDocumentActions.INSTANCE, context, help);
    }

    /**
     * Invoked when a new object has been created.
     * <p/>
     * This implementation displays a confirmation when creating an investigation, as these won't be charged.
     *
     * @param object the new object
     */
    @Override
    protected void onCreated(final DocumentAct object) {
        if (TypeHelper.isA(object, InvestigationArchetypes.PATIENT_INVESTIGATION)) {
            confirmCreation(object, "patient.record.create.investigation.title",
                            "patient.record.create.investigation.message", "newInvestigation");
        } else {
            super.onCreated(object);
        }
    }

    /**
     * Confirms creation of an object.
     *
     * @param object  the object
     * @param title   the dialog title key
     * @param message the dialog message key
     * @param help    the help key
     */
    private void confirmCreation(final DocumentAct object, String title, String message, String help) {
        ConfirmationDialog dialog = new ConfirmationDialog(Messages.get(title), Messages.get(message),
                                                           getHelpContext().subtopic(help));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                PatientDocumentCRUDWindow.super.onCreated(object);
            }
        });
        dialog.show();
    }

    private static class PatientDocumentActions extends DocumentActActions {

        public static final PatientDocumentActions INSTANCE = new PatientDocumentActions();

        /**
         * Determines if an act can be deleted.
         *
         * @param act the act to check
         * @return {@code true} if the act isn't locked
         */
        @Override
        public boolean canDelete(DocumentAct act) {
            return super.canDelete(act) && PatientHistoryActions.INSTANCE.canDelete(act);
        }

        /**
         * Determines if an act is locked from changes.
         *
         * @param act the act
         * @return {@code true} if the act status is {@link ActStatus#POSTED}
         */
        @Override
        public boolean isLocked(DocumentAct act) {
            return super.isLocked(act) || PatientHistoryActions.needsLock(act);
        }
    }
}
