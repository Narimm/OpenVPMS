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

package org.openvpms.web.workspace.workflow.checkin;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.web.component.alert.MandatoryAlerts;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.clinician.ClinicianReferenceEditor;
import org.openvpms.web.component.im.edit.DefaultEditableComponentFactory;
import org.openvpms.web.component.im.edit.PatientReferenceEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.AbstractSaveableEditor;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.ClinicalEventFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.openvpms.component.business.service.archetype.helper.DescriptorHelper.getDisplayName;
import static org.openvpms.component.model.bean.Policies.active;
import static org.openvpms.web.echo.style.Styles.BOLD;

/**
 * Edits check-in details for a patient.
 *
 * @author Tim Anderson
 */
public class CheckInEditor extends AbstractSaveableEditor {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The schedule. May be {@code null}
     */
    private final Entity schedule;

    /**
     * Wraps the schedule.
     */
    private final IMObjectBean scheduleBean;

    /**
     * The practice location.
     */
    private final Party location;

    /**
     * The arrival time.
     */
    private final Date arrivalTime;

    /**
     * The appointment. May be {@code null}
     */
    private final Act appointment;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Property for the patient editor.
     */
    private final SimpleProperty patientProperty = new SimpleProperty("patient", Reference.class);

    /**
     * Property for the clinician editor.
     */
    private final SimpleProperty clinicianProperty = new SimpleProperty("clinician", Reference.class);

    /**
     * The layout context.
     */
    private final LayoutContext layoutContext;

    /**
     * Displays mandatory customer and patient alerts.
     */
    private final MandatoryAlerts alerts;

    /**
     * The flow sheet service factory.
     */
    private final FlowSheetServiceFactory flowSheetServiceFactory;

    /**
     * Determines if Smart Flow Sheet is enabled at the practice location.
     */
    private final boolean enableSmartFlow;

    /**
     * The cage type.
     */
    private final Entity cageType;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The task panel. May be {@code null}
     */
    private final TaskPanel taskPanel;

    /**
     * The patient weight panel. May be {@code null}
     */
    private final WeightPanel weightPanel;

    /**
     * The no. of days a patient is boarding, based on the appointment dates.
     */
    private final int boardingDays;

    /**
     * The clinical event factory.
     */
    private ClinicalEventFactory clinicalEventFactory;

    /**
     * The clinician to assign to new events. May be {@code null}
     */
    private User clinician;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The patient visit. An <em>act.patientClinicalEvent</em>.
     */
    private Act visit;

    /**
     * Visit validation error.
     */
    private String visitError;

    /**
     * The bean wrapping the visit.
     */
    private IMObjectBean visitBean;

    /**
     * The editor component.
     */
    private Component component;

    /**
     * The patient reference editor.
     */
    private PatientReferenceEditor patientReferenceEditor;

    /**
     * The Smart Flow Sheet field panel.
     */
    private FlowSheetPanel flowsheet;

    /**
     * The documents panel.
     */
    private DocumentPanel documents;


    /**
     * Constructs a {@link CheckInDialog}.
     *
     * @param customer    the customer
     * @param patient     the patient. May be {@code null}
     * @param schedule    the appointment schedule. May be {@code null}
     * @param clinician   the clinician to assign to new events. May be {@code null}
     * @param location    the practice location
     * @param arrivalTime the arrival time
     * @param appointment the appointment. May be {@code null}
     * @param user        the author for acts
     * @param help        the help context
     */
    CheckInEditor(Party customer, Party patient, Entity schedule, User clinician, Party location,
                  Date arrivalTime, Act appointment, User user, HelpContext help) {
        service = ServiceHelper.getArchetypeService();
        this.customer = customer;
        this.patient = patient;
        this.schedule = schedule;
        scheduleBean = (schedule != null) ? service.getBean(schedule) : null;
        this.clinician = clinician;
        this.location = location;
        this.arrivalTime = arrivalTime;
        this.appointment = appointment;
        AppointmentRules appointmentRules = ServiceHelper.getBean(AppointmentRules.class);
        flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
        clinicalEventFactory = new ClinicalEventFactory(service, ServiceHelper.getBean(MedicalRecordRules.class),
                                                        appointmentRules);

        cageType = getCageType();

        context = new LocalContext();
        context.setCustomer(customer);
        context.setPatient(patient);
        context.setSchedule(schedule);
        context.setClinician(clinician);
        context.setLocation(location);
        context.setUser(user);

        layoutContext = new DefaultLayoutContext(context, help);
        layoutContext.setComponentFactory(new DefaultEditableComponentFactory(layoutContext));

        alerts = new MandatoryAlerts(context, help);
        clinicianProperty.setDisplayName(getDisplayName(ScheduleArchetypes.APPOINTMENT, "clinician", service));
        clinicianProperty.setArchetypeRange(new String[]{UserArchetypes.USER});
        patientProperty.setDisplayName(getDisplayName(ScheduleArchetypes.APPOINTMENT, "patient", service));
        patientProperty.setArchetypeRange(new String[]{PatientArchetypes.PATIENT});
        patientProperty.setRequired(true);

        if (selectWorkList()) {
            taskPanel = new TaskPanel(arrivalTime, appointment, layoutContext, service);
            enableSmartFlow = flowSheetServiceFactory.isSmartFlowSheetEnabled(location);
            boardingDays = (appointment != null) ? appointmentRules.getBoardingDays(appointment) : -1;
            taskPanel.setWorkListListener(this::onWorkListChanged);
        } else {
            taskPanel = null;
            enableSmartFlow = false;
            boardingDays = -1;
        }

        if (enableSmartFlow || getInputWeight()) {
            PatientRules rules = ServiceHelper.getBean(PatientRules.class);
            weightPanel = new WeightPanel(layoutContext, service, rules);
        } else {
            weightPanel = null;
        }

        if (patient != null) {
            patientProperty.setValue(patient.getObjectReference());
            updateEvent(patient);
        }
        if (clinician != null) {
            clinicianProperty.setValue(clinician.getObjectReference());
        }
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be {@code null} if the editor is invalid
     */
    public Party getPatient() {
        return patient;
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        patientProperty.setValue((patient != null) ? patient.getObjectReference() : patient);
    }

    public void setWorkList(Party worklist) {
        taskPanel.setWorkList(worklist);
    }

    /**
     * Returns the visit.
     *
     * @return an <em>act.patientClinicalEvent</em>. May be {@code null} if the editor is invalid
     */
    public Act getVisit() {
        return visit;
    }

    /**
     * Returns the clinician.
     *
     * @return the clinician. May be {@code null}
     */
    public User getClinician() {
        return clinician;
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician. May be {@code null}
     */
    public void setClinician(User clinician) {
        clinicianProperty.setValue((clinician != null) ? clinician.getObjectReference() : clinician);
    }

    /**
     * Returns the flow sheet info, if the patient is being checked in to Smart FloW Sheet.
     *
     * @return the flow sheet info
     */
    public FlowSheetInfo getFlowSheetInfo() {
        return (flowsheet != null && flowsheet.createFlowSheet()) ? new FlowSheetInfo(flowsheet.getDepartmentId(),
                                                                                      flowsheet.getExpectedStay(),
                                                                                      flowsheet.getTemplate()) : null;
    }

    /**
     * Returns the templates to print.
     *
     * @return the templates to print
     */
    public Collection<Entity> getTemplates() {
        return documents.getTemplates();
    }

    /**
     * Returns the weight act created by Check-In.
     *
     * @return the weight, or {@code null} if none was saved
     */
    public Act getWeight() {
        return weightPanel != null ? weightPanel.getWeight() : null;
    }

    /**
     * Returns the task created by Check-In.
     *
     * @return the task, or {@code null} if none was saved
     */
    public Act getTask() {
        return taskPanel != null ? taskPanel.getTask() : null;
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    @Override
    public Component getComponent() {
        if (component == null) {
            component = doLayout();
        }
        return component;
    }

    /**
     * Displays any mandatory alerts for the customer and patient.
     */
    public void showAlerts() {
        if (customer != null) {
            alerts.show(customer);
        }
        if (patient != null) {
            alerts.show(patient);
        }
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    @Override
    public boolean isModified() {
        return true;
    }

    public void setWeight(BigDecimal weight) {
        weightPanel.setWeight(weight);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = validator.validate(patientProperty) && validator.validate(clinicianProperty);
        if (valid) {
            valid = validateVisit(validator) && validateWeight(validator) && validateTask(validator)
                    && validateFlowSheet(validator);
        }
        return valid;
    }

    /**
     * Save any edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        // NOTE: any weight or task should have the same timestamp, or be dated after the event time
        // This is important for tasks, as a timestamp before an event could result in incorrect event selection
        // when consulting from a task.
        PlatformTransactionManager transactionManager = ServiceHelper.getBean(PlatformTransactionManager.class);
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                saveVisit();
                saveWeight();
                saveTask();
            }
        });
    }

    /**
     * Lays out the component.
     */
    protected Component doLayout() {
        FocusGroup focus = getFocusGroup();
        ComponentGrid grid = new ComponentGrid();

        // add the patient and customer
        patientReferenceEditor = new PatientReferenceEditor(patientProperty, null, layoutContext) {
            @Override
            protected Query<Party> createQuery(String name) {
                PatientQuery query = new PatientQuery(customer);
                query.setValue(name);
                return query;
            }
        };
        patientReferenceEditor.setAllowCreate(true);

        Label customerLabel = LabelFactory.text(getDisplayName(ScheduleArchetypes.APPOINTMENT, "customer", service));
        grid.add(LabelFactory.text(patientProperty.getDisplayName()), patientReferenceEditor.getComponent(),
                 customerLabel, LabelFactory.text(customer.getName()));
        focus.add(patientReferenceEditor.getFocusGroup());

        // add the clinician
        ClinicianReferenceEditor clinicianReferenceEditor = new ClinicianReferenceEditor(clinicianProperty, null,
                                                                                         layoutContext);

        grid.add(LabelFactory.text(clinicianProperty.getDisplayName()), clinicianReferenceEditor.getComponent());
        focus.add(clinicianReferenceEditor.getFocusGroup());

        // add the weight panel, if required
        if (weightPanel != null) {
            weightPanel.setPatient(patient);
            focus.add(weightPanel.layout(grid));
        }

        // add the task panel, if required
        if (taskPanel != null) {
            taskPanel.setPatient(patient);
            focus.add(taskPanel.layout(grid));
        }

        // add the Smart Flow Sheet panel, if required
        if (enableSmartFlow) {
            grid.add(TableHelper.createSpacer());
            grid.add(LabelFactory.create("workflow.checkin.smartflow", BOLD));
            int days = Math.max(boardingDays, 1);
            flowsheet = new FlowSheetPanel(flowSheetServiceFactory, location, -1, null, days, true, service);
            flowsheet.layout(grid, 4);
            flowsheet.setCreateFlowSheet(false);
            focus.add(flowsheet.getFocusGroup());
        }

        grid.add(TableHelper.createSpacer());
        grid.add(LabelFactory.create("workflow.checkin.print", BOLD));
        int maxResults = (enableSmartFlow) ? 7 : 10; // limit to 7 rows to avoid scroll bars when paging
        documents = new DocumentPanel(schedule, getWorkList(), layoutContext, maxResults);
        grid.add(documents.getComponent(), 4);
        focus.add(documents.getFocusGroup());

        patientProperty.addModifiableListener(modifiable -> onPatientChanged());
        clinicianProperty.addModifiableListener(modifiable -> onClinicianChanged());
        focus.setFocus();
        return grid.createGrid();
    }

    /**
     * Returns the patient editor.
     *
     * @return patient editor. May be {@code null}
     */
    PatientReferenceEditor getPatientReferenceEditor() {
        return patientReferenceEditor;
    }

    /**
     * Returns the work list editor.
     *
     * @return the work list editor. May be {@code null}
     */
    Editor getWorkListEditor() {
        return (taskPanel != null) ? taskPanel.getWorkListEditor() : null;
    }

    /**
     * Validates the visit.
     *
     * @param validator the validator
     * @return {@code true} if the visit is valid
     */
    private boolean validateVisit(Validator validator) {
        boolean valid = false;
        if (visit == null) {
            String message;
            if (StringUtils.isEmpty(visitError)) {
                message = Messages.format("property.error.required", getDisplayName(PatientArchetypes.CLINICAL_EVENT));
            } else {
                message = visitError;
            }
            validator.add(this, new ValidatorError(message));
        } else {
            valid = true;
        }
        return valid;
    }

    /**
     * Validates the task.
     *
     * @param validator the validator
     * @return {@code true} if the task is valid
     */
    private boolean validateTask(Validator validator) {
        return taskPanel == null || taskPanel.validate(validator);
    }

    /**
     * Validates weight.
     *
     * @param validator the validator
     * @return {@code true} if the weight is valid
     */
    private boolean validateWeight(Validator validator) {
        return weightPanel == null || weightPanel.validate(validator);
    }

    /**
     * Validates the flow sheet.
     *
     * @param validator the validator
     * @return {@code true} if the flow sheet is valid
     */
    private boolean validateFlowSheet(Validator validator) {
        boolean result = false;
        if (enableSmartFlow && flowsheet.createFlowSheet()) {
            if (weightPanel.isZero() || !weightPanel.isWeightCurrent()) {
                validator.add(this, new ValidatorError(Messages.get("workflow.checkin.weight.smartflow")));
            } else if (visit != null && !visit.isNew()) {
                PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
                PatientContext patientContext = factory.createContext(patient, visit, location);
                if (flowSheetServiceFactory.getHospitalizationService(location).exists(patientContext)) {
                    validator.add(this, new ValidatorError(
                            Messages.format("workflow.flowsheet.exists", patient.getName())));
                    flowsheet.setCreateFlowSheet(false);
                } else {
                    result = true;
                }
            } else {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Returns the cage type associated with the schedule.
     *
     * @return the cage type. May be {@code null}
     */
    private Entity getCageType() {
        return (scheduleBean != null) ? scheduleBean.getTarget("cageType", Entity.class, active()) : null;
    }

    /**
     * Determines if the patient weight should be input.
     *
     * @return {@code true} if the patient weight should be input
     */
    private boolean getInputWeight() {
        return scheduleBean != null && scheduleBean.getBoolean("inputWeight", true);
    }

    /**
     * Determines a work list should be selected.
     *
     * @return {@code true} if work-lists should be selected
     */
    private boolean selectWorkList() {
        boolean result = true;
        if (scheduleBean != null) {
            boolean useAllWorkLists = scheduleBean.getBoolean("useAllWorkLists", true);
            if (!useAllWorkLists) {
                result = !scheduleBean.getValues("workLists").isEmpty();
            }
        }
        return result;
    }

    /**
     * Returns the work list.
     *
     * @return the work list
     */
    private Entity getWorkList() {
        return taskPanel != null ? taskPanel.getWorkList() : null;
    }

    /**
     * Saves the <em>act.patientClinicalEvent</em>, if required.
     */
    private void saveVisit() {
        IMObjectBean appointmentBean = (appointment != null) ? service.getBean(appointment) : null;
        boolean patientChanged = false;
        if (appointmentBean != null) {
            patientChanged = !ObjectUtils.equals(patient.getObjectReference(),
                                                 appointmentBean.getTargetRef("patient"));
        }
        if (visit.isNew() || patientChanged) {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            List<Act> toSave = new ArrayList<>();
            if (visit.isNew()) {
                toSave.add(visit);
            }
            if (appointmentBean != null) {
                if (patientChanged) {
                    appointmentBean.setTarget("patient", patient);
                }
                if (visit.isNew()) {
                    Relationship relationship = appointmentBean.setTarget("event", visit);
                    visitBean.addValue("appointment", relationship);
                }
                toSave.add(appointment);
            }
            service.save(toSave);
        }
    }

    /**
     * Saves the <em>act.patientWeight</em>, if required.
     * <p/>
     * It will be linked to the event.
     */
    private void saveWeight() {
        if (weightPanel != null) {
            weightPanel.save(visit);
        }
    }

    /**
     * Saves the <em>act.customerTask</em>, if required.
     */
    private void saveTask() {
        if (taskPanel != null) {
            taskPanel.save();
        }
    }

    /**
     * Invoked when the patient changes.
     * <p/>
     * Updates the event and weight, and displays any mandatory alerts.
     */
    private void onPatientChanged() {
        patient = (Party) layoutContext.getCache().get(patientProperty.getReference());
        context.setPatient(patient);
        updateEvent(patient);
        if (weightPanel != null) {
            weightPanel.setPatient(patient);
        }
        if (taskPanel != null) {
            taskPanel.setPatient(patient);
        }
        if (patient != null) {
            alerts.show(patient);
        }
    }


    /**
     * Updates the event when the patient changes.
     *
     * @param patient the patient. May be {@code null}
     */
    private void updateEvent(Party patient) {
        if (patient != null) {
            try {
                String reason = (appointment != null) ? appointment.getReason() : null;
                boolean newEvent = cageType != null;  // require a new event if the schedule has a cage type
                visit = clinicalEventFactory.getEvent(arrivalTime, patient, clinician, appointment, reason, location,
                                                      newEvent);
                visitBean = (visit.isNew()) ? service.getBean(visit) : null;
                visitError = null;
            } catch (IllegalStateException exception) {
                visit = null;
                visitBean = null;
                visitError = exception.getMessage();
            }
        } else {
            visit = null;
            visitBean = null;
        }
    }

    /**
     * Invoked when the work list changes.
     */
    private void onWorkListChanged() {
        Entity worklist = taskPanel.getWorkList();
        Component focus = FocusHelper.getFocus();
        if (enableSmartFlow) {
            if (worklist != null) {
                flowsheet.setWorkList(worklist);
                int expectedHospitalStay = flowsheet.getExpectedStay();
                int days = Math.max(boardingDays, expectedHospitalStay);
                if (days != expectedHospitalStay) {
                    flowsheet.setExpectedStay(days);
                }
            } else {
                flowsheet.setCreateFlowSheet(false);
            }
        }
        documents.setWorkList(worklist);
        if (focus != null) {
            // document panel moves the focus, so put it back
            FocusHelper.setFocus(focus);
        }

    }

    /**
     * Invoked when the clinician changes.
     * <p/>
     * Updates the weight and task, and the event, if it is new.
     */
    private void onClinicianChanged() {
        clinician = (User) layoutContext.getCache().get(clinicianProperty.getReference());
        if (visit != null && visit.isNew()) {
            visitBean.setTarget("clinician", clinician);
        }
        if (taskPanel != null) {
            taskPanel.setClinician(clinician);
        }
        if (weightPanel != null) {
            weightPanel.setClinician(clinician);
        }
    }

}
