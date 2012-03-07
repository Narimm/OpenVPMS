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
 *  $Id: OpenOfficeIMReportTestCase.java 1713 2007-01-11 06:21:23Z tanderson $
 */

package org.openvpms.report.msword;

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
import org.openvpms.report.openoffice.AbstractOpenOfficeDocumentTest;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeDocument;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * {@link MsWordIMReport} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-01-11 17:21:23 +1100 (Thu, 11 Jan 2007) $
 */
public class MsWordIMReportTestCase extends AbstractOpenOfficeDocumentTest {

    /**
     * Tests reporting.
     *
     * @throws IOException for any I/O error
     */
    @Test
    public void testReport() throws IOException {
        Document doc = getDocument("src/test/reports/act.customerEstimation.doc", DocFormats.DOC_TYPE);

        IMReport<IMObject> report = new MsWordIMReport<IMObject>(doc, getHandlers());
        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setValue("startTime", java.sql.Date.valueOf("2006-08-04"));
        act.setValue("lowTotal", new BigDecimal("100"));
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());

        // TODO:  Currently just do generate to make sure no exception.
        // result not used for merge testing until we can figure out how to
        // maintain fields in generated document.

        report.generate(objects.iterator(), DocFormats.DOC_TYPE);
    }

    /**
     * Verfies that input fields are returned as parameters, and that
     * by specifying it as a parameter updates the corresponding input field.
     */
    @Test
    public void testParameters() {
        Document doc = getDocument("src/test/reports/act.customerEstimation.doc", DocFormats.DOC_TYPE);

        IMReport<IMObject> report = new MsWordIMReport<IMObject>(doc, getHandlers());

        Set<ParameterType> parameterTypes = report.getParameterTypes();
        Map<String, ParameterType> types = new HashMap<String, ParameterType>();
        for (ParameterType type : parameterTypes) {
            types.put(type.getName(), type);
        }
        assertTrue(types.containsKey("Enter Field 1"));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("Enter Field 1", "the input value");

        Party party = createCustomer();
        ActBean act = createAct("act.customerEstimation");
        act.setParticipant("participation.customer", party);

        List<IMObject> objects = Arrays.asList((IMObject) act.getAct());
        Document result = report.generate(objects.iterator(),
                                          parameters,
                                          DocFormats.ODT_TYPE);

        Map<String, String> inputFields = getInputFields(result);
        assertEquals("the input value", inputFields.get("Enter Field 1"));
    }

    /**
     * Creates a new {@link OpenOfficeDocument} wrapping a {@link Document}.
     *
     * @param document   the document
     * @param connection the connection
     * @return a new OpenOffice document
     */
    @Override
    protected OpenOfficeDocument getDocument(Document document,
                                             OOConnection connection) {
        return new MsWordDocument(document, connection, getHandlers());
    }
}
