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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.ContextException;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.workflow.AbstractTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.DefaultTableHeaderRenderer;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * A task that collects the boarding appointments to check-out.
 *
 * @author Tim Anderson
 */
class GetBoardingAppointmentsTask extends AbstractTask {

    /**
     * The current appointment.
     */
    private final Act appointment;

    /**
     * Collects the visits to check-out.
     */
    private final Visits visits;

    /**
     * The appointment rules.
     */
    private final AppointmentRules rules;

    /**
     * Constructs a {@link GetBoardingAppointmentsTask}.
     *
     * @param appointment the appointment
     * @param visits      collects the visits
     */
    public GetBoardingAppointmentsTask(Act appointment, Visits visits) {
        this.rules = ServiceHelper.getBean(AppointmentRules.class);
        this.appointment = appointment;
        this.visits = visits;
    }

    /**
     * Starts the task.
     *
     * @param context the task context
     * @throws OpenVPMSException for any error
     */
    @Override
    public void start(TaskContext context) {
        Act event = rules.getEvent(appointment);
        if (event == null) {
            Party patient = context.getPatient();
            if (patient == null) {
                throw new ContextException(ContextException.ErrorCode.NoPatient);
            }
            InformationDialog.show(Messages.format("workflow.checkin.visit.novisit", patient.getName(),
                                                   appointment.getActivityStartTime()));
            notifyCancelled();
        } else {
            List<Visit> visits = getVisits(context);
            visits.add(0, this.visits.create(event, appointment));
            this.visits.rate(visits, new Date());
            DefaultLayoutContext layoutContext = new DefaultLayoutContext(context, context.getHelpContext());
            final SelectionDialog dialog = new SelectionDialog(visits, appointment, layoutContext);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    onSelected(dialog.getSelected());
                }

                @Override
                public void onAction(String action) {
                    notifyCancelled();
                }
            });
            dialog.show();
        }
    }

    private void onSelected(List<Visit> visits) {
        this.visits.addAll(visits);
        notifyCompleted();
    }

    private List<Visit> getVisits(TaskContext context) {
        List<Visit> result = new ArrayList<>();
        Party customer = context.getCustomer();
        if (customer == null) {
            throw new ContextException(ContextException.ErrorCode.NoCustomer);
        }
        Party location = context.getLocation();
        if (location == null) {
            throw new ContextException(ContextException.ErrorCode.NoLocation);
        }
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.APPOINTMENT);
        query.add(join("customer").add(eq("entity", customer)));
        query.add(join("schedule").add(join("entity").add(join("location").add(eq("target", location)))));
        query.add(Constraints.ne("id", appointment.getId()));
        query.add(Constraints.not(Constraints.in("status", AppointmentStatus.PENDING, AppointmentStatus.COMPLETED,
                                                 AppointmentStatus.CANCELLED)));
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        IMObjectQueryIterator<Act> iterator = new IMObjectQueryIterator<>(query);
        while (iterator.hasNext()) {
            Act appointment = iterator.next();
            Act event = rules.getEvent(appointment);
            if (event != null) {
                Visit e = visits.create(event, appointment);
                if (e.getCageType() != null) {
                    // if the appointment doesn't have a cage type, its not a boarding appointment
                    result.add(e);
                }
            }
        }
        return result;
    }

    /**
     * Dialog that enables a set of appointments to be selected for check-out.
     */
    private static class SelectionDialog extends PopupDialog {

        /**
         * The table of events.
         */
        private final IMTable<Visit> table;


        /**
         * Constructs a {@link SelectionDialog}.
         *
         * @param events   the events to select
         * @param required the required object. May be {@code null}
         */
        public SelectionDialog(List<Visit> events, Act required, LayoutContext context) {
            super(Messages.get("workflow.checkout.appointments.title"), "MediumDialog", OK_CANCEL,
                  context.getHelpContext());
            setModal(true);
            AppointmentTableModel model = new AppointmentTableModel(events, required, context);
            table = new IMTable<>(model);
            table.setDefaultHeaderRenderer(DefaultTableHeaderRenderer.DEFAULT);
            Column column = ColumnFactory.create(Styles.CELL_SPACING,
                                                 LabelFactory.create("workflow.checkout.appointments.message"),
                                                 table);
            getLayout().add(ColumnFactory.create(Styles.INSET, column));
        }

        /**
         * Returns the selected objects.
         *
         * @return the selected objects
         */
        public List<Visit> getSelected() {
            AppointmentTableModel model = (AppointmentTableModel) table.getModel();
            return model.getSelected();
        }

        /**
         * Invoked when the 'OK' button is pressed. This sets the action and closes
         * the window.
         */
        @Override
        protected void onOK() {
            if (haveSelectedFirstPetRate()) {
                super.onOK();
            }
        }

        /**
         * Ensures that a pet that attracts the First Pet rate is selected. This is required as there is no
         * reliable way of determining if a pet was charged the first or second pet rate once they are checked out.
         *
         * @return {@code true} if a pet is selected that attracts the first pet rate
         */
        private boolean haveSelectedFirstPetRate() {
            boolean first = false;
            for (Visit visit : getSelected()) {
                if (visit.isFirstPet()) {
                    first = true;
                    break;
                }
            }
            if (!first) {
                InformationDialog.show(Messages.get("workflow.checkout.appointments.firstpetselected"));
            }
            return first;
        }
    }

    private static class AppointmentTableModel extends AbstractIMTableModel<Visit> {

        /**
         * The selection boxes.
         */
        private List<CheckBox> selected = new ArrayList<>();

        /**
         * The required appointment. May be {@code null}
         */
        private final Act required;

        private final LayoutContext context;

        /**
         * Selected column model index.
         */
        private static final int SELECTED_INDEX = 0;

        private static final int SCHEDULE_INDEX = SELECTED_INDEX + 1;

        private static final int PATIENT_INDEX = SCHEDULE_INDEX + 1;

        private static final int APPOINTMENT_INDEX = PATIENT_INDEX + 1;

        private static final int EVENT_INDEX = APPOINTMENT_INDEX + 1;

        private static final int REASON_INDEX = EVENT_INDEX + 1;

        private static final int DAYS_INDEX = REASON_INDEX + 1;

        private static final int RATE_INDEX = DAYS_INDEX + 1;

        private static final int LATE_CHECKOUT_INDEX = RATE_INDEX + 1;

        private static final int CHARGED_INDEX = LATE_CHECKOUT_INDEX + 1;

        /**
         * Constructs a {@link AppointmentTableModel}.
         *
         * @param events   the events
         * @param required the required appointment. May be {@code null}
         * @param context  the layout context
         */
        public AppointmentTableModel(List<Visit> events, Act required, LayoutContext context) {
            this.required = required;
            this.context = new DefaultLayoutContext(context);
            this.context.setComponentFactory(new TableComponentFactory(context));
            setObjects(events);
            setTableColumnModel(createTableColumnModel());
        }

        /**
         * Returns the list of events selected for check-out.
         *
         * @return the selected events
         */
        public List<Visit> getSelected() {
            List<Visit> events = new ArrayList<>();
            for (int i = 0; i < selected.size(); ++i) {
                CheckBox check = selected.get(i);
                if (check.isSelected()) {
                    events.add(getObject(i));
                }
            }
            return events;
        }

        /**
         * Sets the objects to display.
         *
         * @param objects the objects to display
         */
        @Override
        public void setObjects(List<Visit> objects) {
            super.setObjects(objects);
            selected = new ArrayList<>();
            for (Visit event : objects) {
                CheckBox box = CheckBoxFactory.create(false);
                if (required != null && ObjectUtils.equals(required, event.getAppointment())) {
                    box.setEnabled(false);
                    box.setSelected(true);
                }
                this.selected.add(box);
            }
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
         * @return the sort criteria, or {@code null} if the column isn't sortable
         */
        @Override
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            return null;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(Visit object, TableColumn column, int row) {
            Object result = null;
            if (column instanceof DescriptorTableColumn) {
                result = ((DescriptorTableColumn) column).getComponent(object.getAppointment(), context);
            } else {
                switch (column.getModelIndex()) {
                    case SELECTED_INDEX:
                        result = selected.get(row);
                        break;
                    case APPOINTMENT_INDEX:
                        result = getAppointment(object);
                        break;
                    case EVENT_INDEX:
                        result = getEvent(object);
                        break;
                    case DAYS_INDEX:
                        result = getDays(object);
                        break;
                    case RATE_INDEX:
                        result = getRate(object);
                        break;
                    case LATE_CHECKOUT_INDEX:
                        result = getLateCheckout(object);
                        break;
                    case CHARGED_INDEX:
                        result = getCharged(object);
                        break;
                }
            }
            return result;
        }

        private Object getAppointment(Visit visit) {
            Act act = visit.getAppointment();
            Date startTime = act.getActivityStartTime();
            String from = DateFormatter.formatDateTimeAbbrev(startTime);
            String to = DateFormatter.formatDateTimeAbbrev(act.getActivityEndTime(), startTime);
            return from + " - " + to;
        }

        private Object getEvent(Visit visit) {
            Date startTime = visit.getStartTime();
            Date endTime = visit.getEndTime();
            String from = DateFormatter.formatDateTimeAbbrev(startTime);
            String to = (endTime != null) ? DateFormatter.formatDateTimeAbbrev(endTime, startTime) : "now";
            return from + " - " + to;
        }

        private Object getDays(Visit visit) {
            return TableHelper.rightAlign(Integer.toString(visit.getDays()));
        }

        private Object getRate(Visit visit) {
            String key = visit.isFirstPet() ? "workflow.checkout.appointments.first"
                                            : "workflow.checkout.appointments.second";
            return Messages.get(key);
        }

        private Object getLateCheckout(Visit visit) {
            return getCheckBox(visit.isLateCheckout());
        }

        private Object getCharged(Visit visit) {
            return getCheckBox(visit.isCharged());
        }

        /**
         * Creates a new column model.
         *
         * @return a new column model
         */
        protected TableColumnModel createTableColumnModel() {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            ArchetypeDescriptor descriptor = DescriptorHelper.getArchetypeDescriptor(ScheduleArchetypes.APPOINTMENT);
            model.addColumn(new TableColumn(SELECTED_INDEX));
            model.addColumn(new DescriptorTableColumn(SCHEDULE_INDEX, "schedule", descriptor));
            model.addColumn(new DescriptorTableColumn(PATIENT_INDEX, "patient", descriptor));
            model.addColumn(new DescriptorTableColumn(REASON_INDEX, "reason", descriptor));
            model.addColumn(createTableColumn(APPOINTMENT_INDEX, "workflow.checkout.appointments.appointment"));
            model.addColumn(createTableColumn(EVENT_INDEX, "workflow.checkout.appointments.event"));
            model.addColumn(createTableColumn(DAYS_INDEX, "workflow.checkout.appointments.days"));
            model.addColumn(createTableColumn(RATE_INDEX, "workflow.checkout.appointments.rate"));
            model.addColumn(createTableColumn(LATE_CHECKOUT_INDEX, "workflow.checkout.appointments.latecheckout"));
            model.addColumn(createTableColumn(CHARGED_INDEX, "workflow.checkout.appointments.charged"));
            return model;
        }
    }

}
