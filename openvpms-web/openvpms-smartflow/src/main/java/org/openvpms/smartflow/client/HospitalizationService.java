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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.component.business.domain.im.act.Act;
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
import org.openvpms.component.system.common.i18n.Message;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Client;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Patient;
import org.openvpms.smartflow.service.Hospitalizations;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import static org.openvpms.smartflow.client.MediaTypeHelper.APPLICATION_PDF;

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
            public Message failed(Exception exception) {
                return FlowSheetMessages.failedToGetHospitalization(context.getPatient());
            }
        };
        return call(Hospitalizations.class, call);
    }

    /**
     * Adds a hospitalization for a patient.
     * <p/>
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
            public Message failed(Exception exception) {
                return FlowSheetMessages.failedToCreateFlowSheet(context.getPatient());
            }
        };
        call(Hospitalizations.class, call);
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
            public Message failed(Exception exception) {
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
        saveReport(name, patient, visit, clinician, new ReportRetriever() {
            @Override
            public Response getResponse(Hospitalizations service, String id) {
                return service.getFlowSheetReport(id);
            }
        });
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
        saveReport(name, patient, visit, clinician, new ReportRetriever() {
            @Override
            public Response getResponse(Hospitalizations service, String id) {
                return service.getMedicalRecordsReport(id);
            }
        });
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
        saveReport(name, patient, visit, clinician, new ReportRetriever() {
            @Override
            public Response getResponse(Hospitalizations service, String id) {
                return service.getBillingReport(id);
            }
        });
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
        saveReport(name, patient, visit, clinician, new ReportRetriever() {
            @Override
            public Response getResponse(Hospitalizations service, String id) {
                return service.getNotesReport(id);
            }
        });
    }

    /**
     * Saves a report to the patient history.
     * <p/>
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
        final String id = Long.toString(visit.getId());
        Call<Void, Hospitalizations> call = new Call<Void, Hospitalizations>() {
            @Override
            public Void call(Hospitalizations resource) throws Exception {
                Response response = retriever.getResponse(resource, id);
                if (response.hasEntity() && MediaTypeHelper.isPDF(response.getMediaType())) {
                    try (InputStream stream = (InputStream) response.getEntity()) {
                        String fileName = name + ".pdf";
                        List<IMObject> objects = new ArrayList<>();
                        DocumentAct act = getAttachment(fileName, patient, visit, clinician);
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
                    log.error("Failed to get " + name + " for hospitalizationId=" + id + ", status="
                              + response.getStatus() + ", mediaType=" + response.getMediaType());
                    throw new FlowSheetException(FlowSheetMessages.failedToDownloadPDF(patient, name));
                }
                return null;
            }

            @Override
            public Message failed(Exception exception) {
                return FlowSheetMessages.failedToDownloadPDF(patient, name);
            }
        };
        call(Hospitalizations.class, call);
    }

    /**
     * Returns the most recent attachment with the specified, name, or creates a new one if none exists.
     *
     * @param name      the file name
     * @param patient   the patient
     * @param visit     the visit
     * @param clinician the clinician. May be {@code null}
     * @return an attachment
     */
    private DocumentAct getAttachment(String name, Party patient, Act visit, User clinician) {
        DocumentAct act = rules.getAttachment(name, visit);
        if (act == null) {
            act = (DocumentAct) service.create(PatientArchetypes.DOCUMENT_ATTACHMENT);
            ActBean bean = new ActBean(act, service);
            bean.setNodeParticipant("patient", patient);
            bean.setNodeParticipant("clinician", clinician);
        }
        return act;
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
     * Returns the patient colour.
     * <p/>
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
