package org.openvpms.smartflow.client;

import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
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
import java.math.BigDecimal;
import java.util.Collections;

/**
 * .
 *
 * @author Tim Anderson
 */
public class SmartFlowSheet {

    private static final String COLOUR = "colour";
    private static final Form EMPTY_FORM = new Form();
    private final String uri;

    private final IArchetypeService service;

    private final ILookupService lookups;

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
     * Adds a patient to Smart Flow Sheet.
     * <p>
     * The patient should have a current weight.
     *
     * @param context the patient context
     */
    public void addPatient(PatientContext context) {
        ClientConfig config = new ClientConfig().register(JacksonFeature.class);
        javax.ws.rs.client.Client resource = ClientBuilder.newClient(config);
        MultivaluedMap<String, Object> header = new MultivaluedHashMap<>();
        header.add("emrApiKey", "emr-api-key-received-from-sfs");
        header.add("clinicApiKey", "clinic-api-key-taken-from-account-web-page");

        Hospitalization hospitalization = new Hospitalization();
        hospitalization.setPatient(createPatient(context));
        hospitalization.setHospitalizationId(Long.toString(context.getVisitId()));
        hospitalization.setDateCreated(context.getVisitStartTime());
        BigDecimal weight = context.getPatientWeight();
        if (weight == null) {
            weight = BigDecimal.ZERO;
        }
        hospitalization.setWeight(weight.doubleValue());
        User clinician = context.getClinician();
        if (clinician != null) {
            hospitalization.setDoctorName(clinician.getName());
        }

        WebTarget target = resource.target(uri);
        try {

            Hospitalizations hospitalizations = WebResourceFactory.newResource(Hospitalizations.class,
                                                                               target, false,
                                                                               header, Collections.<Cookie>emptyList(),
                                                                               EMPTY_FORM);
            hospitalizations.add(hospitalization);
        } finally {
            resource.close();
        }
    }

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

    private String getColour(IMObjectBean bean) {
        NodeDescriptor node = bean.getDescriptor(COLOUR);
        return (node.isLookup()) ? lookups.getName(bean.getObject(), COLOUR) : bean.getString(COLOUR);
    }

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

}
