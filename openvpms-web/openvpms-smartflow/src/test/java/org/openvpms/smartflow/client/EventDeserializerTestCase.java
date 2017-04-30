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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.smartflow.model.Client;
import org.openvpms.smartflow.model.Hospitalization;
import org.openvpms.smartflow.model.Hospitalizations;
import org.openvpms.smartflow.model.Patient;
import org.openvpms.smartflow.model.event.DischargeEvent;
import org.openvpms.smartflow.model.event.Event;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link EventDeserializer}.
 *
 * @author Tim Anderson
 */
public class EventDeserializerTestCase {

    /**
     * Verifies a {@link DischargeEvent} can be deserialized.
     *
     * @throws Exception for any error
     */
    @Test
    public void testDischargeEvent() throws Exception {
        String reportPath
                = "https://sfstestcontent.blob.core.windows.net/flowsheetreports/c343deec-27ff-45ed-b92c-7a75c5757826";
        String json = "{\"clinicApiKey\":\"51a6f8ddcd6516d9ec055689a35ac775f4d9f2a6\"," +
                      "\"eventType\":\"hospitalizations.discharged\",\"object\":" +
                      "{\"objectType\":\"hospitalizations\",\"hospitalizations\":" +
                      "[{\"objectType\":\"hospitalization\"," +
                      "\"hospitalizationGuid\":\"c343deec-27ff-45ed-b92c-7a75c5757826\"," +
                      "\"hospitalizationId\":\"388\",\"departmentId\":0,\"dateMovedToDepartment\":null," +
                      "\"dateCreated\":\"2016-07-11T03:09:53.000+00:00\",\"treatmentTemplateName\":\"Addinson's\"," +
                      "\"temperatureUnits\":\"F\",\"weightUnits\":\"kg\",\"weight\":5.1,\"estimatedDaysOfStay\":2," +
                      "\"fileNumber\":\"1275\",\"cageNumber\":null,\"color\":null,\"dnr\":null,\"caution\":false," +
                      "\"doctorName\":null,\"medicId\":null,\"diseases\":[\"Checkup\"],\"" +
                      "reportPath\":\"" + reportPath + "\"," +
                      "\"status\":\"discharged\",\"patient\":{\"objectType\":\"patient\",\"patientId\":\"1275\"," +
                      "\"name\":\"Fido\",\"birthday\":null,\"species\":\"Canine\"," +
                      "\"owner\":{\"objectType\":\"client\",\"ownerId\":\"8\",\"nameLast\":\"Bourke\"," +
                      "\"nameFirst\":\"Judith\",\"homePhone\":\"(07) 34561234\",\"workPhone\":null},\"color\":null," +
                      "\"sex\":\"M\",\"breed\":\"Alaskan Malamute\",\"criticalNotes\":null,\"customField\":null," +
                      "\"imagePath\":null}}],\"id\":\"d84f930f-94b2-4b90-93fc-db5073ce9f7f\"}}";
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
        Event event = mapper.readValue(json, Event.class);
        assertTrue(event instanceof DischargeEvent);
        DischargeEvent dischargeEvent = (DischargeEvent) event;
        Hospitalizations list = dischargeEvent.getObject();
        assertNotNull(list);
        assertEquals(1, list.getHospitalizations().size());
        Hospitalization hospitalization = list.getHospitalizations().get(0);

        assertEquals("hospitalization", hospitalization.getObjectType());
        assertEquals("388", hospitalization.getHospitalizationId());
        assertEquals(0, hospitalization.getDepartmentId());
        assertEquals("c343deec-27ff-45ed-b92c-7a75c5757826", hospitalization.getHospitalizationGuid());
        assertEquals(TestHelper.getDatetime("2016-07-11 13:09:53"), hospitalization.getDateCreated());
        assertEquals("Addinson's", hospitalization.getTreatmentTemplateName());
        assertEquals("F", hospitalization.getTemperatureUnits());
        assertEquals(5.1, hospitalization.getWeight(), 0);
        assertEquals("kg", hospitalization.getWeightUnits());
        assertEquals(2, hospitalization.getEstimatedDaysOfStay());
        assertEquals("1275", hospitalization.getFileNumber());
        assertFalse(hospitalization.getCaution());
        assertNull(hospitalization.getDoctorName());
        assertNull(hospitalization.getMedicId());
        assertEquals(new String[]{"Checkup"}[0], hospitalization.getDiseases()[0]);
        assertNull(hospitalization.getCageNumber());
        assertNull(hospitalization.getColor());
        assertEquals(reportPath, hospitalization.getReportPath());
        assertEquals("discharged", hospitalization.getStatus());
        Patient patient = hospitalization.getPatient();
        assertNotNull(patient);
        assertEquals("patient", patient.getObjectType());
        assertEquals("1275", patient.getPatientId());
        assertEquals("Fido", patient.getName());
        assertNull(patient.getBirthday());
        assertEquals("M", patient.getSex());
        assertEquals("Canine", patient.getSpecies());
        assertEquals("Alaskan Malamute", patient.getBreed());
        assertNull(patient.getColor());
        assertNull(patient.getCriticalNotes());
        assertNull(patient.getCustomField());
        assertNull(patient.getImagePath());
        Client owner = patient.getOwner();
        assertNotNull(owner);
        assertEquals("client", owner.getObjectType());
        assertEquals("8", owner.getOwnerId());
        assertEquals("Judith", owner.getNameFirst());
        assertEquals("Bourke", owner.getNameLast());
        assertEquals("(07) 34561234", owner.getHomePhone());
        assertNull(owner.getWorkPhone());
    }
}
