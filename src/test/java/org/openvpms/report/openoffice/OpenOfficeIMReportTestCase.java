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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.openoffice;

import org.apache.commons.jxpath.Functions;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ParameterType;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * {@link OpenOfficeIMReport} test case.
 *
 * @author Tim Anderson
 */
public class OpenOfficeIMReportTestCase extends AbstractOpenOfficeDocumentTest {

    /**
     * Tests reporting.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testReport() throws IOException {
        Document doc = getDocument("src/test/reports/act.customerEstimation.odt", DocFormats.ODT_TYPE);

        Functions functions = applicationContext.getBean(Functions.class);
        IMReport<IMObject> report = new OpenOfficeIMReport<IMObject>(doc, getArchetypeService(), getLookupService(),
                                                                     getHandlers(), functions);
        Map<String, Object> fields = new HashMap<String, Object>();
        Party practice = (Party) create(PracticeArchetypes.PRACTICE);
        practice.setName("Vets R Us");
        fields.put("OpenVPMS.practice", practice);

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        Date startTime = Date.valueOf("2006-08-04");
        act.setValue("startTime", startTime);
        act.setValue("lowTotal", new BigDecimal("100"));
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
        Document result = report.generate(objects, null, fields, DocFormats.ODT_TYPE);
        Map<String, String> userFields = getUserFields(result);
        String expectedStartTime = DateFormat.getDateInstance(DateFormat.MEDIUM).format(startTime);
        assertEquals(expectedStartTime, userFields.get("startTime"));
        assertEquals("$100.00", userFields.get("lowTotal"));
        assertEquals("J", userFields.get("firstName"));
        assertEquals("Zoo", userFields.get("lastName"));
        assertEquals("2.00", userFields.get("expression"));
        assertEquals("1234 Foo St\nMelbourne VIC 3001",
                     userFields.get("address"));
        assertEquals("Vets R Us", userFields.get("practiceName"));
        assertEquals("Invalid property name: invalid", userFields.get("invalid"));
    }

    /**
     * Verfies that input fields are returned as parameters, and that
     * by specifying it as a parameter updates the corresponding input field.
     */
    @Test
    public void testParameters() {
        Document doc = getDocument("src/test/reports/act.customerEstimation.odt", DocFormats.ODT_TYPE);

        Functions functions = applicationContext.getBean(Functions.class);
        IMReport<IMObject> report = new OpenOfficeIMReport<IMObject>(doc, getArchetypeService(), getLookupService(),
                                                                     getHandlers(), functions);

        Set<ParameterType> parameterTypes = report.getParameterTypes();
        Map<String, ParameterType> types = new HashMap<String, ParameterType>();
        for (ParameterType type : parameterTypes) {
            types.put(type.getName(), type);
        }
        assertTrue(types.containsKey("inputField1"));
        assertTrue(types.containsKey("IsEmail"));
        assertTrue(report.hasParameter("inputField1"));
        assertTrue(report.hasParameter("IsEmail"));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inputField1", "the input value");
        parameters.put("IsEmail", "true");

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
        Document result = report.generate(objects, parameters, null, DocFormats.ODT_TYPE);

        Map<String, String> inputFields = getInputFields(result);
        assertEquals("the input value", inputFields.get("inputField1"));

        Map<String, String> userFields = getUserFields(result);
        assertEquals("true", userFields.get("IsEmail"));
    }

}
