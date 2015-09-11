package org.openvpms.smartflow.client;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentRules;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.math.WeightUnits;
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
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.smartflow.i18n.FlowSheetMessages;
import org.openvpms.smartflow.model.Client;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Patient;
import org.openvpms.smartflow.service.Hospitalizations;

import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.openvpms.smartflow.client.MediaTypeHelper.APPLICATION_PDF;

/**
 * Smart Flow Sheet hospitalisations client.
 *
 * @author Tim Anderson
 */
public class HospitalizationService {

    /**
     * The Smart Flow Sheet service root URL.
     */
    private final String url;

    /**
     * The EMR API key.
     */
    private final String emrApiKey;

    /**
     * The clinic API key.
     */
    private final String clinicApiKey;

    /**
     * The time zone.
     */
    private final TimeZone timeZone;

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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(HospitalizationService.class);

    /**
     * Patient colour node.
     */
    private static final String COLOUR = "colour";

    /**
     * Empty form.
     */
    private static final Form EMPTY_FORM = new Form();

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
     */
    public HospitalizationService(String url, String emrApiKey, String clinicApiKey, TimeZone timeZone,
                                  IArchetypeService service, ILookupService lookups,
                                  DocumentHandlers handlers) {
        this.url = url;
        this.emrApiKey = emrApiKey;
        this.clinicApiKey = clinicApiKey;
        this.timeZone = timeZone;
        this.service = service;
        this.lookups = lookups;
        this.handlers = handlers;
    }

    /**
     * Determines if a patient hospitalization exists.
     *
     * @param context the patient context
     * @return {@code true} if it exists
     */
    public boolean exists(PatientContext context) {
        return getHospitalization(context) != null;
    }

    /**
     * Returns the hospitalization for the specified patient context.
     *
     * @param context the patient context
     * @return the hospitalization, or {@code null} if none exists
     */
    public Hospitalization getHospitalization(PatientContext context) {
        Hospitalization result = null;
        javax.ws.rs.client.Client client = getClient();
        WebTarget target = client.target(url);
        try {
            Hospitalizations hospitalizations = getHospitalizations(target);
            try {
                result = hospitalizations.get(Long.toString(context.getVisitId()));
            } catch (NotFoundException ignore) {
                log.debug("No hospitalization found for id=" + context.getVisitId());
            }
        } catch (NotAuthorizedException exception) {
            notAuthorised(exception);
        } catch (Throwable exception) {
            checkSSL(exception);
            throw new FlowSheetException(FlowSheetMessages.failedToGetHospitalization(context.getPatient()), exception);
        } finally {
            client.close();
        }
        return result;
    }

    /**
     * Adds a hospitalization for a patient.
     * <p>
     * The patient should have a current weight.
     *
     * @param context the patient context
     */
    public void add(PatientContext context) {
        Hospitalization hospitalization = new Hospitalization();
        hospitalization.setPatient(createPatient(context));
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

        javax.ws.rs.client.Client client = getClient();
        try {
            WebTarget target = client.target(url);
            Hospitalizations hospitalizations = getHospitalizations(target);
            hospitalizations.add(hospitalization);
        } catch (NotAuthorizedException exception) {
            notAuthorised(exception);
        } catch (Throwable exception) {
            checkSSL(exception);
            throw new FlowSheetException(FlowSheetMessages.failedToCreateFlowSheet(context.getPatient()), exception);
        } finally {
            client.close();
        }
    }

    /**
     * Saves a medical record report associated with a patient visit, to the patient visit.
     *
     * @param name    the name to use for the file, excluding the extension
     * @param context the patient context
     */
    public void saveMedicalRecords(String name, PatientContext context) {
        saveReport(name, context, new ReportRetriever() {
            @Override
            public Response getResponse(Hospitalizations service, String id) {
                return service.getMedicalRecordsReport(id);
            }
        });
    }

    /**
     * Saves a inventory report associated with a patient visit, to the patient visit.
     *
     * @param name    the name to use for the file, excluding the extension
     * @param context the patient context
     */
    public void saveInventoryReport(String name, PatientContext context) {
        saveReport(name, context, new ReportRetriever() {
            @Override
            public Response getResponse(Hospitalizations service, String id) {
                return service.getInventoryReport(id);
            }
        });
    }

    /**
     * Saves a tech notes report associated with a patient visit, to the patient visit.
     *
     * @param name    the name to use for the file, excluding the extension
     * @param context the patient context
     */
    public void saveTechNotesReport(String name, PatientContext context) {
        saveReport(name, context, new ReportRetriever() {
            @Override
            public Response getResponse(Hospitalizations service, String id) {
                return service.getTechNotesReport(id);
            }
        });
    }

    /**
     * Saves a flow sheet report associated with a patient visit, to the patient visit.
     *
     * @param name    the name to use for the file, excluding the extension
     * @param context the patient context
     */
    public void saveFlowSheetReport(String name, PatientContext context) {
        saveReport(name, context, new ReportRetriever() {
            @Override
            public Response getResponse(Hospitalizations service, String id) {
                return service.getFlowSheetReport(id);
            }
        });
    }

    /**
     * Saves a report to the patient history.
     *
     * @param name      the report name
     * @param context   the patient context
     * @param retriever the report retriever
     */
    private void saveReport(String name, PatientContext context, ReportRetriever retriever) {
        String id = Long.toString(context.getVisitId());
        javax.ws.rs.client.Client client = getClient();
        try {
            WebTarget target = client.target(url);
            Hospitalizations hospitalizations = getHospitalizations(target);
            Response response = retriever.getResponse(hospitalizations, id);
            if (response.hasEntity() && MediaTypeHelper.isPDF(response.getMediaType())) {
                try (InputStream stream = (InputStream) response.getEntity()) {
                    String fileName = name + ".pdf";
                    DocumentHandler documentHandler = handlers.find(fileName, APPLICATION_PDF);
                    DocumentRules rules = new DocumentRules(service);
                    Document document = documentHandler.create(fileName, stream, APPLICATION_PDF, -1);
                    DocumentAct act = (DocumentAct) service.create(PatientArchetypes.DOCUMENT_ATTACHMENT);
                    ActBean bean = new ActBean(act, service);
                    if (context.getClinician() != null) {
                        bean.addNodeParticipation("clinician", context.getClinician());
                    }
                    Act visit = context.getVisit();
                    ActBean visitBean = new ActBean(visit, service);
                    visitBean.addNodeRelationship("items", act);
                    bean.addNodeParticipation("patient", context.getPatient());
                    List<IMObject> objects = rules.addDocument(act, document);
                    objects.add(act);
                    service.save(objects);
                } catch (NotAuthorizedException exception) {
                    notAuthorised(exception);
                } catch (Throwable exception) {
                    checkSSL(exception);
                    throw new FlowSheetException(FlowSheetMessages.failedToDownloadPDF(context.getPatient(), name),
                                                 exception);
                }
            } else {
                log.error("Failed to get " + name + " for hospitalizationId=" + id + ", status=" + response.getStatus()
                          + ", mediaType=" + response.getMediaType());
                throw new FlowSheetException(FlowSheetMessages.failedToDownloadPDF(context.getPatient(), name));
            }
        } finally {
            client.close();
        }
    }

    /**
     * Throws a {@link FlowSheetException} with an appropriate error message for a {@code NotAuthorizedException}.
     *
     * @param exception the original exception
     */
    private void notAuthorised(NotAuthorizedException exception) {
        log.error(exception, exception);
        throw new FlowSheetException(FlowSheetMessages.notAuthorised());
    }

    /**
     * Checks an exception for an SSLHandshakeException cause, and throws an {@link FlowSheetException} with appropriate
     * error message if it has one.
     *
     * @param exception the exception
     * @throws FlowSheetException if the exception has a SSLHandshakeException cause
     */
    private void checkSSL(Throwable exception) {
        if (exception.getCause() instanceof SSLHandshakeException) {
            log.error(exception, exception);
            throw new FlowSheetException(FlowSheetMessages.cannotConnectUsingSSL(url));
        }
    }

    /**
     * Creates a JAX-RS client.
     *
     * @return a new JAX-RS client
     */
    private javax.ws.rs.client.Client getClient() {
        ObjectMapperContextResolver resolver = new ObjectMapperContextResolver(timeZone);
        ClientConfig config = new ClientConfig()
                .register(resolver)
                .register(JacksonFeature.class)
                .register(new ErrorResponseFilter(resolver.getContext(Object.class)));
        javax.ws.rs.client.Client resource = ClientBuilder.newClient(config);
        if (log.isDebugEnabled()) {
            resource.register(new LoggingFilter(new DebugLog(log), true));
        }
        return resource;
    }

    /**
     * Creates a new {@link Hospitalizations} proxy for the specified target.
     *
     * @param target the target
     * @return a new proxy
     */
    private Hospitalizations getHospitalizations(WebTarget target) {
        MultivaluedMap<String, Object> header = new MultivaluedHashMap<>();
        header.add("emrApiKey", emrApiKey);
        header.add("clinicApiKey", clinicApiKey);
        header.add("timezoneName", timeZone.getID());

        return WebResourceFactory.newResource(Hospitalizations.class, target, false, header,
                                              Collections.<Cookie>emptyList(), EMPTY_FORM);
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

    /**
     * Workaround to allow JAX-RS logging to be delegated to log4j.
     */
    private static final class DebugLog extends Logger {

        private final Log log;

        protected DebugLog(Log log) {
            super(GLOBAL_LOGGER_NAME, null);
            this.log = log;
        }

        @Override
        public void info(String msg) {
            log.debug(msg);
        }

    }
}
