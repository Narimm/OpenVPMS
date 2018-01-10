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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.summary;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.archetype.rules.patient.insurance.InsuranceRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.archetype.rules.supplier.SupplierRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextApplicationInstance;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.im.view.IMObjectViewerDialog;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.alert.Alert;
import org.openvpms.web.workspace.alert.AlertSummary;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.customer.estimate.CustomerEstimates;
import org.openvpms.web.workspace.customer.estimate.EstimateViewer;
import org.openvpms.web.workspace.patient.PatientIdentityEditor;
import org.openvpms.web.workspace.summary.PartySummary;
import org.openvpms.web.workspace.workflow.worklist.FollowUpTaskEditor;

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
     * The insurance rules.
     */
    private final InsuranceRules insuranceRules;

    /**
     * Value of <em>showReferrals</em> to indicate to always show referral information.
     */
    private static final String ALWAYS_SHOW_REFERRAL = "ALWAYS";

    /**
     * Value of <em>showReferrals</em> to indicate to show the active referral.
     */
    private static final String SHOW_ACTIVE_REFERRAL = "ACTIVE";

    /**
     * Value of <em>showReferrals</em> to indicate to never show referrals in the summary.
     */
    private static final String NEVER_SHOW_REFERRAL = "NEVER";

    /**
     * Constructs a {@link PatientSummary}.
     *
     * @param context     the context
     * @param help        the help context
     * @param preferences user preferences
     */
    public PatientSummary(Context context, HelpContext help, Preferences preferences) {
        super(context, help, preferences);
        rules = ServiceHelper.getBean(PatientRules.class);
        reminderRules = ServiceHelper.getBean(ReminderRules.class);
        insuranceRules = ServiceHelper.getBean(InsuranceRules.class);
    }

    /**
     * Returns summary information for a party.
     * <p>
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
                column.add(ColumnFactory.create(Styles.SMALL_INSET, component));
            } else {
                column.add(component);
            }
        }
        AlertSummary alerts = getAlertSummary(patient);
        if (alerts != null) {
            column.add(ColumnFactory.create(Styles.SMALL_INSET, alerts.getComponent()));
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
        List<Component> result = new ArrayList<>();
        result.add(getPatientName(patient));
        result.add(getPatientId(patient));
        if (rules.isDeceased(patient)) {
            result.add(getDeceased());
        }

        result.add(getSpecies(patient));
        result.add(getBreed(patient));
        result.add(createSummaryGrid(patient));
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
        Label patientId = createLabel("patient.id", patient.getId());
        Button followup = ButtonFactory.create(null, "button.followup", new ActionListener() {
            public void onAction(ActionEvent event) {
                onFollowUp();
            }
        });
        Row right = RowFactory.create(followup);

        RowLayoutData rightLayout = new RowLayoutData();
        rightLayout.setAlignment(Alignment.ALIGN_RIGHT);
        rightLayout.setWidth(Styles.FULL_WIDTH);
        right.setLayoutData(rightLayout);

        return RowFactory.create(Styles.WIDE_CELL_SPACING, patientId, right);
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
        addDateOfBirth(patient, grid);
        addWeight(patient, grid);
        addMicrochip(patient, grid);
        addInsurancePolicy(patient, grid);
        addReferral(patient, grid);
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
    protected Button getReminderButton(final Party patient) {
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
     * Displays the patient date of birth in a grid.
     *
     * @param patient the patient
     * @param grid    the grid
     */
    protected void addDateOfBirth(Party patient, Grid grid) {
        Label title = LabelFactory.create("patient.dateOfBirth");
        Label text = LabelFactory.create();
        Date dateOfBirth = rules.getDateOfBirth(patient);
        if (dateOfBirth != null) {
            text.setText(DateFormatter.formatDate(dateOfBirth, false));
        }
        grid.add(title);
        grid.add(text);
    }

    /**
     * Displays the patient weight in a grid.
     *
     * @param patient the patient
     * @param grid    the grid
     */
    protected void addWeight(Party patient, Grid grid) {
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
    protected void addMicrochip(final Party patient, Grid grid) {
        Label title = LabelFactory.create("patient.microchip");
        final Row container = new Row();
        grid.add(title);
        grid.add(container);
        refreshMicrochip(patient, container);
    }

    /**
     * Refreshes the microchip display.
     *
     * @param patient   the patient
     * @param container the microchip container
     */
    protected void refreshMicrochip(final Party patient, final Component container) {
        container.removeAll();
        String identity = rules.getMicrochipNumber(patient);
        if (identity != null) {
            Label microchip = LabelFactory.create();
            microchip.setText(identity);
            container.add(microchip);
        } else {
            Button add = ButtonFactory.create("button.add", Styles.DEFAULT, false, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onCreateMicrochip(patient, container);
                }
            });
            container.add(add);
        }
    }

    /**
     * Displays the patient insurance policy in a grid.
     *
     * @param patient the patient
     * @param grid    the grid
     */
    protected void addInsurancePolicy(Party patient, Grid grid) {
        Label title = LabelFactory.create("patient.insurance");
        Act policy = insuranceRules.getPolicy(patient);
        String name;
        if (policy == null) {
            name = Messages.get("patient.insurance.none");
        } else {
            Date endTime = policy.getActivityEndTime();
            if (endTime != null && endTime.compareTo(new Date()) < 0) {
                name = Messages.get("patient.insurance.expired");
            } else {
                Party insurer = insuranceRules.getInsurer(policy);
                name = (insurer != null) ? insurer.getName() : Messages.get("patient.insurance.none");
            }
        }

        Button button = ButtonFactory.create(null, "hyperlink-bold", new ActionListener() {
            public void onAction(ActionEvent event) {
                ContextApplicationInstance instance = ContextApplicationInstance.getInstance();
                ContextHelper.setPatient(instance.getContext(), patient);
                instance.switchTo(InsuranceArchetypes.POLICY);
            }
        });
        button.setText(name);

        grid.add(title);
        grid.add(button);
    }

    /**
     * Displays the patient referral in a grid, based on the practice <em>showReferrals</em> node. If it is:
     * <ul>
     * <li>ALWAYS - the active referral is displayed, or if there is none, {@code None} is displayed </li>
     * <li>ACTIVE - the active referral is displayed. If there is no active referral, nothing is displayed</li>
     * <li>NEVER - no referral is displayed</li>
     * </ul>
     *
     * @param patient the patient
     * @param grid    the grid
     */
    protected void addReferral(Party patient, Grid grid) {
        String showReferral = getPreferences().getString(PreferenceArchetypes.SUMMARY, "showReferral",
                                                         NEVER_SHOW_REFERRAL);
        if (!NEVER_SHOW_REFERRAL.equals(showReferral)) {
            final Party vet = rules.getReferralVet(patient, new Date());
            if (vet != null || ALWAYS_SHOW_REFERRAL.equals(showReferral)) {
                grid.add(LabelFactory.create("patient.referralvet"));
                if (vet != null) {
                    grid.add(new Label());
                    Button name = ButtonFactory.create(null, "hyperlink-bold", new ActionListener() {
                        public void onAction(ActionEvent event) {
                            onShowReferralVet(vet);
                        }
                    });
                    name.setText(vet.getName());
                    GridLayoutData layout = ComponentGrid.layout(1, 2);
                    grid.add(RowFactory.create(Styles.INSET_X, layout, name));
                    Component referralPractice = getReferralPractice(vet);
                    if (referralPractice != null) {
                        Row row = RowFactory.create(Styles.INSET_X, layout, referralPractice);
                        grid.add(row);
                    }
                } else {
                    grid.add(LabelFactory.create("imobject.none"));
                }
            }
        }
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
    @Override
    protected List<Alert> getAlerts(Party party) {
        List<Alert> result = new ArrayList<>();
        ResultSet<Act> set = createAlertsResultSet(party, 20);
        ResultSetIterator<Act> iterator = new ResultSetIterator<>(set);
        while (iterator.hasNext()) {
            Act act = iterator.next();
            ActBean bean = new ActBean(act);
            Entity alertType = bean.getNodeParticipant("alertType");
            if (alertType != null) {
                result.add(new Alert(alertType, act));
            }
        }
        return result;
    }

    /**
     * Invoked when the 'Follow-up' button is pressed.
     */
    protected void onFollowUp() {
        Act act = (Act) IMObjectCreator.create(ScheduleArchetypes.TASK);
        DefaultLayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext().topic(act, "edit"));
        List<Entity> workLists = FollowUpTaskEditor.getWorkLists(getContext());
        if (workLists.isEmpty()) {
            InformationDialog.show(Messages.get("patient.followup.noworklists"));
        } else {
            FollowUpTaskEditor editor = new FollowUpTaskEditor(act, workLists, context);
            EditDialog dialog = EditDialogFactory.create(editor, getContext());
            dialog.show();
        }
    }

    /**
     * Invoked to create a new microchip linked to the patient.
     *
     * @param patient   the patient
     * @param container the container to display the microchip
     */
    protected void onCreateMicrochip(Party patient, final Component container) {
        final PatientIdentityEditor editor = PatientIdentityEditor.create(patient, PatientArchetypes.MICROCHIP,
                                                                          getContext(), getHelpContext());
        if (editor != null) {
            EditDialog dialog = editor.edit(false);
            dialog.addWindowPaneListener(
                    new PopupDialogListener() {
                        @Override
                        public void onOK() {
                            Party latest = editor.getPatient();
                            refreshMicrochip(latest, container);

                            // if the patient is selected, refresh it
                            GlobalContext globalContext = ContextApplicationInstance.getInstance().getContext();
                            if (ObjectUtils.equals(latest, globalContext.getPatient())) {
                                globalContext.setPatient(latest);
                            }
                        }
                    }
            );
            dialog.show();
        }
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

        return new ActResultSet<>(archetypes, participants, dateRange, statuses, false, null, pageSize, null);
    }

    /**
     * Returns the highest due state of a patient's reminders.
     *
     * @param patient the patient
     * @return the patient's highest due state. May be {@code null}
     */
    protected ReminderRules.DueState getDueState(Party patient) {
        ActResultSet<Act> reminders = createActResultSet(patient, 20, ReminderArchetypes.REMINDER);
        ResultSetIterator<Act> iterator = new ResultSetIterator<>(reminders);
        ReminderRules.DueState result = null;
        while (iterator.hasNext()) {
            ReminderRules.DueState due = getDueState(iterator.next());
            if (result == null || due.compareTo(result) > 0) {
                result = due;
            }
            if (result == ReminderRules.DueState.OVERDUE) {
                break;
            }
        }
        return result;
    }

    /**
     * Determines the due state of a reminder relative to the current date.
     *
     * @param reminder the reminder
     * @return the due state
     */
    protected ReminderRules.DueState getDueState(Act reminder) {
        return reminderRules.getDueState(reminder);
    }

    /**
     * Invoked to show reminders for a patient in a popup.
     *
     * @param patient the patient
     */
    protected void onShowReminders(Party patient) {
        PagedIMTable<Act> table = new PagedIMTable<>(new ReminderTableModel(getContext(), getHelpContext()),
                                                     getReminders(patient));
        table.getTable().setDefaultRenderer(Object.class, new ReminderTableCellRenderer());
        new ViewerDialog(Messages.get("patient.summary.reminders"), "PatientSummary.ReminderDialog", table);
    }

    /**
     * Returns outstanding reminders for a patient.
     *
     * @param patient the patient
     * @return the set of outstanding reminders for the patient
     */
    protected ResultSet<Act> getReminders(Party patient) {
        String[] shortNames = {ReminderArchetypes.REMINDER};
        String[] statuses = {ActStatus.IN_PROGRESS};
        ShortNameConstraint archetypes = new ShortNameConstraint(
                shortNames, true, true);
        ParticipantConstraint[] participants = {
                new ParticipantConstraint("patient", "participation.patient", patient)
        };
        SortConstraint[] sort = {new NodeSortConstraint("endTime", true)};
        return new ActResultSet<>(archetypes, participants, null, statuses, false, null, 10, sort);
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
        return new ActResultSet<>(archetypes, participants, null, statuses, false, null, pageSize, null);
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
        Context context = getContext();
        HelpContext help = getHelpContext();
        CustomerMailContext mailContext = new CustomerMailContext(context, help);
        IMObjectViewerDialog dialog = new IMObjectViewerDialog(vet, PopupDialog.OK, context, mailContext, help);
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
    protected static class ViewerDialog extends PopupDialog {

        /**
         * Constructs a {@link ViewerDialog}.
         *
         * @param title the dialog title
         * @param style the window style
         * @param table the table to display
         */
        public ViewerDialog(String title, String style, PagedIMTable<Act> table) {
            super(title, style, OK);
            setModal(true);
            getLayout().add(ColumnFactory.create(Styles.INSET, table.getComponent()));
            show();
        }
    }

    protected static class ReminderTableModel extends AbstractActTableModel {

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
