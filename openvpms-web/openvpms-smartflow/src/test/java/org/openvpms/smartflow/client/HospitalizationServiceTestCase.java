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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.smartflow.model.Client;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.HttpHeaders;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link HospitalizationService} class.
 *
 * @author Tim Anderson
 */
public class HospitalizationServiceTestCase extends ArchetypeServiceTest {

    /**
     * Sets up a WireMock service.
     */
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(Options.DYNAMIC_PORT);

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
     * The patient date of birth.
     */
    private Date dateOfBirth;

    /**
     * The visit start time.
     */
    private Date startTime;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        dateOfBirth = TestHelper.getDate("2014-06-21");
        startTime = TestHelper.getDatetime("2015-08-25 12:51:01");
        Party customer = TestHelper.createCustomer("J", "Bloggs", false, true);
        customer.addContact(TestHelper.createPhoneContact("", "123456789"));
        Party patient = PatientTestHelper.createPatient("Fido", "CANINE", "KELPIE", dateOfBirth, customer);
        Party location = TestHelper.createLocation();
        Act weight = PatientTestHelper.createWeight(patient, new BigDecimal("5.1"), WeightUnits.KILOGRAMS);
        Act visit = PatientTestHelper.createEvent(startTime, patient, weight);
        save(visit);
        User clinician = TestHelper.createClinician();
        clinician.setName("Dr Seuss");
        MedicalRecordRules rules = new MedicalRecordRules(getArchetypeService());
        PatientContextFactory factory = new PatientContextFactory(patientRules, customerRules, rules,
                                                                  getArchetypeService(), getLookupService());
        context = factory.createContext(patient, customer, visit, location, clinician);
        context.getWeight();
        Lookup checkup = TestHelper.getLookup(ScheduleArchetypes.VISIT_REASON, "CHECKUP");
        context.getVisit().setReason(checkup.getCode());
        // now override the ids, to support result comparison.
        location.setId(1);
        clinician.setId(10);
        customer.setId(20);
        patient.setId(30);
        visit.setId(40);
    }

    /**
     * Tests the {@link HospitalizationService#exists(PatientContext)} method, when no hospitalization exists.
     */
    @Test
    public void testExistsForNoHospitalization() {
        stubFor(WireMock.post(urlEqualTo("/hospitalization/40"))
                        .willReturn(aResponse()
                                            .withStatus(404)));
        HospitalizationService client = createService();
        assertFalse(client.exists(context));
    }

    /**
     * Tests serialisation of {@link Hospitalization} instances via {@link HospitalizationService#add}.
     */
    @Test
    public void testAdd() {
        String expected = "{\"objectType\":\"hospitalization\",\"hospitalizationId\":\"40\",\"departmentId\":0,"
                          + "\"hospitalizationGuid\":null,\"dateCreated\":\"2015-08-25T12:51:01.000+10:00\","
                          + "\"treatmentTemplateName\":null,\"temperatureUnits\":null,\"weightUnits\":\"kg\","
                          + "\"weight\":5.1,\"estimatedDaysOfStay\":2,\"fileNumber\":\"30\",\"caution\":false,"
                          + "\"doctorName\":\"Dr Seuss\",\"medicId\":null,\"diseases\":[\"Checkup\"],"
                          + "\"cageNumber\":null,\"color\":null,\"reportPath\":null,\"status\":null,"
                          + "\"patient\":{\"objectType\":\"patient\",\"patientId\":\"30\",\"name\":\"Fido\","
                          + "\"birthday\":\"2014-06-21T10:00:00.000+10:00\",\"sex\":\"M\",\"species\":\"Canine\","
                          + "\"color\":null,\"breed\":\"Kelpie\",\"criticalNotes\":null,\"customField\":null,"
                          + "\"imagePath\":null,\"owner\":{\"objectType\":\"client\",\"ownerId\":\"20\","
                          + "\"nameLast\":\"Bloggs\",\"nameFirst\":\"J\","
                          + "\"homePhone\":\"123456789\",\"workPhone\":null}}}";
        stubFor(WireMock.post(urlEqualTo("/hospitalization"))
                        .willReturn(aResponse()
                                            .withStatus(201)
                                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                                            .withBody(expected)));

        HospitalizationService client = createService();
        client.add(context, 2, -1, null);

        List<LoggedRequest> requests = WireMock.findAll(postRequestedFor(urlEqualTo("/hospitalization")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        assertEquals(expected, request.getBodyAsString());
        assertEquals("foo", request.getHeader("emrApiKey"));
        assertEquals("bar", request.getHeader("clinicApiKey"));
    }

    /**
     * Verifies that a {@link BadRequestException} is thrown with appropriate message if a 400 status response is
     * returned. This simulates the response if a hospitalization is posted with the same id as an existing one.
     */
    @Test
    public void testAddDuplicateHospitalization() {
        stubFor(WireMock.post(urlEqualTo("/hospitalization"))
                        .willReturn(aResponse()
                                            .withStatus(400)
                                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                                            .withBody("{\"Message\":\"Hospitalization already exists\"}")));

        HospitalizationService client = createService();
        try {
            client.add(context, 2, -1, null);
            fail("Expected add() to fail");
        } catch (FlowSheetException exception) {
            assertEquals("SFS-0101: Failed to create Flow Sheet for Fido\n\nHospitalization already exists",
                         exception.getMessage());
            Throwable cause = exception.getCause();
            assertTrue(cause instanceof BadRequestException);
            assertEquals("Hospitalization already exists", cause.getMessage());
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
                                            .withHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                                            .withBody(body)));
        HospitalizationService client = createService();
        try {
            client.add(context, 2, -1, null);
            fail("Expected add() to fail");
        } catch (FlowSheetException exception) {
            assertEquals("SFS-0103: Failed to connect to Smart Flow Sheet.\n\n"
                         + "Check that the Smart Flow Sheet Clinic API Key is correct.", exception.getMessage());
        }
    }

    /**
     * Tests the {@link HospitalizationService#getHospitalization(PatientContext)} method.
     * <p/>
     * Verifies that any unknown properties returned in a Hospitalization, Patient or Client don't cause
     * deserialization to fail.
     */
    @Test
    public void testGetHospitalisation() {
        String response = "{\"objectType\":\"hospitalization\",\"hospitalizationId\":\"40\",\"departmentId\":0,"
                          + "\"hospitalizationGuid\":null,\"dateCreated\":\"2015-08-25T12:51:01.000+10:00\","
                          + "\"treatmentTemplateName\":null,\"temperatureUnits\":null,\"weightUnits\":\"kg\","
                          + "\"weight\":5.1,\"estimatedDaysOfStay\":2,\"fileNumber\":\"30\",\"caution\":false,"
                          + "\"doctorName\":\"Dr Seuss\",\"medicId\":null,\"diseases\":[\"CHECKUP\"],"
                          + "\"cageNumber\":null,\"color\":null,\"reportPath\":null,\"status\":null,"
                          + "\"newHospitalizationProperty\":\"foo\","
                          + "\"patient\":{\"objectType\":\"patient\",\"patientId\":\"30\",\"name\":\"Fido\","
                          + "\"birthday\":\"2014-06-21T00:00:00.000+10:00\",\"sex\":\"M\",\"species\":\"Canine\","
                          + "\"color\":null,\"breed\":\"Kelpie\",\"criticalNotes\":null,\"customField\":null,"
                          + "\"imagePath\":null,\"newPatientProperty\":\"foo\","
                          + "\"owner\":{\"objectType\":\"client\",\"ownerId\":\"20\","
                          + "\"nameLast\":\"Bloggs\",\"nameFirst\":\"J\","
                          + "\"homePhone\":\"123456789\",\"workPhone\":null,\"newClientProperty\":\"foo\"}}}";
        stubFor(WireMock.get(urlEqualTo("/hospitalization/40"))
                        .willReturn(aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json; charset=utf-8")
                                            .withBody(response)));

        HospitalizationService client = createService();
        Hospitalization result = client.getHospitalization(context);
        assertNotNull(result);
        assertEquals("hospitalization", result.getObjectType());
        assertEquals("40", result.getHospitalizationId());
        assertEquals(0, result.getDepartmentId());
        assertNull(result.getHospitalizationGuid());
        assertEquals(startTime, result.getDateCreated());
        assertNull(result.getTreatmentTemplateName());
        assertNull(result.getTemperatureUnits());
        assertEquals(5.1, result.getWeight(), 0);
        assertEquals("kg", result.getWeightUnits());
        assertEquals(2, result.getEstimatedDaysOfStay());
        assertEquals("30", result.getFileNumber());
        assertFalse(result.getCaution());
        assertEquals("Dr Seuss", result.getDoctorName());
        assertNull(result.getMedicId());
        assertEquals(new String[]{"CHECKUP"}[0], result.getDiseases()[0]);
        assertNull(result.getCageNumber());
        assertNull(result.getColor());
        assertNull(result.getReportPath());
        assertNull(result.getStatus());
        Patient patient = result.getPatient();
        assertNotNull(patient);
        assertEquals("patient", patient.getObjectType());
        assertEquals("30", patient.getPatientId());
        assertEquals("Fido", patient.getName());
        assertEquals(dateOfBirth, patient.getBirthday());
        assertEquals("M", patient.getSex());
        assertEquals("Canine", patient.getSpecies());
        assertEquals("Kelpie", patient.getBreed());
        assertNull(patient.getColor());
        assertNull(patient.getCriticalNotes());
        assertNull(patient.getCustomField());
        assertNull(patient.getImagePath());
        Client owner = patient.getOwner();
        assertNotNull(owner);
        assertEquals("client", owner.getObjectType());
        assertEquals("20", owner.getOwnerId());
        assertEquals("J", owner.getNameFirst());
        assertEquals("Bloggs", owner.getNameLast());
        assertEquals("123456789", owner.getHomePhone());
        assertNull(owner.getWorkPhone());
    }

    /**
     * Creates a new {@link HospitalizationService}.
     *
     * @return a new service
     */
    private HospitalizationService createService() {
        String url = "http://localhost:" + wireMockRule.port() + "/";
        return new HospitalizationService(url, "foo", "bar", TimeZone.getTimeZone("Australia/Sydney"),
                                          getArchetypeService(), getLookupService(),
                                          new DocumentHandlers(getArchetypeService()),
                                          new MedicalRecordRules(getArchetypeService()));
    }

}
