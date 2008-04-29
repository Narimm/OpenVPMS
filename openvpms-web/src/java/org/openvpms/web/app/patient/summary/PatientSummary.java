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

package org.openvpms.web.app.patient.summary;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
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
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The reminder rules.
     */
    private final ReminderRules reminderRules;


    /**
     * Creates a new <tt>PatientSummary</tt>.
     */
    public PatientSummary() {
        rules = new PatientRules();
        reminderRules = new ReminderRules();
    }

    /**
     * Returns summary information for a patient.
     *
     * @param patient the patient. May be <code>null</code>
     * @return a summary component, or <code>null</code> if there is no summary
     */
    public Component getSummary(final Party patient) {
        Component result = null;
        if (patient != null) {
            result = ColumnFactory.create();
            Label patientName = LabelFactory.create(null, "Patient.Name");
            patientName.setText(patient.getName());
            result.add(RowFactory.create("Inset.Small", patientName));
            if (rules.isDeceased(patient)) {
                Label deceased = LabelFactory.create("patient.deceased",
                                                     "Patient.Deceased");
                result.add(RowFactory.create("Inset.Small", deceased));
            }
            Label species = LabelFactory.create();
            species.setText(getPatientSpecies(patient));
            result.add(RowFactory.create("Inset.Small", species));

            Label breed = LabelFactory.create();
            breed.setText(getPatientBreed(patient));
            result.add(RowFactory.create("Inset.Small", breed));

            Label alertTitle = LabelFactory.create("patient.alerts");
            int alerts = reminderRules.countAlerts(patient, new Date());
            Component alertCount;
            if (alerts == 0) {
                alertCount = LabelFactory.create("patient.noreminders");
            } else {
                alertCount = ButtonFactory.create(
                        null, "alert", new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onShowAlerts(patient);
                    }
                });
                alertCount = RowFactory.create(alertCount);
            }

            Label reminderTitle = LabelFactory.create("patient.reminders");
            int reminders = reminderRules.countReminders(patient);
            Component reminderCount;
            if (reminders == 0) {
                reminderCount = LabelFactory.create("patient.noreminders");
            } else {
                reminderCount = ButtonFactory.create(
                        null, "reminder", new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        onShowReminders(patient);
                    }
                });
                reminderCount = RowFactory.create(reminderCount);
            }
            Label ageTitle = LabelFactory.create("patient.age");
            Label age = LabelFactory.create();
            age.setText(getPatientAge(patient));

            Label weightTitle = LabelFactory.create("patient.weight");
            Label weight = LabelFactory.create();
            weight.setText(getPatientWeight(patient));
            Grid grid = GridFactory.create(2, alertTitle, alertCount,
                                           reminderTitle, reminderCount,
                                           ageTitle, age, weightTitle, weight);
            result.add(grid);
        }
        return result;
    }

    /**
     * Invoked to show alerts for a patient in a popup.
     *
     * @param patient the patient
     */
    private void onShowAlerts(Party patient) {
        PagedIMTable<Act> table = new PagedIMTable<Act>(
                new AlertTableModel(), getAlerts(patient));
        new ViewerDialog(Messages.get("patient.summary.alerts"),
                         "PatientSummary.AlertDialog", table);
    }

    /**
     * Invoked to show reminders for a patient in a popup.
     *
     * @param patient the patient
     */
    private void onShowReminders(Party patient) {
        PagedIMTable<Act> table = new PagedIMTable<Act>(
                new ReminderTableModel(), getReminders(patient));
        table.getTable().setDefaultRenderer(Object.class,
                                            new ReminderTableCellRenderer());
        new ViewerDialog(Messages.get("patient.summary.reminders"),
                         "PatientSummary.ReminderDialog", table);
    }

    /**
     * Returns outstanding alerts for a patient.
     *
     * @param patient the patient
     * @return the set of outstanding alerts for the patient
     */
    private ActResultSet<Act> getAlerts(Party patient) {
        String[] shortNames = {"act.patientAlert"};
        String[] statuses = {ActStatus.IN_PROGRESS};
        ShortNameConstraint archetypes = new ShortNameConstraint(
                shortNames, true, true);
        ParticipantConstraint[] participants = {
                new ParticipantConstraint("patient", "participation.patient",
                                          patient)
        };
        OrConstraint time = new OrConstraint();
        time.add(new NodeConstraint("endTime", RelationalOp.GT, new Date()));
        time.add(new NodeConstraint("endTime", RelationalOp.IsNULL));
        SortConstraint[] sort = {new NodeSortConstraint("endTime", true)};

        return new ActResultSet<Act>(archetypes, participants, time, statuses,
                                     false, null, 5, sort);
    }

    /**
     * Returns the species for a patient.
     *
     * @param patient the patient
     * @return a string representing the patient species
     */
    private String getPatientSpecies(Party patient) {
        return rules.getPatientSpecies(patient);
    }

    /**
     * Returns the breed for a patient.
     *
     * @param patient the patient
     * @return a string representing the patient breed
     */
    private String getPatientBreed(Party patient) {
        return rules.getPatientBreed(patient);
    }

    /**
     * Returns the age for a patient.
     * todo localise
     *
     * @param patient the patient
     * @return a string representing the patient age
     */
    private String getPatientAge(Party patient) {
        return rules.getPatientAge(patient);
    }

    /**
     * Returns the current weight for a patient.
     *
     * @param patient the patient
     * @return a string representing the patient weight
     */
    private String getPatientWeight(Party patient) {
        String weight = rules.getPatientWeight(patient);
        return (weight != null) ? weight : Messages.get("patient.noweight");
    }

    /**
     * Returns outstanding reminders for a patient.
     *
     * @param patient the patient
     * @return the set of outstanding reminders for the patient
     */
    private ResultSet<Act> getReminders(Party patient) {
        String[] shortNames = {"act.patientReminder"};
        String[] statuses = {ActStatus.IN_PROGRESS};
        ShortNameConstraint archetypes = new ShortNameConstraint(
                shortNames, true, true);
        ParticipantConstraint[] participants = {
                new ParticipantConstraint("patient", "participation.patient",
                                          patient)
        };
        SortConstraint[] sort = {new NodeSortConstraint("endTime", true)};
        return new ActResultSet<Act>(archetypes, participants, null,
                                     statuses, false, null, 10, sort);
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
     * Displays a table in popup window.
     *
     * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
     * @version $LastChangedDate: 2006-04-11 04:09:07Z $
     */
    private static class ViewerDialog extends PopupDialog {

        /**
         * Construct a new <code>ViewerDialog</code>.
         *
         * @param table the table to display
         * @param style the window style
         */
        public ViewerDialog(String title, String style,
                            PagedIMTable<Act> table) {
            super(title, style, OK);
            setModal(true);
            getLayout().add(ColumnFactory.create("Inset", table));
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
        protected String[] getNodeNames() {
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
        protected String[] getNodeNames() {
            return new String[]{"reminderType", "endTime", "product"};
        }

    }

}
