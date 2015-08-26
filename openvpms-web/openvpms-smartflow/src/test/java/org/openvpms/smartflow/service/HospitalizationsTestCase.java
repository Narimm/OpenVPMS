package org.openvpms.smartflow.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Rule;
import org.junit.Test;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.smartflow.client.SmartFlowSheet;
import org.openvpms.smartflow.model.Hospitalization;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;

/**
 * .
 *
 * @author Tim Anderson
 */
public class HospitalizationsTestCase extends ArchetypeServiceTest {


    @Autowired
    private PatientRules patientRules;

    @Autowired
    private CustomerRules customerRules;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    /**
     * Tests serialisation of {@link Hospitalization} instances via {@link SmartFlowSheet}.
     */
    @Test
    public void testAdd() {
        stubFor(WireMock.post(urlEqualTo("/hospitalization"))
                        .willReturn(aResponse()
                                            .withStatus(200)));
//                                            .withHeader("Content-Type", "application/json")
//                                            .withBody("<response>Some content</response>")));

        Date startTime = TestHelper.getDatetime("2015-08-25 12:51:01");
        Party patient = TestHelper.createPatient();
        patient.setName("Fido");
        Party customer = TestHelper.createCustomer("J", "Bloggs", true);
        customer.addContact(TestHelper.createPhoneContact("", "123456789"));
        Party location = TestHelper.createLocation();
        Act weight = PatientTestHelper.createWeight(patient, new BigDecimal("5.1"), WeightUnits.KILOGRAMS);
        Act visit = PatientTestHelper.createEvent(startTime, patient, weight);
        save(visit);
        User clinician = TestHelper.createClinician();
        SmartFlowSheet client = new SmartFlowSheet("http://mercury:8089/", getArchetypeService(), getLookupService());
        PatientContextFactory factory = new PatientContextFactory(patientRules, customerRules, getArchetypeService(), getLookupService());
        PatientContext context = factory.createContext(patient, customer, visit, location, clinician);
        client.addPatient(context);

        List<LoggedRequest> requests = WireMock.findAll(postRequestedFor(urlEqualTo("/hospitalization")));
        assertEquals(1, requests.size());
        System.out.println(requests.get(0).getBodyAsString());
    }

}
