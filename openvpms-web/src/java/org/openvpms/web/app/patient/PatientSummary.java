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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.table.PagedIMObjectTable;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * Renders Patient Summary Information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public class PatientSummary {

    /**
     * Returns summary information for a patient.
     *
     * @param patient the patient. May be <code>null</code>
     * @return a summary component, or <code>null</code> if there is no summary
     */
    public static Component getSummary(final Party patient) {
        Component result = null;
        if (patient != null) {
            Label alertTitle = LabelFactory.create("patient.alerts");
            int alerts = getRows(getAlerts(patient));
            Component alertCount;
            if (alerts == 0) {
                alertCount = LabelFactory.create("patient.noreminders");
            } else {
                Button button = ButtonFactory.create(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onShowAlerts(patient);
                    }
                });
                button.setText(Integer.toString(alerts));
                alertCount = button;
            }

            Label reminderTitle = LabelFactory.create("patient.reminders");
            int reminders = getRows(getReminders(patient));
            Component reminderCount;
            if (reminders == 0) {
                reminderCount = LabelFactory.create("patient.noreminders");
            } else {
                Button button = ButtonFactory.create(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onShowReminders(patient);
                    }
                });
                button.setText(Integer.toString(reminders));
                reminderCount = button;
            }
            result = GridFactory.create(2, alertTitle, alertCount,
                                        reminderTitle, reminderCount);
        }
        return result;
    }

    /**
     * Invoked to show alerts for a patient in a popup.
     *
     * @param patient the patient
     */
    private static void onShowAlerts(Party patient) {
        PagedIMObjectTable<Act> table = new PagedIMObjectTable<Act>(
                new AlertTableModel(), getAlerts(patient));
        new ViewerDialog(Messages.get("patient.summary.alerts"), table);
    }

    /**
     * Invoked to show reminders for a patient in a popup.
     *
     * @param patient the patient
     */
    private static void onShowReminders(Party patient) {
        PagedIMObjectTable<Act> table = new PagedIMObjectTable<Act>(
                new ReminderTableModel(), getReminders(patient));
        new ViewerDialog(Messages.get("patient.summary.reminders"), table);
    }

    /**
     * Helper to return the no. of rows in a result set.
     *
     * @param set the result set
     * @return the no. of rows in the set
     */
    private static int getRows(ActResultSet set) {
        IPage<Act> page = set.getPage(0);
        return (page != null) ? page.getTotalNumOfRows() : 0;
    }

    /**
     * Returns outstanding alerts for a patient.
     *
     * @param patient the patient
     * @return the set of outstanding alerts for the patient
     */
    private static ActResultSet getAlerts(Party patient) {
        String[] shortNames = {"act.patientAlert"};
        String[] statuses = {"In Progress"};
        BaseArchetypeConstraint archetypes = new ArchetypeShortNameConstraint(
                shortNames, true, true);
        ParticipantConstraint[] participants = {
                new ParticipantConstraint("patient", "participation.patient",
                                          patient)
        };
        OrConstraint time = new OrConstraint();
        time.add(new NodeConstraint("endTime", RelationalOp.GT, new Date()));
        time.add(new NodeConstraint("endTime", RelationalOp.IsNULL));

        return new ActResultSet(participants, archetypes, time, statuses,
                                false, null, 5, null);
    }

    /**
     * Returns outstanding reminders for a patient.
     *
     * @param patient the patient
     * @return the set of outstanding reminders for the patient
     */
    private static ActResultSet getReminders(Party patient) {
        String[] shortNames = {"act.patientReminder"};
        String[] statuses = {"In Progress"};
        BaseArchetypeConstraint archetypes = new ArchetypeShortNameConstraint(
                shortNames, true, true);
        ParticipantConstraint[] participants = {
                new ParticipantConstraint("patient", "participation.patient",
                                          patient)
        };
        return new ActResultSet(participants, archetypes, null, statuses,
                                false, null, 5, null);
    }

    /**
     * Helper to create a layout context where hyperlinks are disabled.
     *
     * @return a new layout context
     */
    private static LayoutContext createLayoutContext() {
        LayoutContext context = new DefaultLayoutContext();
        context.setEdit(true); // hack to disable hyerlinks
        TableComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        return context;
    }

    /**
     * Displays an {@link IMObjectViewer} in popup window.
     *
     * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
     * @version $LastChangedDate: 2006-04-11 04:09:07Z $
     */
    private static class ViewerDialog extends PopupDialog {

        /**
         * Construct a new <code>IMObjectViewerDialog</code>.
         *
         * @param table the table to display
         */
        public ViewerDialog(String title, PagedIMObjectTable<Act> table) {
            super(title, Buttons.OK);
            setModal(true);
            getLayout().add(table);
            show();
        }
    }

    private static class AlertTableModel extends AbstractActTableModel {

        /**
         * Creates a new <code>AlertTableModel</code>.
         */
        public AlertTableModel() {
            super(new String[]{"act.patientAlert"}, createLayoutContext());
        }

        /**
         * Returns a list of descriptor names to include in the table.
         *
         * @return the list of descriptor names to include in the table
         */
        @Override
        protected String[] getDescriptorNames() {
            return new String[]{"alertType", "reason"};
        }

    }

    private static class ReminderTableModel extends AbstractActTableModel {

        /**
         * Creates a new <code>AlertTableModel</code>.
         */
        public ReminderTableModel() {
            super(new String[]{"act.patientReminder"}, createLayoutContext());
        }

        /**
         * Returns a list of descriptor names to include in the table.
         *
         * @return the list of descriptor names to include in the table
         */
        @Override
        protected String[] getDescriptorNames() {
            return new String[]{"reminderType", "endTime"};
        }

    }

}
