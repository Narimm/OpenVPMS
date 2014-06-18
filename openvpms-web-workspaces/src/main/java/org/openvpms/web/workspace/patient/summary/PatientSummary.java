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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.patient.summary;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.im.view.IMObjectViewerDialog;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.alert.Alert;
import org.openvpms.web.workspace.alert.AlertSummary;
import org.openvpms.web.workspace.customer.estimate.CustomerEstimates;
import org.openvpms.web.workspace.customer.estimate.EstimateViewer;
import org.openvpms.web.workspace.summary.PartySummary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_PARTICIPATION;

/**
 * Renders Patient Summary Information.
 *
 * @author Tim Anderson
 */
public class PatientSummary extends PartySummary {

    /**
     * The patient rules.
     */
    private final PatientRules rules;

    /**
     * The reminder rules.
     */
    private final ReminderRules reminderRules;


    /**
     * Constructs a {@link PatientSummary}.
     *
     * @param context the context
     * @param help    the help context
     */
    public PatientSummary(Context context, HelpContext help) {
        super(context, help);
        rules = ServiceHelper.getBean(PatientRules.class);
        reminderRules = new ReminderRules(ServiceHelper.getArchetypeService(), rules);
    }

    /**
     * Returns summary information for a party.
     * <p/>
     * The summary includes any alerts.
     *
     * @param patient the patient
     * @return a summary component
     */
    protected Component createSummary(Party patient) {
        Component column = ColumnFactory.create();

        List<Component> components = getSummaryComponents(patient);
        for (Component component : components) {
            if (!(component instanceof Grid)) {
                // grid already inset.... ugly TODO
                column.add(ColumnFactory.create("Inset.Small", component));
            } else {
                column.add(component);
            }
        }
        AlertSummary alerts = getAlertSummary(patient);
        if (alerts != null) {
            column.add(ColumnFactory.create("Inset.Small", alerts.getComponent()));
        }
        return ColumnFactory.create("PartySummary", column);
    }

    /**
     * Returns the summary components for a patient.
     *
     * @param patient the patient
     * @return the summary components
     */
    protected List<Component> getSummaryComponents(Party patient) {
        List<Component> result = new ArrayList<Component>();
        result.add(getPatientName(patient));
        result.add(getPatientId(patient));
        if (rules.isDeceased(patient)) {
            result.add(getDeceased());
        }

        result.add(getSpecies(patient));
        result.add(getBreed(patient));
        result.add(createSummaryGrid(patient));
        result.add(addReferralVet(patient));
        return result;
    }

    /**
     * Returns a component that displays the patient name.
     *
     * @param patient the patient
     * @return the patient name
     */
    protected Component getPatientName(Party patient) {
        String name = patient.getName();
        if (rules.isDesexed(patient)) {
            name += " (" + getPatientSex(patient) + " " + Messages.get("patient.desexed") + ")";
        } else {
            name += " (" + getPatientSex(patient) + " " + Messages.get("patient.entire") + ")";
        }
        IMObjectReferenceViewer patientName = new IMObjectReferenceViewer(patient.getObjectReference(), name, true,
                                                                          getContext());
        patientName.setStyleName("hyperlink-bold");
        return patientName.getComponent();
    }

    /**
     * Returns the patient Id component.
     *
     * @param patient the patient
     * @return the patient Id
     */
    protected Component getPatientId(Party patient) {
        return createLabel("patient.id", patient.getId());
    }

    /**
     * Returns a component indicating the patient is deceased.
     *
     * @return the component
     */
    protected Component getDeceased() {
        return LabelFactory.create("patient.deceased", "Patient.Deceased");
    }

    /**
     * Returns a component that displays the patient species.
     *
     * @param patient the patient
     * @return the patient species
     */
    protected Component getSpecies(Party patient) {
        Label species = LabelFactory.create();
        species.setText(getPatientSpecies(patient));
        return species;
    }

    /**
     * Returns a component that displays the patient breed.
     *
     * @param patient the patient
     * @return the patient breed
     */
    protected Component getBreed(Party patient) {
        Label breed = LabelFactory.create();
        breed.setText(getPatientBreed(patient));
        return breed;
    }

    /**
     * Displays a summary of patient information in a grid.
     *
     * @param patient the patient
     * @return the summary grid
     */
    protected Grid createSummaryGrid(Party patient) {
        Grid grid = GridFactory.create(2);

        addPopupButtons(patient, grid);
        addAge(patient, grid);
        addWeight(patient, grid);
        addMicrochip(patient, grid);
        return grid;
    }

    /**
     * Displays buttons to view patient reminders and estimates.
     *
     * @param patient the patient
     * @param grid    the summary grid
     */
    protected void addPopupButtons(final Party patient, Grid grid) {
        Label label = LabelFactory.create("patient.reminders");  // the buttons are kinda sorta reminders
        Component component;
        Button reminders = getReminderButton(patient);
        Button estimates = getEstimateButton(patient);

        if (reminders == null && estimates == null) {
            component = LabelFactory.create("patient.noreminders");
        } else {
            component = RowFactory.create(Styles.CELL_SPACING);
            if (reminders != null) {
                component.add(reminders);
            }
            if (estimates != null) {
                component.add(estimates);
            }
        }
        grid.add(label);
        grid.add(component);
    }

    /**
     * Returns a button to launch a viewer of patient reminders, if there are any.
     *
     * @param patient the patient
     * @return a button, or {@code null} if there are no reminders
     */
    private Button getReminderButton(final Party patient) {
        Button result = null;
        ReminderRules.DueState due = getDueState(patient);
        if (due != null) {
            String style = "reminder." + due.toString();
            result = ButtonFactory.create(null, style, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onShowReminders(patient);
                }
            });
        }
        return result;
    }

    /**
     * Adds estimates.
     *
     * @param patient the patient
     */
    protected Button getEstimateButton(final Party patient) {
        Button result = null;
        if (hasEstimates(patient)) {
            result = ButtonFactory.create(null, "estimate.available", new ActionListener() {
                public void onAction(ActionEvent event) {
                    onShowEstimates(patient);
                }
            });
        }
        return result;
    }

    /**
     * Displays the patient age in a grid.
     *
     * @param patient the patient
     * @param grid    the grid
     */
    protected void addAge(Party patient, Grid grid) {
        Label ageTitle = LabelFactory.create("patient.age");
        Label age = LabelFactory.create();
        age.setText(rules.getPatientAge(patient));
        grid.add(ageTitle);
        grid.add(age);
    }

    /**
     * Displays the patient weight in a grid.
     *
     * @param patient the patient
     * @param grid    the grid
     */
    private void addWeight(Party patient, Grid grid) {
        Label weightTitle = LabelFactory.create("patient.weight");
        Label weight = LabelFactory.create();
        weight.setText(getPatientWeight(patient));
        grid.add(weightTitle);
        grid.add(weight);
    }

    /**
     * Displays the patient microchip in a grid.
     *
     * @param patient the patient
     * @param grid    the grid
     */
    protected void addMicrochip(Party patient, Grid grid) {
        String identity = rules.getMicrochip(patient);
        if (identity != null) {
            Label microchipTitle = LabelFactory.create("patient.microchip");
            Label microchip = LabelFactory.create();
            microchip.setText(identity);
            grid.add(microchipTitle);
            grid.add(microchip);
        }
    }

    /**
     * Displays the referral vet if indicated otherwise is empty.
     *
     * @param patient the patient
     * @return Grid with referral Vet info
     */
    protected Grid addReferralVet(Party patient) {
        Grid grid = GridFactory.create(1);
        Party vet = rules.getReferralVet(patient, new Date());
        if (vet != null) {
            Label title = LabelFactory.create("patient.referralvet");
            Label name = LabelFactory.create();
            name.setText(vet.getName());
            grid.add(title);
            grid.add(RowFactory.create("InsetX", name));
            Component practice = getReferralPractice(vet);
            if (practice != null) {
                grid.add(RowFactory.create("InsetX", practice));
            }
        }
        return grid;
    }

    /**
     * Returns a component displaying the referral practice.
     *
     * @param vet the referring vet
     * @return the referral practice hyperlinked, or {@code null} if the vet isn't linked to a practice
     */
    protected Component getReferralPractice(Party vet) {
        Button result = null;
        SupplierRules bean = ServiceHelper.getBean(SupplierRules.class);
        final Party practice = bean.getReferralVetPractice(vet, new Date());
        if (practice != null) {
            result = ButtonFactory.create(null, "hyperlink-bold", new ActionListener() {
                public void onAction(ActionEvent event) {
                    onShowReferralVet(practice);
                }

            });
            result.setText(practice.getName());
        }
        return result;
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
        String[] statuses = {ActStatus.IN_PROGRESS};
        ShortNameConstraint archetypes = new ShortNameConstraint(PatientArchetypes.ALERT, true, true);
        ParticipantConstraint[] participants = {new ParticipantConstraint("patient", PATIENT_PARTICIPATION, patient)};

        IConstraint dateRange = QueryHelper.createDateRangeConstraint(new Date());
        // constrain to alerts that intersect today

        return new ActResultSet<Act>(archetypes, participants, dateRange, statuses, false, null, pageSize, null);
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
        ParticipantConstraint[] participants = {new ParticipantConstraint("patient", PATIENT_PARTICIPATION, patient)};
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
        PagedIMTable<Act> table = new PagedIMTable<Act>(new ReminderTableModel(getContext(), getHelpContext()),
                                                        getReminders(patient));
        table.getTable().setDefaultRenderer(Object.class, new ReminderTableCellRenderer());
        new ViewerDialog(Messages.get("patient.summary.reminders"), "PatientSummary.ReminderDialog", table);
    }

    /**
     * Displays estimates for a patient.
     *
     * @param patient the patient
     */
    private void onShowEstimates(Party patient) {
        Party customer = rules.getOwner(patient);
        if (customer != null) {
            CustomerEstimates query = new CustomerEstimates();
            List<Act> estimates = query.getEstimates(customer, patient);
            if (!estimates.isEmpty()) {
                EstimateViewer viewer = new EstimateViewer(estimates, getContext(), getHelpContext());
                viewer.show();
            }
        }
    }

    /**
     * Displays a referral vet.
     *
     * @param vet the vet
     */
    private void onShowReferralVet(Party vet) {
        IMObjectViewerDialog dialog = new IMObjectViewerDialog(vet, PopupDialog.OK, getContext(), getHelpContext());
        dialog.setStyleName("PatientSummary.ReferralDialog");
        dialog.show();
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
     * Determines if there are any estimates for the patient.
     *
     * @param patient the patient
     * @return {@code true} if there are estimates
     */
    private boolean hasEstimates(Party patient) {
        Party customer = rules.getOwner(patient);
        if (customer != null) {
            CustomerEstimates query = new CustomerEstimates();
            return query.hasEstimates(customer, patient);
        }
        return false;
    }

    /**
     * Helper to create a layout context where hyperlinks are disabled.
     *
     * @param help the help context
     * @return a new layout context
     */
    private static LayoutContext createLayoutContext(Context context, HelpContext help) {
        LayoutContext result = new DefaultLayoutContext(context, help);
        result.setEdit(true); // hack to disable hyperlinks
        TableComponentFactory factory = new TableComponentFactory(result);
        result.setComponentFactory(factory);
        return result;
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
         * Constructs a {@code ReminderTableModel}.
         *
         * @param context the context
         * @param help    the help context
         */
        public ReminderTableModel(Context context, HelpContext help) {
            super(new String[]{ReminderArchetypes.REMINDER}, createLayoutContext(context, help));
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
