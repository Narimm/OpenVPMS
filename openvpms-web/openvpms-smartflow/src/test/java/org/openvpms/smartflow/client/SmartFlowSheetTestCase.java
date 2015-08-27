package org.openvpms.smartflow.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Before;
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
import org.openvpms.smartflow.model.Hospitalization;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAuthorizedException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the {@link SmartFlowSheet} class.
 *
 * @author Tim Anderson
 */
public class SmartFlowSheetTestCase extends ArchetypeServiceTest {

    /**
     * Sets up a WireMock service.
     */
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    /**
     * The patient rules.
     */
    @Autowired
    private PatientRules patientRules;

    /**
     * The customer rules.
     */
    @Autowired
    private CustomerRules customerRules;

    /**
     * The patient context.
     */
    private PatientContext context;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Date dateOfBirth = TestHelper.getDate("2014-06-21");
        Date startTime = TestHelper.getDatetime("2015-08-25 12:51:01");
        Party customer = TestHelper.createCustomer("J", "Bloggs", true);
        customer.addContact(TestHelper.createPhoneContact("", "123456789"));
        Party patient = PatientTestHelper.createPatient("Fido", "CANINE", "KELPIE", dateOfBirth, customer);
        Party location = TestHelper.createLocation();
        Act weight = PatientTestHelper.createWeight(patient, new BigDecimal("5.1"), WeightUnits.KILOGRAMS);
        Act visit = PatientTestHelper.createEvent(startTime, patient, weight);
        save(visit);
        User clinician = TestHelper.createClinician();
        clinician.setName("Dr Seuss");
        PatientContextFactory factory = new PatientContextFactory(patientRules, customerRules, getArchetypeService(),
                                                                  getLookupService());
        context = factory.createContext(patient, customer, visit, location, clinician);
        context.getPatientWeight();

        // now override the ids, to give predictable results.
        location.setId(1);
        clinician.setId(10);
        customer.setId(20);
        patient.setId(30);
        visit.setId(40);
    }

    /**
     * Tests serialisation of {@link Hospitalization} instances via {@link SmartFlowSheet#addPatient(PatientContext)}.
     */
    @Test
    public void testAddPatient() {
        String expected = "{\"objectType\":\"hospitalization\",\"hospitalizationId\":\"40\",\"departmentId\":0,"
                          + "\"hospitalizationGuid\":null,\"dateCreated\":\"2015-08-25T02:51:01.000Z\","
                          + "\"treatmentTemplateName\":null,\"temperatureUnits\":null,\"weightUnits\":\"kg\","
                          + "\"weight\":5.1,\"estimatedDaysOfStay\":0,\"fileNumber\":null,\"caution\":false,"
                          + "\"dnr\":false,\"doctorName\":\"Dr Seuss\",\"medicId\":null,\"diseases\":null,"
                          + "\"cageNumber\":null,\"color\":null,\"reportPath\":null,\"status\":null,"
                          + "\"patient\":{\"objectType\":\"patient\",\"patientId\":\"30\",\"name\":\"Fido\","
                          + "\"birthday\":\"2014-06-20T14:00:00.000Z\",\"sex\":\"M\",\"species\":\"Canine\","
                          + "\"color\":null,\"breed\":\"Kelpie\",\"criticalNotes\":null,\"customField\":null,"
                          + "\"imagePath\":null,\"owner\":{\"objectType\":\"client\",\"ownerId\":\"20\","
                          + "\"nameLast\":\"Bloggs\",\"nameFirst\":\"J\","
                          + "\"homePhone\":\"123456789\",\"workPhone\":null}}}";
        stubFor(WireMock.post(urlEqualTo("/hospitalization"))
                        .willReturn(aResponse()
                                            .withStatus(201)
                                            .withHeader("Content-Type", "application/json; charset=utf-8")
                                            .withBody(expected)));

        SmartFlowSheet client = new SmartFlowSheet("http://localhost:8089/", getArchetypeService(), getLookupService());
        client.addPatient(context);

        List<LoggedRequest> requests = WireMock.findAll(postRequestedFor(urlEqualTo("/hospitalization")));
        assertEquals(1, requests.size());
        assertEquals(expected, requests.get(0).getBodyAsString());
    }

    /**
     * Verifies that a {@link BadRequestException} is thrown with appropriate message if a 400 status response is
     * returned. This simulates the response if a hospitalization is posted with the same id as an existing one.
     */
    @Test
    public void testHospitalizationExists() {
        stubFor(WireMock.post(urlEqualTo("/hospitalization"))
                        .willReturn(aResponse()
                                            .withStatus(400)
                                            .withHeader("Content-Type", "application/json; charset=utf-8")
                                            .withBody("{\"Message\":\"Hospitalization already exists\"}")));

        SmartFlowSheet client = new SmartFlowSheet("http://localhost:8089/", getArchetypeService(), getLookupService());
        try {
            client.addPatient(context);
            fail("Expected addPatient() to fail");
        } catch (BadRequestException expected) {
            assertEquals("Hospitalization already exists", expected.getMessage());
        }
    }

    /**
     * Simulate an HTTP 401. This can occur when the emrKey or clinicKey is incorrect.
     */
    @Test
    public void testNotAuthorizedException() {
        String body = "{\"Message\":\"Authorization has been denied for this request\"}";
        stubFor(WireMock.post(urlEqualTo("/hospitalization"))
                        .willReturn(aResponse()
                                            .withStatus(401)
                                            .withHeader("Content-Type", "application/json; charset=utf-8")
                                            .withBody(body)));
        SmartFlowSheet client = new SmartFlowSheet("http://localhost:8089/", getArchetypeService(), getLookupService());
        try {
            client.addPatient(context);
            fail("Expected addPatient() to fail");
        } catch (NotAuthorizedException expected) {
            assertEquals("Authorization has been denied for this request", expected.getMessage());
        }
    }

}
