package org.openvpms.smartflow.client;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.smartflow.model.Client;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Patient;
import org.openvpms.smartflow.service.Hospitalizations;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Smart Flow Sheet client.
 *
 * @author Tim Anderson
 */
public class SmartFlowSheet {

    /**
     * The Smart Flow Sheet service URI.
     */
    private final String uri;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SmartFlowSheet.class);

    /**
     * Patient colour node.
     */
    private static final String COLOUR = "colour";

    /**
     * Empty form.
     */
    private static final Form EMPTY_FORM = new Form();

    /**
     * Constructs a {@link SmartFlowSheet}.
     *
     * @param uri     the Smart Flow Sheet URI
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public SmartFlowSheet(String uri, IArchetypeService service, ILookupService lookups) {
        this.uri = uri;
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Determines if a patient hospitalization exists.
     *
     * @param context the patient context
     * @return {@code true} if it exists
     */
    public boolean exists(PatientContext context) {
        boolean result = false;
        javax.ws.rs.client.Client client = getClient();
        WebTarget target = client.target(uri);
        try {
            Hospitalizations hospitalizations = getHospitalizations(target);
            if (hospitalizations.get(Long.toString(context.getVisitId())) != null) {
                result = true;
            }
        } finally {
            client.close();
        }
        return result;
    }

    /**
     * Adds a patient to Smart Flow Sheet.
     * <p>
     * The patient should have a current weight.
     *
     * @param context the patient context
     */
    public void addPatient(PatientContext context) {
        Hospitalization hospitalization = new Hospitalization();
        hospitalization.setPatient(createPatient(context));
        hospitalization.setHospitalizationId(Long.toString(context.getVisitId()));
        hospitalization.setDateCreated(context.getVisitStartTime());
        Weight weight = context.getPatientWeight();
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
            WebTarget target = client.target(uri);
            Hospitalizations hospitalizations = getHospitalizations(target);
            hospitalizations.add(hospitalization);
        } finally {
            client.close();
        }
    }

    /**
     * Creates a JAX-RS client.
     *
     * @return a new JAX-RS client
     */
    private javax.ws.rs.client.Client getClient() {
        ClientConfig config = new ClientConfig()
                .register(JacksonFeature.class)
                .register(ClientErrorResponseFilter.class);
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
        header.add("emrApiKey", "873af17b2163255a3eb70a7d7413be152657bfab");
        header.add("clinicApiKey", "51a6f8ddcd6516d9ec055689a35ac775f4d9f2a6");

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
        result.setBirthday(context.getDateOfBirth());
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
