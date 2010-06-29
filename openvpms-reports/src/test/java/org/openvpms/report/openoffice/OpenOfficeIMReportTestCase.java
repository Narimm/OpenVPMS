/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.openoffice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ParameterType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.DateFormat;
import java.sql.Date;


/**
 * {@link OpenOfficeIMReport} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OpenOfficeIMReportTestCase extends AbstractOpenOfficeDocumentTest {

    /**
     * Tests reporting.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testReport() throws IOException {
        Document doc = getDocument(
                "src/test/reports/act.customerEstimation.odt",
                DocFormats.ODT_TYPE);

        IMReport<IMObject> report = new OpenOfficeIMReport<IMObject>(
                doc, getHandlers());

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        Date startTime = Date.valueOf("2006-08-04");
        act.setValue("startTime", startTime);
        act.setValue("lowTotal", new BigDecimal("100"));
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
        Document result = report.generate(objects.iterator(),
                                          DocFormats.ODT_TYPE);
        Map<String, String> fields = getUserFields(result);
        String expectedStartTime = DateFormat.getDateInstance(DateFormat.MEDIUM).format(startTime);
        assertEquals(expectedStartTime, fields.get("startTime"));
        assertEquals("$100.00", fields.get("lowTotal"));
        assertEquals("J", fields.get("firstName"));
        assertEquals("Zoo", fields.get("lastName"));
        assertEquals("2.00", fields.get("expression"));
        assertEquals("1234 Foo St\nMelbourne VIC 3001",
                     fields.get("address"));
        assertEquals("Invalid property name: invalid", fields.get("invalid"));
    }

    /**
     * Verfies that input fields are returned as parameters, and that
     * by specifying it as a parameter updates the corresponding input field.
     */
    @Test
    public void testParameters() {
        Document doc = getDocument(
                "src/test/reports/act.customerEstimation.odt",
                DocFormats.ODT_TYPE);

        IMReport<IMObject> report = new OpenOfficeIMReport<IMObject>(
                doc, getHandlers());

        Set<ParameterType> parameterTypes = report.getParameterTypes();
        Map<String, ParameterType> types = new HashMap<String, ParameterType>();
        for (ParameterType type : parameterTypes) {
            types.put(type.getName(), type);
        }
        assertTrue(types.containsKey("inputField1"));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inputField1", "the input value");

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
        Document result = report.generate(objects.iterator(),
                                          parameters,
                                          DocFormats.ODT_TYPE);

        Map<String, String> inputFields = getInputFields(result);
        assertEquals("the input value", inputFields.get("inputField1"));
    }

}
