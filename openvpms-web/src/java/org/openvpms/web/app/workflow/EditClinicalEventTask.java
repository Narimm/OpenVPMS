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

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.patient.mr.PatientSummaryQuery;
import org.openvpms.web.app.patient.mr.SummaryCRUDWindow;
import org.openvpms.web.app.patient.mr.SummaryTableBrowser;
import org.openvpms.web.app.subsystem.CRUDWindowListener;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.DoubleClickMonitor;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


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
        User clinician = (User) IMObjectHelper.getObject(bean.getNodeParticipantRef("clinician"));
        // If clinician is null then populate with current context clinician
        if (clinician == null && context.getClinician() != null) {
            bean.addNodeParticipation("clinician", context.getClinician());
            bean.save();
        }
        Party patient = (Party) IMObjectHelper.getObject(bean.getNodeParticipantRef("patient"));
        if (patient != null) {
            PatientSummaryQuery query = new PatientSummaryQuery(patient);
            query.setAllDates(true);
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
         * The CRUD window for editing events and their items.
         */
        private CRUDWindow window;

        /**
         * Edit button identifier.
         */
        private static final String EDIT_ID = "edit";

        /**
         * Delete button identifier.
         */
        private static final String DELETE_ID = "delete";

        /**
         * The dialog buttons.
         */
        private static final String[] BUTTONS = {NEW_ID, EDIT_ID, DELETE_ID, OK_ID, CANCEL_ID};


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
            window = new CRUDWindow(context);
            window.setQuery((PatientSummaryQuery) browser.getQuery());
            window.setListener(new CRUDWindowListener<Act>() {
                public void saved(Act object, boolean isNew) {
                    refreshBrowser(object);
                }

                public void deleted(Act object) {
                    refreshBrowser(null);
                }

                public void refresh(Act object) {
                    if (object.isNew()) {
                        // object not persistent, so don't attempt to reselect after refresh
                        refreshBrowser(null);
                    } else {
                        refreshBrowser(object);
                    }
                }
            });
            enableDelete();
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
            if (NEW_ID.equals(button)) {
                onCreate();
            } else if (EDIT_ID.equals(button)) {
                onEdit();
            } else if (DELETE_ID.equals(button)) {
                onDelete();
            } else {
                super.onButton(button);
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
         * Selects the current object. If the object is "double clicked", edits it.
         *
         * @param object the selected object
         */
        @Override
        protected void onSelected(Act object) {
            super.onSelected(object);
            long id = (object != null) ? object.getId() : 0;
            if (click.isDoubleClick(id)) {
                onEdit();
            }
            enableDelete();
        }

        /**
         * Invoked when the 'New' button is pressed.
         */
        private void onCreate() {
            Act event = getEvent();
            window.setEvent(event);
            window.create();
        }

        /**
         * Invoked when the 'Edit' button is pressed.
         * <p/>
         * Edits the selected act.
         */
        private void onEdit() {
            window.setObject(getSelected());
            window.setEvent(getEvent());
            window.edit();
        }

        /**
         * Invoked when the 'Delete' button is pressed.
         */
        private void onDelete() {
            window.setObject(getSelected());
            window.setEvent(getEvent());
            window.delete();
        }

        /**
         * Refresh the browser.
         *
         * @param object the object to select. May be <tt>null</tt>
         */
        private void refreshBrowser(Act object) {
            Browser<Act> browser = getBrowser();
            browser.query();
            browser.setSelected(object);
            onSelected(object);
        }

        /**
         * Returns the event associated with the selection.
         *
         * @return the event, or <tt>null</tt> if none is found
         */
        private Act getEvent() {
            Act act = getSelected();
            boolean found = false;
            if (act != null) {
                List<Act> acts = getBrowser().getObjects();
                int index = acts.indexOf(act);
                while (!(found = TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT)) && index > 0) {
                    act = acts.get(--index);
                }
            }
            return (found) ? act : null;
        }

        /**
         * Determines if the delete button should be enabled or disabled.
         * <p/>
         * This prevents deletion of visits.
         */
        private void enableDelete() {
            Act object = getSelected();
            boolean enableDelete = object != null && !TypeHelper.isA(object, PatientArchetypes.CLINICAL_EVENT);
            getButtons().setEnabled(DELETE_ID, enableDelete);
        }

    }

    /**
     * The CRUD window for editing events and their items.
     */
    private static class CRUDWindow extends SummaryCRUDWindow {

        /**
         * The task context.
         */
        private TaskContext taskContext;

        /**
         * Constructs a <tt>CRUDWindow</tt>.
         *
         * @param context the task context
         */
        public CRUDWindow(TaskContext context) {
            taskContext = context;
        }

        /**
         * Creates a layout context for editing an object.
         *
         * @return a new layout context.
         */
        @Override
        protected LayoutContext createLayoutContext() {
            LayoutContext context = super.createLayoutContext();
            context.setContext(taskContext);
            return context;
        }

    }

}
