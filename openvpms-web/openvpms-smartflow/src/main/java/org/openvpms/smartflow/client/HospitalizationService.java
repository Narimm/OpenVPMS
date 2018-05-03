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

package org.openvpms.smartflow.client;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActIdentity;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.i18n.Message;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Anesthetic;
import org.openvpms.smartflow.model.Anesthetics;
import org.openvpms.smartflow.model.Client;
import org.openvpms.smartflow.model.Form;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Medic;
import org.openvpms.smartflow.model.Patient;
import org.openvpms.smartflow.service.Hospitalizations;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static org.openvpms.smartflow.client.MediaTypeHelper.APPLICATION_PDF;
import static org.openvpms.smartflow.client.MediaTypeHelper.APPLICATION_PDF_TYPE;
import static org.openvpms.smartflow.event.impl.SmartFlowSheetHelper.getObject;

/**
 * Smart Flow Sheet hospitalisations client.
 *
 * @author Tim Anderson
 */
public class HospitalizationService extends FlowSheetService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The medical record rules.
     */
    private final MedicalRecordRules rules;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(HospitalizationService.class);

    /**
     * Patient colour node.
     */
    private static final String COLOUR = "colour";

    /**
     * Smart Flow Sheet identity archetype.
     */
    private static final String IDENTITY_ARCHETYPE = "actIdentity.smartflowsheet";

    /**
     * Constructs a {@link HospitalizationService}.
     *
     * @param url          the Smart Flow Sheet URL
     * @param emrApiKey    the EMR API key
     * @param clinicApiKey the clinic API key
     * @param timeZone     the timezone. This determines how dates are serialized
     * @param service      the archetype service
     * @param lookups      the lookup service
     * @param handlers     the document handlers
     * @param rules        the medical record rules
     */
    public HospitalizationService(String url, String emrApiKey, String clinicApiKey, TimeZone timeZone,
                                  IArchetypeService service, ILookupService lookups,
                                  DocumentHandlers handlers, MedicalRecordRules rules) {
        super(url, emrApiKey, clinicApiKey, timeZone, log);
        this.service = service;
        this.lookups = lookups;
        this.handlers = handlers;
        this.rules = rules;
    }

    /**
     * Determines if a patient hospitalization exists.
     *
     * @param context the patient context
     * @return {@code true} if it exists
     * @throws FlowSheetException for any error
     */
    public boolean exists(PatientContext context) {
        return getHospitalization(context) != null;
    }

    /**
     * Returns the hospitalization for the specified patient context.
     *
     * @param context the patient context
     * @return the hospitalization, or {@code null} if none exists
     * @throws FlowSheetException for any error
     */
    public Hospitalization getHospitalization(final PatientContext context) {
        Call<Hospitalization, Hospitalizations> call = new Call<Hospitalization, Hospitalizations>() {
            @Override
            public Hospitalization call(Hospitalizations resource) throws Exception {
                Hospitalization result = null;
                try {
                    result = resource.get(Long.toString(context.getVisitId()));
                } catch (NotFoundException ignore) {
                    log.debug("No hospitalization found for id=" + context.getVisitId());
                }
                return result;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToGetHospitalization(context.getPatient());
            }
        };
        return call(Hospitalizations.class, call);
    }

    /**
     * Adds a hospitalization for a patient.
     * <p>
     * The patient should have a current weight.
     *
     * @param context      the patient context
     * @param stayDuration the estimated days of stay
     * @param departmentId the department identifier, or {@code -1} to use the default department
     * @param template     the treatment template name. May be {@code null}
     * @throws FlowSheetException if the patient cannot be added
     */
    public void add(final PatientContext context, int stayDuration, int departmentId, String template) {
        final Hospitalization hospitalization = new Hospitalization();
        Patient patient = createPatient(context);
        hospitalization.setPatient(patient);
        hospitalization.setFileNumber(patient.getPatientId());
        hospitalization.setEstimatedDaysOfStay(stayDuration);
        hospitalization.setCaution(context.isAggressive());
        if (departmentId != -1) {
            hospitalization.setDepartmentId(departmentId);
        }
        hospitalization.setTreatmentTemplateName(template);
        hospitalization.setHospitalizationId(Long.toString(context.getVisitId()));
        hospitalization.setDateCreated(context.getVisitStartTime());
        Weight weight = context.getWeight();
        if (weight != null) {
            if (weight.getUnits() == WeightUnits.KILOGRAMS || weight.getUnits() == WeightUnits.GRAMS) {
                hospitalization.setWeight(weight.toKilograms().doubleValue());
                hospitalization.setWeightUnits("kg");
            } else if (weight.getUnits() == WeightUnits.POUNDS) {
                hospitalization.setWeight(weight.getWeight().doubleValue());
                hospitalization.setWeightUnits("lbs");
            }
        }
        User clinician = context.getClinician();
        if (clinician != null) {
            hospitalization.setDoctorName(clinician.getName());
        }
        String reason = lookups.getName(context.getVisit(), "reason");
        if (reason != null) {
            String diseases[] = new String[]{reason};
            hospitalization.setDiseases(diseases);
        }

        Call<Void, Hospitalizations> call = new Call<Void, Hospitalizations>() {
            @Override
            public Void call(Hospitalizations resource) throws Exception {
                resource.add(hospitalization);
                return null;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToCreateFlowSheet(context.getPatient(), exception.getMessage());
            }
        };
        call(Hospitalizations.class, call);
    }

    /**
     * Returns the anaesthetics for a patient.
     *
     * @param patient the patient
     * @param visit   the patient visit
     * @return the anaesthetics
     * @throws FlowSheetException if the sheet cannot be retrieved
     */
    public Anesthetics getAnesthetics(final Party patient, Act visit) {
        final String hospitalizationId = Long.toString(visit.getId());
        Call<Anesthetics, Hospitalizations> call = new Call<Anesthetics, Hospitalizations>() {
            @Override
            public Anesthetics call(Hospitalizations resource) throws Exception {
                return resource.getAnesthetics(hospitalizationId);
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToGetAnaesthetics(patient);
            }
        };
        return call(Hospitalizations.class, call);
    }

    /**
     * Returns the forms for a patient.
     *
     * @param patient the patient
     * @param visit   the patient visit
     * @return the forms
     * @throws FlowSheetException if the forms cannot be retrieved
     */
    public List<Form> getForms(Party patient, Act visit) {
        final String hospitalizationId = Long.toString(visit.getId());

        Call<List<Form>, Hospitalizations> call = new Call<List<Form>, Hospitalizations>() {
            @Override
            public List<Form> call(Hospitalizations resource) throws Exception {
                return resource.getForms(hospitalizationId);
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToGetForms(patient);
            }
        };
        return call(Hospitalizations.class, call);
    }

    /**
     * Discharges the patient associated with a visit.
     *
     * @param patient the patient
     * @param visit   the visit
     * @throws FlowSheetException if the patient cannot be discharged
     */
    public void discharge(final Party patient, Act visit) {
        final String hospitalizationId = Long.toString(visit.getId());
        Call<Void, Hospitalizations> call = new Call<Void, Hospitalizations>() {
            @Override
            public Void call(Hospitalizations resource) throws Exception {
                resource.discharge(hospitalizationId, "");
                return null;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToDischargePatient(patient, exception.getMessage());
            }
        };
        call(Hospitalizations.class, call);
    }

    /**
     * Saves a flow sheet report associated with a patient visit, to the patient visit.
     *
     * @param context the patient context
     * @throws FlowSheetException for any error
     */
    public void saveFlowSheetReport(PatientContext context) {
        saveFlowSheetReport(context.getPatient(), context.getVisit(), context.getClinician());
    }

    /**
     * Saves a flow sheet report associated with a patient visit, to the patient visit.
     *
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician. May be {@code null}
     * @throws FlowSheetException for any error
     */
    public void saveFlowSheetReport(Party patient, Act visit, User clinician) {
        String name = FlowSheetMessages.reportFileName(FlowSheetMessages.flowSheetReportName());
        saveReport(name, patient, visit, clinician, Hospitalizations::getFlowSheetReport);
    }

    /**
     * Saves a medical record report associated with a patient visit, to the patient visit.
     *
     * @param context the patient context
     * @throws FlowSheetException for any error
     */
    public void saveMedicalRecords(PatientContext context) {
        saveMedicalRecordsReport(context.getPatient(), context.getVisit(), context.getClinician());
    }

    /**
     * Saves a medical record report associated with a patient visit, to the patient visit.
     *
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician. May be {@code null}
     * @throws FlowSheetException for any error
     */
    public void saveMedicalRecordsReport(Party patient, Act visit, User clinician) {
        String name = FlowSheetMessages.reportFileName(FlowSheetMessages.medicalRecordsReportName());
        saveReport(name, patient, visit, clinician, Hospitalizations::getMedicalRecordsReport);
    }

    /**
     * Saves a billing report associated with a patient visit, to the patient visit.
     *
     * @param context the patient context
     * @throws FlowSheetException for any error
     */
    public void saveBillingReport(PatientContext context) {
        saveBillingReport(context.getPatient(), context.getVisit(), context.getClinician());
    }

    /**
     * Saves a billing report associated with a patient visit, to the patient visit.
     *
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician. May be {@code null}
     * @throws FlowSheetException for any error
     */
    public void saveBillingReport(Party patient, Act visit, User clinician) {
        String name = FlowSheetMessages.reportFileName(FlowSheetMessages.billingReportName());
        saveReport(name, patient, visit, clinician, Hospitalizations::getBillingReport);
    }

    /**
     * Saves a notes report associated with a patient visit, to the patient visit.
     *
     * @param context the patient context
     * @throws FlowSheetException for any error
     */
    public void saveNotesReport(PatientContext context) {
        saveNotesReport(context.getPatient(), context.getVisit(), context.getClinician());
    }

    /**
     * Saves a notes report associated with a patient visit, to the patient visit.
     *
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician. May be {@code null}
     * @throws FlowSheetException for any error
     */
    public void saveNotesReport(Party patient, Act visit, User clinician) {
        String name = FlowSheetMessages.reportFileName(FlowSheetMessages.notesReportName());
        saveReport(name, patient, visit, clinician, Hospitalizations::getNotesReport);
    }

    /**
     * Saves all anaesthetic sheet report and associated anaesthetic records report for a patient.
     * <p>
     * If there are existing reports with the same surgery identifier associated with the patient visit, they will
     * be versioned.
     *
     * @param patient the patient
     * @param visit   the visit
     * @throws FlowSheetException for any error
     */
    public void saveAnestheticsReports(Party patient, Act visit) {
        Anesthetics anesthetics = getAnesthetics(patient, visit);
        List<Anesthetic> list = anesthetics.getAnesthetics();
        if (list != null) {
            for (Anesthetic anesthetic : list) {
                saveAnestheticReports(patient, visit, anesthetic);
            }
        }
    }

    /**
     * Saves an anaesthetic sheet report and associated anaesthetic records report for a patient.
     * <p>
     * If there are existing reports with the same surgery identifier associated with the patient visit, they will
     * be versioned.
     *
     * @param context    the patient context
     * @param anesthetic the anaesthetic sheet
     * @throws FlowSheetException for any error
     */
    public void saveAnestheticReports(PatientContext context, Anesthetic anesthetic) {
        saveAnestheticReports(context.getPatient(), context.getVisit(), anesthetic);
    }

    /**
     * Saves an anaesthetic sheet report and associated anaesthetic records report for a patient.
     * <p>
     * If there are existing reports with the same surgery identifier associated with the patient visit, they will
     * be versioned.
     *
     * @param patient the patient
     * @param visit   the patient visit
     * @throws FlowSheetException for any error
     */
    public void saveAnestheticReports(Party patient, Act visit, Anesthetic anesthetic) {
        javax.ws.rs.client.Client client = null;
        try {
            client = getClient();
            String identity = anesthetic.getSurgeryGuid();
            String reportPath = anesthetic.getReportPath();
            String recordsReportPath = anesthetic.getRecordsReportPath();
            boolean haveReport = !StringUtils.isEmpty(reportPath);
            boolean haveRecordsReport = !StringUtils.isEmpty(recordsReportPath);
            if (haveReport || haveRecordsReport) {
                User clinician = getClinician(anesthetic);
                if (haveReport) {
                    String name = FlowSheetMessages.reportFileName(FlowSheetMessages.anaestheticReportName());
                    saveAnaestheticReport(name, identity, patient, visit, clinician, reportPath, client);
                }
                if (haveRecordsReport) {
                    String name = FlowSheetMessages.reportFileName(FlowSheetMessages.anaestheticRecordsReportName());
                    saveAnaestheticReport(name, identity, patient, visit, clinician, recordsReportPath, client);
                }
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * Saves each of the finalised forms reports for a patient.
     *
     * @param patient   the patient
     * @param visit     the patient visit
     * @param clinician the clinician. May be {@code null}
     * @throws FlowSheetException for any error
     */
    public void saveFormsReports(Party patient, Act visit, User clinician) {
        for (Form form : getForms(patient, visit)) {
            if (!form.isDeleted() && form.isFinalized()) {
                saveFormReport(patient, visit, clinician, form);
            }
        }
    }

    /**
     * Saves a form report for patient.
     *
     * @param context the patient context
     * @param form    the form
     * @throws FlowSheetException for any error
     */
    public void saveFormReport(PatientContext context, Form form) {
        saveFormReport(context.getPatient(), context.getVisit(), context.getClinician(), form);
    }

    /**
     * Saves a form report for patient.
     *
     * @param patient   the patient
     * @param visit     the patient visit
     * @param clinician the clinician. May be {@code null}
     * @param form      the form
     * @throws FlowSheetException for any error
     */
    public void saveFormReport(Party patient, Act visit, User clinician, Form form) {
        ReportRetriever reportRetriever = (service, id) -> service.getFormReport(id, form.getFormGuid());
        saveReport(form.getTitle(), patient, visit, clinician, reportRetriever);
    }

    /**
     * Saves an anaesthetic report.
     *
     * @param name      the report name
     * @param identity  the Smart Flow Sheet identity to add to the report. May be {@code null}
     * @param patient   the patient
     * @param visit     the patient visit
     * @param clinician the clinician. May be {@code null}
     * @param path      the report path
     * @param client    the client
     * @throws FlowSheetException for any error
     */
    private void saveAnaestheticReport(String name, String identity, Party patient, Act visit, User clinician,
                                       String path, javax.ws.rs.client.Client client) {
        try {
            WebTarget target = client.target(path);
            Response response = target.request().headers(getHeaders()).get();
            saveReport(response, identity, name, patient, visit, clinician);
        } catch (FlowSheetException exception) {
            throw exception;
        } catch (NotAuthorizedException exception) {
            notAuthorised(exception);
        } catch (Exception exception) {
            checkSSL(exception);
            if (isAccessToDocumentDenied(exception)) {
                throw new AccessToDocumentDeniedException(
                        FlowSheetMessages.accessToDocumentDenied(name, exception.getMessage()));
            }
            throw new FlowSheetException(FlowSheetMessages.failedToDownloadPDF(patient, name), exception);
        }
    }

    /**
     * Saves a report to the patient history.
     * <p>
     * If an instance of the report is already present, it will be versioned.
     *
     * @param name      the report name
     * @param patient   the patient
     * @param visit     the patient visit
     * @param clinician the clinician. May be {@code null}
     * @param retriever the report retriever
     * @throws FlowSheetException for any error
     */
    private void saveReport(final String name, final Party patient, final Act visit, final User clinician,
                            final ReportRetriever retriever) {
        Call<Void, Hospitalizations> call = new Call<Void, Hospitalizations>() {
            @Override
            public Void call(Hospitalizations resource) throws Exception {
                final String id = Long.toString(visit.getId());
                Response response = retriever.getResponse(resource, id);
                saveReport(response, null, name, patient, visit, clinician);
                return null;
            }

            /**
             * Wraps an exception in a {@link FlowSheetException}, with an appropriate message.
             *
             * @param exception the exception
             * @return a new {@link FlowSheetException}
             */
            @Override
            public FlowSheetException failed(Exception exception) {
                FlowSheetException result;
                if (isAccessToDocumentDenied(exception)) {
                    result = new AccessToDocumentDeniedException(
                            FlowSheetMessages.accessToDocumentDenied(name, exception.getMessage()));
                } else {
                    result = super.failed(exception);
                }
                return result;
            }

            @Override
            public Message getMessage(Exception exception) {
                return FlowSheetMessages.failedToDownloadPDF(patient, name);
            }
        };
        call(Hospitalizations.class, call);
    }

    /**
     * Saves a report to the patient history.
     * <p>
     * If an instance of the report is already present, it will be versioned.
     *
     * @param response  the HTTP response
     * @param identity  the Smart Flow Sheet identity to add to the report. May be {@code null}
     * @param name      the report name
     * @param patient   the patient
     * @param visit     the patient visit
     * @param clinician the clinician. May be {@code null}     @throws FlowSheetException if the response is not a PDF
     * @throws IOException for any I/O error
     */
    private void saveReport(Response response, String identity, String name, Party patient, Act visit, User clinician)
            throws IOException {
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL
            && response.hasEntity() && MediaTypeHelper.isA(response.getMediaType(), APPLICATION_PDF_TYPE,
                                                           APPLICATION_OCTET_STREAM_TYPE)) {
            // some SFS pdfs are incorrectly classified as application/octet-stream
            try (InputStream stream = (InputStream) response.getEntity()) {
                String fileName = name + ".pdf";
                List<IMObject> objects = new ArrayList<>();
                DocumentAct act = getAttachment(fileName, identity, patient, visit, clinician);
                DocumentHandler documentHandler = handlers.find(fileName, APPLICATION_PDF);
                DocumentRules rules = new DocumentRules(service);
                Document document = documentHandler.create(fileName, stream, APPLICATION_PDF, -1);
                if (act.isNew()) {
                    ActBean visitBean = new ActBean(visit, service);
                    visitBean.addNodeRelationship("items", act);
                    objects.add(visit);
                }
                objects.addAll(rules.addDocument(act, document));
                service.save(objects);
            }
        } else {
            log.error("Failed to get " + name + " for hospitalizationId=" + visit.getId() + ", status="
                      + response.getStatus() + ", mediaType=" + response.getMediaType());
            throw new FlowSheetException(FlowSheetMessages.failedToDownloadPDF(patient, name));
        }
    }

    /**
     * Returns the most recent attachment with the specified name and identity, or creates a new one if none exists.
     *
     * @param name      the file name
     * @param identity  the act identity
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician. May be {@code null}
     * @return an attachment
     */
    private DocumentAct getAttachment(String name, String identity, Party patient, Act visit, User clinician) {

        DocumentAct act;
        identity = StringUtils.trimToNull(identity);
        if (identity != null) {
            act = rules.getAttachment(name, visit, IDENTITY_ARCHETYPE, identity);
        } else {
            act = rules.getAttachment(name, visit);
        }
        if (act == null) {
            act = (DocumentAct) service.create(PatientArchetypes.DOCUMENT_ATTACHMENT);
            if (identity != null) {
                ActIdentity id = (ActIdentity) service.create(IDENTITY_ARCHETYPE);
                id.setIdentity(identity);
                act.addIdentity(id);
            }
            ActBean bean = new ActBean(act, service);
            bean.setNodeParticipant("patient", patient);
            bean.setNodeParticipant("clinician", clinician);
        }
        return act;
    }

    /**
     * Determines if an exception is an {@link WebApplicationException} with a custom 465 http status code indicating
     * that access to documents have been denied.
     *
     * @param exception the exception
     * @return {@code true} if the exception indicates that access to documents have been denied
     */
    private boolean isAccessToDocumentDenied(Throwable exception) {
        return exception instanceof WebApplicationException
               && ((WebApplicationException) exception).getResponse().getStatus() == 465;
    }

    /**
     * Creates a Smart Flow Sheet patient from the patient context.
     *
     * @param context the patient context.
     * @return a new patient
     */
    private Patient createPatient(PatientContext context) {
        Patient result = new Patient();
        IMObjectBean bean = new IMObjectBean(context.getPatient(), service);
        result.setPatientId(Long.toString(context.getPatientId()));
        result.setName(context.getPatientFirstName());
        Date dateOfBirth = context.getDateOfBirth();
        if (dateOfBirth != null) {
            // need to add the timezone to the date of birth otherwise it is converted to UTC by SFS.
            dateOfBirth = getUTCDate(dateOfBirth);
        }
        result.setBirthday(dateOfBirth);
        result.setSpecies(context.getSpeciesName());
        result.setBreed(context.getBreedName());
        result.setColor(getColour(bean));
        result.setSex(getSex(context));
        StringBuilder criticalNotes = new StringBuilder();
        for (Act alert : context.getAllergies()) {
            String reason = alert.getReason();
            if (!StringUtils.isEmpty(reason)) {
                if (criticalNotes.length() != 0) {
                    criticalNotes.append('\n');
                }
                criticalNotes.append(reason);
            }
        }
        if (criticalNotes.length() != 0) {
            result.setCriticalNotes(criticalNotes.toString());
        }

        Party owner = context.getCustomer();
        if (owner != null) {
            result.setOwner(createClient(context));
        }
        return result;
    }

    /**
     * Converts a local date to UTC, ignoring its timezone. This should be used for absolute dates such as birthdays
     * which shouldn't change if the locale does.
     *
     * @param date the date
     * @return the converted date
     */
    private Date getUTCDate(Date date) {
        GregorianCalendar utc = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        GregorianCalendar local = new GregorianCalendar();
        local.setTime(date);
        utc.set(Calendar.YEAR, local.get(Calendar.YEAR));
        utc.set(Calendar.MONTH, local.get(Calendar.MONTH));
        utc.set(Calendar.DATE, local.get(Calendar.DATE));
        utc.set(Calendar.HOUR_OF_DAY, 0);
        utc.set(Calendar.MINUTE, 0);
        utc.set(Calendar.SECOND, 0);
        utc.set(Calendar.MILLISECOND, 0);
        return utc.getTime();
    }

    /**
     * Creates a new Smart Flow Sheet customer.
     *
     * @param context the patient context
     * @return a new customer
     */
    private Client createClient(PatientContext context) {
        Client result = new Client();
        IMObjectBean bean = new IMObjectBean(context.getCustomer(), service);
        result.setOwnerId(Long.toString(context.getCustomer().getId()));
        result.setNameFirst(bean.getString("firstName"));
        result.setNameLast(bean.getString("lastName"));
        result.setHomePhone(context.getHomePhone());
        result.setWorkPhone(context.getWorkPhone());
        return result;
    }

    /**
     * Returns the clinician associated with an anesthetic sheet.
     *
     * @param anesthetic the anesthetic
     * @return the clinician. May be {@code null}
     */
    private User getClinician(Anesthetic anesthetic) {
        Medic surgeon = anesthetic.getSurgeon();
        return (surgeon != null) ? (User) getObject(surgeon.getMedicId(), UserArchetypes.USER, service) : null;
    }

    /**
     * Returns the patient colour.
     * <p>
     * This allows for the fact that some practices use lookups for the colour node.
     *
     * @param bean the patient bean
     * @return the patient colour
     */
    private String getColour(IMObjectBean bean) {
        NodeDescriptor node = bean.getDescriptor(COLOUR);
        return (node.isLookup()) ? lookups.getName(bean.getObject(), COLOUR) : bean.getString(COLOUR);
    }

    /**
     * Returns the patient sex.
     *
     * @param context the patient context
     * @return the patient sex
     */
    private String getSex(PatientContext context) {
        String result;
        String sex = context.getPatientSex();
        boolean desexed = context.isDesexed();
        if ("FEMALE".equals(sex)) {
            result = desexed ? "FS" : "F";
        } else {
            result = desexed ? "MN" : "M";
        }
        return result;
    }

    /**
     * Retrieves documents from the {@link Hospitalizations} service.
     */
    private interface ReportRetriever {

        /**
         * Retrieves a document.
         *
         * @param service the service
         * @param id      the hospitalisation identifier
         * @return the document response
         */
        Response getResponse(Hospitalizations service, String id);
    }

}
