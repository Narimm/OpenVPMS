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


/**
 * {@link OpenOfficeIMReport} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OpenOfficeIMReportTestCase extends AbstractOpenOfficeTest {

    /**
     * Tests reporting.
     */
    public void testReport() throws IOException {
        Document doc = getDocument(
                "src/test/reports/act.customerEstimation.odt",
                DocFormats.ODT_TYPE);

        IMReport<IMObject> report = new OpenOfficeIMReport<IMObject>(
                doc, getHandlers());

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setValue("startTime", java.sql.Date.valueOf("2006-08-04"));
        act.setValue("lowTotal", new BigDecimal("100"));
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
        Document result = report.generate(objects.iterator(),
                                          DocFormats.ODT_TYPE);
        Map<String, String> fields = getFields(result);
        assertEquals("4/08/2006", fields.get("startTime"));  // @todo localise
        assertEquals("$100.00", fields.get("lowTotal"));
        assertEquals("J", fields.get("firstName"));
        assertEquals("Zoo", fields.get("lastName"));
        assertEquals("2.00", fields.get("expression"));
        assertEquals("1234 Foo St\nMelbourne VIC 3001",
                     fields.get("address"));
        assertEquals("Invalid node name: invalid", fields.get("invalid"));
    }

    /**
     * Verfies that input fields are returned as parameters, and that
     * by specifying it as a parameter updates the corresponding user field.
     */
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
        assertTrue(types.containsKey("inputUserField1"));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("inputUserField1", "the input value");

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
        Document result = report.generate(objects.iterator(),
                                          parameters,
                                          DocFormats.ODT_TYPE);
        Map<String, String> fields = getFields(result);

        assertEquals("the input value", fields.get("inputUserField1"));
    }

    /**
     * Returns the user fields in a document.
     *
     * @param document an OpenOffice document
     * @return a map of user field names and their corresponding values
     */
    private Map<String, String> getFields(Document document) {
        Map<String, String> fields = new HashMap<String, String>();
        OOConnectionPool pool = OpenOfficeHelper.getConnectionPool();
        OOConnection connection = pool.getConnection();
        try {
            OpenOfficeDocument doc = new OpenOfficeDocument(
                    document, connection, getHandlers());
            for (String name : doc.getUserFieldNames()) {
                fields.put(name, doc.getUserField(name));
            }
        } finally {
            OpenOfficeHelper.close(connection);
        }
        return fields;
    }

}
