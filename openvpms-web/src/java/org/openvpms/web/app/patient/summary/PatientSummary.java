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
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.alert.Alert;
import org.openvpms.web.app.alert.AlertSummary;
import org.openvpms.web.app.summary.PartySummary;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Renders Patient Summary Information.
 *
 * @author Tim Anderson
 */
public class PatientSummary extends PartySummary {

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The reminder rules.
     */
    private final ReminderRules reminderRules;


    /**
     * Constructs a {@code PatientSummary}.
     *
     * @param help the help context
     */
    public PatientSummary(HelpContext help) {
        this.help = help;
        rules = (PatientRules) ServiceHelper.getContext().getBean("patientRules");
        reminderRules = new ReminderRules(ServiceHelper.getArchetypeService(), rules);
    }

    /**
     * Returns summary information for a party.
     * <p/>
     * The summary includes any alerts.
     *
     * @param party the party
     * @return a summary component
     */
    protected Component createSummary(final Party party) {
        Component column = ColumnFactory.create();
        String name = party.getName();
        if (rules.isDesexed(party)) {
            name += " (" + getPatientSex(party) + " " + Messages.get("patient.desexed") + ")";
        } else {
            name += " (" + getPatientSex(party) + " " + Messages.get("patient.entire") + ")";
        }
        IMObjectReferenceViewer patientName
                = new IMObjectReferenceViewer(party.getObjectReference(),
                                              name, true);
        patientName.setStyleName("hyperlink-bold");
        column.add(RowFactory.create("Inset.Small", patientName.getComponent()));
        if (rules.isDeceased(party)) {
            Label deceased = LabelFactory.create("patient.deceased", "Patient.Deceased");
            column.add(RowFactory.create("Inset.Small", deceased));
        }
        Label species = LabelFactory.create();
        species.setText(getPatientSpecies(party));
        column.add(RowFactory.create("Inset.Small", species));

        Label breed = LabelFactory.create();
        breed.setText(getPatientBreed(party));
        column.add(RowFactory.create("Inset.Small", breed));

        Label reminderTitle = LabelFactory.create("patient.reminders");
        Component reminders;
        ReminderRules.DueState due = getDueState(party);
        if (due == null) {
            reminders = LabelFactory.create("patient.noreminders");
        } else {
            String style = "reminder." + due.toString();
            reminders = ButtonFactory.create(null, style, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onShowReminders(party);
                }
            });
            reminders = RowFactory.create(reminders);
        }
        Label ageTitle = LabelFactory.create("patient.age");
        Label age = LabelFactory.create();
        age.setText(rules.getPatientAge(party));

        Label weightTitle = LabelFactory.create("patient.weight");
        Label weight = LabelFactory.create();
        weight.setText(getPatientWeight(party));
        Grid grid = GridFactory.create(2, reminderTitle, reminders,
                                       ageTitle, age, weightTitle, weight);

        String identity = rules.getMicrochip(party);
        if (identity != null) {
            Label microchipTitle = LabelFactory.create("patient.microchip");
            Label microchip = LabelFactory.create();
            microchip.setText(identity);
            grid.add(microchipTitle);
            grid.add(microchip);
        }

        column.add(grid);

        AlertSummary alerts = getAlertSummary(party);
        if (alerts != null) {
            grid.add(LabelFactory.create("alerts.patient"));
            column.add(ColumnFactory.create("Inset.Small", alerts.getComponent()));
        }
        return ColumnFactory.create("PartySummary", column);
    }

    /**
     * Returns the alerts for a party.
     *
     * @param party the party
     * @return the party's alerts
     */
    protected List<Alert> getAlerts(Party party) {
        return queryAlerts(party);
    }

    /**
     * Returns outstanding alerts for a patient.
     *
     * @param patient  the patient
     * @param pageSize the no. of alerts to return per page
     * @return the set of outstanding alerts for the patient
     */
    protected ActResultSet<Act> createAlertsResultSet(Party patient, int pageSize) {
        return createActResultSet(patient, pageSize, PatientArchetypes.ALERT);
    }

    /**
     * Returns outstanding acts for a patient.
     *
     * @param patient  the patient
     * @param pageSize the no. of alerts to return per page
     * @return the set IN_PROGRESS acts for the patient
     */
    private ActResultSet<Act> createActResultSet(Party patient, int pageSize, String... shortNames) {
        String[] statuses = {ActStatus.IN_PROGRESS};
        ShortNameConstraint archetypes = new ShortNameConstraint(shortNames, true, true);
        ParticipantConstraint[] participants = {new ParticipantConstraint("patient", "participation.patient", patient)};
        return new ActResultSet<Act>(archetypes, participants, null, statuses, false, null, pageSize, null);
    }

    /**
     * Returns the highest due state of a patient's reminders.
     *
     * @param patient the patient
     * @return the patient's highest due state
     */
    private ReminderRules.DueState getDueState(Party patient) {
        ActResultSet<Act> reminders = createActResultSet(patient, 20, ReminderArchetypes.REMINDER);
        ResultSetIterator<Act> iterator = new ResultSetIterator<Act>(reminders);
        ReminderRules.DueState result = null;
        while (iterator.hasNext()) {
            ReminderRules.DueState due = reminderRules.getDueState(iterator.next());
            if (due != null) {
                if (result == null || due.compareTo(result) > 0) {
                    result = due;
                }
                if (result == ReminderRules.DueState.OVERDUE) {
                    break;
                }
            }

        }
        return result;
    }

    /**
     * Invoked to show reminders for a patient in a popup.
     *
     * @param patient the patient
     */
    private void onShowReminders(Party patient) {
        PagedIMTable<Act> table = new PagedIMTable<Act>(new ReminderTableModel(help), getReminders(patient));
        table.getTable().setDefaultRenderer(Object.class, new ReminderTableCellRenderer());
        new ViewerDialog(Messages.get("patient.summary.reminders"), "PatientSummary.ReminderDialog", table);
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
     * Returns the sex for a patient.
     *
     * @param patient the patient
     * @return a string representing the patient sex
     */
    private String getPatientSex(Party patient) {
        return rules.getPatientSex(patient);
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
        String[] shortNames = {ReminderArchetypes.REMINDER};
        String[] statuses = {ActStatus.IN_PROGRESS};
        ShortNameConstraint archetypes = new ShortNameConstraint(
                shortNames, true, true);
        ParticipantConstraint[] participants = {
                new ParticipantConstraint("patient", "participation.patient", patient)
        };
        SortConstraint[] sort = {new NodeSortConstraint("endTime", true)};
        return new ActResultSet<Act>(archetypes, participants, null,
                                     statuses, false, null, 10, sort);
    }

    /**
     * Helper to create a layout context where hyperlinks are disabled.
     *
     * @param help the help context
     * @return a new layout context
     */
    private static LayoutContext createLayoutContext(HelpContext help) {
        LayoutContext context = new DefaultLayoutContext(help);
        context.setEdit(true); // hack to disable hyerlinks
        TableComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        return context;
    }

    /**
     * Displays a table in popup window.
     */
    private static class ViewerDialog extends PopupDialog {

        /**
         * Constructs a {@code ViewerDialog}.
         *
         * @param title the dialog title
         * @param style the window style
         * @param table the table to display
         */
        public ViewerDialog(String title, String style, PagedIMTable<Act> table) {
            super(title, style, OK);
            setModal(true);
            getLayout().add(ColumnFactory.create("Inset", table));
            show();
        }
    }

    private static class ReminderTableModel extends AbstractActTableModel {

        /**
         * Constructs an {@code ReminderTableModel}.
         */
        public ReminderTableModel(HelpContext help) {
            super(new String[]{ReminderArchetypes.REMINDER}, createLayoutContext(help));
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
