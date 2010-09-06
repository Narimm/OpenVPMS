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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.workflow;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.patient.mr.PatientSummaryQuery;
import org.openvpms.web.app.patient.mr.SummaryTableBrowser;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.DoubleClickMonitor;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.resource.util.Messages;


/**
 * Launches a browser to select and edit clinical events and their child acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EditClinicalEventTask extends AbstractTask {

    /**
     * Constructs a <tt>EditClinicalEventTask</tt> to edit an object
     * in the {@link org.openvpms.web.component.workflow.TaskContext}.
     */
    public EditClinicalEventTask() {
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
        Act event = (Act) context.getObject(GetClinicalEventTask.EVENT_SHORTNAME);
        if (event != null) {
            edit(event, context);
        } else {
            notifyCancelled();
        }
    }

    /**
     * Launches a {@link ClinicalEventBrowserDialog} to select and edit an event.
     * <p/>
     * The supplied event is selected by default.
     *
     * @param event   the event
     * @param context the task context
     */
    protected void edit(Act event, TaskContext context) {
        ActBean bean = new ActBean(event);
        Party patient = (Party) IMObjectHelper.getObject(bean.getNodeParticipantRef("patient"));
        if (patient != null) {
            PatientSummaryQuery query = new PatientSummaryQuery(patient);
            query.setAllDates(false);
            query.setFrom(event.getActivityStartTime());
            query.setTo(DateRules.getDate(event.getActivityStartTime(), 1, DateUnits.DAYS));
            SummaryTableBrowser browser = new SummaryTableBrowser(query);
            browser.setSelected(event);
            String title = Messages.get("workflow.consult.selectrecord.title");
            BrowserDialog dialog = new ClinicalEventBrowserDialog(title, browser, context);
            dialog.addWindowPaneListener(new PopupDialogListener() {
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
     * Browser that displays clinical events and their child acts and supports
     * editing them.
     */
    private static class ClinicalEventBrowserDialog extends BrowserDialog<Act> {

        /**
         * Helper to monitor double clicks. When an act is double clicked, an edit dialog is displayed
         */
        private DoubleClickMonitor click = new DoubleClickMonitor();

        /**
         * The task context.
         */
        private final TaskContext context;

        /**
         * Edit button identifier.
         */
        private static final String EDIT_ID = "edit";

        /**
         * The dialog buttons.
         */
        private static final String[] BUTTONS = {EDIT_ID, OK_ID, CANCEL_ID};


        /**
         * Constructs a <tt>ClinicalEventBrowserDialog</tt>.
         *
         * @param title   the dialog title
         * @param browser the event browser
         * @param context the task context
         */
        public ClinicalEventBrowserDialog(String title, SummaryTableBrowser browser, TaskContext context) {
            super(title, BUTTONS, browser);
            setCloseOnSelection(false);
            this.context = context;
        }

        /**
         * Invoked when a button is pressed. This delegates to the appropriate
         * on*() method for the button if it is known, else sets the action to
         * the button identifier and closes the window.
         *
         * @param button the button identifier
         */
        @Override
        protected void onButton(String button) {
            if (EDIT_ID.equals(button)) {
                onEdit();
            } else {
                super.onButton(button);
            }
        }

        /**
         * Selects the current object. If the object is "double clicked", edits it.
         *
         * @param object the selected object
         */
        @Override
        protected void onSelected(Act object) {
            super.onSelected(object);
            if (object != null && click.isDoubleClick(object.getId())) {
                edit(object);
            }
        }

        /**
         * Invoked when the edit button is pressed.
         * <p/>
         * Edits the selected act.
         */
        private void onEdit() {
            Act selected = getSelected();
            if (selected != null) {
                edit(selected);
            }
        }

        /**
         * Returns the selected object.
         *
         * @return the selected object, or <tt>null</tt> if none was selected
         */
        @Override
        public Act getSelected() {
            Act selected = super.getSelected();
            if (selected == null) {
                // get the default from the browser
                selected = getBrowser().getSelected();
            }
            return selected;
        }

        /**
         * Edits an act.
         *
         * @param act the act to edit
         */
        private void edit(Act act) {
            try {
                LayoutContext layout = new DefaultLayoutContext(true);
                layout.setContext(context);
                IMObjectEditor editor = IMObjectEditorFactory.create(act, layout);
                EditDialog dialog = EditDialogFactory.create(editor);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    protected void onAction(PopupDialog dialog) {
                        super.onAction(dialog);
                        refresh();
                    }
                });
                dialog.show();
            } catch (Throwable exception) {
                ErrorHelper.show(exception);
            }
        }

        /**
         * Refresh the browser.
         */
        private void refresh() {
            getBrowser().query();
        }
    }

}
