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

package org.openvpms.web.workspace.workflow.checkin;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.AbstractBrowserListener;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.workflow.PrintActTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskListener;
import org.openvpms.web.component.workflow.Tasks;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;

import java.util.Collection;

import static org.openvpms.web.workspace.workflow.checkin.ScheduleDocumentTemplateQuery.hasTemplates;

/**
 * Task to optionally print <em>act.patientDocumentForm</em> and <em>act.patientDocumentLetter</em> for a patient.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPrintPatientDocumentsTask extends Tasks {

    /**
     * The print mode.
     */
    private final PrintIMObjectTask.PrintMode printMode;

    /**
     * The browser dialog.
     */
    private BrowserDialog<Entity> dialog;

    /**
     * Constructs an {@link AbstractPrintPatientDocumentsTask}.
     *
     * @param printMode the print mode
     * @param help      the help context
     */
    public AbstractPrintPatientDocumentsTask(PrintIMObjectTask.PrintMode printMode, HelpContext help) {
        super(help);
        this.printMode = printMode;
    }

    /**
     * Starts the task.
     * <p>
     * The registered {@link TaskListener} will be notified on completion or failure.
     *
     * @param context the task context
     */
    @Override
    public void start(final TaskContext context) {
        Entity schedule = getSchedule(context);
        Entity worklist = getWorkList(context);
        if (hasTemplates(schedule) || hasTemplates(worklist)) {
            Party patient = context.getPatient();
            if (patient == null) {
                throw new ContextException(ContextException.ErrorCode.NoPatient);
            }

            String title = Messages.get("workflow.print.title");
            HelpContext help = context.getHelpContext().subtopic("print");
            PatientDocumentTemplateBrowser browser = new PatientDocumentTemplateBrowser(
                    new ScheduleDocumentTemplateQuery(schedule, worklist), new DefaultLayoutContext(context, help));
            String[] buttons = canCancel() ? PopupDialog.OK_SKIP_CANCEL : PopupDialog.OK_SKIP;
            dialog = new BrowserDialog<>(title, buttons, browser, help);
            enableOK(browser);
            browser.addBrowserListener(new AbstractBrowserListener<Entity>() {
                @Override
                public void selected(Entity object) {
                    enableOK(browser);
                }

                /**
                 * Invoked when an object is browsed.
                 *
                 * @param object the browsed object
                 */
                @Override
                public void browsed(Entity object) {
                    enableOK(browser);
                }
            });
            dialog.setCloseOnSelection(false);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    print(browser.getSelections(), context);
                }

                @Override
                public void onSkip() {
                    notifySkipped();
                }

                @Override
                public void onCancel() {
                    notifyCancelled();
                }

                /**
                 * Invoked when an unknown button is selected.
                 *
                 * @param action the dialog action
                 */
                @Override
                public void onAction(String action) {
                    notifyCancelled();
                }

                @Override
                protected void onAction(PopupDialog dialog) {
                    try {
                        super.onAction(dialog);
                    } finally {
                        AbstractPrintPatientDocumentsTask.this.dialog = null;
                    }
                }

            });
            dialog.show();
        } else {
            // no templates associated with the schedule or worklist
            notifyCompleted();
        }
    }

    /**
     * Returns the browser dialog.
     *
     * @return the browser dialog, or {@code null} if none is being displayed
     */
    public BrowserDialog<Entity> getBrowserDialog() {
        return dialog;
    }

    /**
     * Determines if the task can be cancelled.
     *
     * @return {@code true}
     */
    protected boolean canCancel() {
        return true;
    }

    /**
     * Returns the schedule to use to locate templates.
     *
     * @param context the context
     * @return the schedule, or {@code null} if there is no schedule
     */
    protected abstract Entity getSchedule(TaskContext context);

    /**
     * Returns the work list to use to locate templates.
     *
     * @param context the context
     * @return the work list, or {@code null} if there is no work list
     */
    protected abstract Entity getWorkList(TaskContext context);

    /**
     * Creates a task to print a document.
     *
     * @param document    the document to print
     * @param mailContext the mail context
     * @param printMode   the print mode
     * @return a new task
     */
    protected PrintActTask createPrintTask(Act document, CustomerMailContext mailContext,
                                           PrintIMObjectTask.PrintMode printMode) {
        return new PrintPatientActTask(document, mailContext, printMode);
    }

    /**
     * Enables the OK button if the browser has selections.
     *
     * @param browser the browser
     */
    private void enableOK(PatientDocumentTemplateBrowser browser) {
        dialog.getButtons().setEnabled(PopupDialog.OK_ID, !browser.getSelections().isEmpty());
    }

    /**
     * Generate documents from a list of templates, and queue tasks to print them.
     *
     * @param templates the templates
     * @param context   the context
     */
    private void print(Collection<Entity> templates, TaskContext context) {
        CustomerMailContext mailContext = new CustomerMailContext(context, context.getHelpContext());
        for (Entity template : templates) {
            IMObjectBean templateBean = new IMObjectBean(template);
            Act document = (Act) ServiceHelper.getArchetypeService().create(templateBean.getString("archetype"));
            IMObjectBean bean = new IMObjectBean(document);
            bean.setTarget("patient", context.getPatient());
            bean.setTarget("documentTemplate", template);
            bean.setTarget("clinician", context.getClinician());
            addTask(createPrintTask(document, mailContext, printMode));
        }
        // now start the workflow to print the documents
        super.start(context);
    }


}
