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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.workflow;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.patient.visit.VisitEditor;
import org.openvpms.web.app.patient.visit.VisitEditorDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.i18n.Messages;


/**
 * Launches a browser to select and edit clinical events and their child acts.
 *
 * @author Tim Anderson
 */
public class EditVisitTask extends AbstractTask {

    /**
     * The dialog.
     */
    private VisitEditorDialog dialog;


    /**
     * Constructs an {@code EditClinicalEventTask} to edit an object in the {@link TaskContext}.
     */
    public EditVisitTask() {
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link org.openvpms.web.component.workflow.TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     * @throws org.openvpms.component.system.common.exception.OpenVPMSException
     *          for any error
     */
    public void start(TaskContext context) {
        Act event = (Act) context.getObject(PatientArchetypes.CLINICAL_EVENT);
        FinancialAct invoice = (FinancialAct) context.getObject(CustomerAccountArchetypes.INVOICE);
        if (event != null && invoice != null) {
            edit(event, invoice, context);
        } else {
            notifyCancelled();
        }
    }

    /**
     * Returns the visit dialog.
     *
     * @return the visit dialog, or {@code null} if none is being displayed.
     */
    public VisitEditorDialog getVisitDialog() {
        return dialog;
    }

    /**
     * Launches a {@link VisitEditorDialog} to select and edit an event.
     * <p/>
     * The supplied event is selected by default.
     *
     * @param event   the event
     * @param invoice the invoice
     * @param context the task context
     */
    protected void edit(Act event, FinancialAct invoice, TaskContext context) {
        ActBean bean = new ActBean(event);
        User clinician = (User) IMObjectHelper.getObject(bean.getNodeParticipantRef("clinician"), context);
        // If clinician is null then populate with current context clinician
        if (clinician == null && context.getClinician() != null) {
            bean.addNodeParticipation("clinician", context.getClinician());
            bean.save();
        }
        Party patient = (Party) IMObjectHelper.getObject(bean.getNodeParticipantRef("patient"), context);
        if (patient != null) {
            HelpContext help = context.getHelpContext().topic("visit");
            VisitEditor editor = createVisitEditor(event, invoice, patient, context, help);
            String title = Messages.get("workflow.visit.edit.title");
            dialog = new VisitEditorDialog(title, editor, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                protected void onAction(PopupDialog dialog) {
                    EditVisitTask.this.dialog = null;
                    super.onAction(dialog);
                }

                @Override
                public void onOK() {
                    notifyCompleted();
                }

                @Override
                public void onCancel() {
                    notifyCancelled();
                }
            });
            dialog.show();
        } else {
            notifyCancelled();
        }
    }

    /**
     * Creates a new visit editor.
     *
     * @param event   the event
     * @param invoice the invoice
     * @param patient the patient
     * @param context the task context
     * @param help    the help context
     * @return a new editor
     */
    protected VisitEditor createVisitEditor(Act event, FinancialAct invoice, Party patient, TaskContext context,
                                            HelpContext help) {
        return new VisitEditor(patient, event, invoice, context, help);
    }

}
