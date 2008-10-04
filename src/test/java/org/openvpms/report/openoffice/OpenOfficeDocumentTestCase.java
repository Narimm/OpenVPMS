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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.openoffice;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Tests the {@link OpenOfficeDocument} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeDocumentTestCase extends AbstractOpenOfficeDocumentTest {

    /**
     * Tests the {@link OpenOfficeDocument#getUserFieldNames()},
     * {@link OpenOfficeDocument#getUserField(String)}
     * and {@link OpenOfficeDocument#setUserField(String, String)} methods.
     */
    public void testUserFields() {
        OpenOfficeDocument doc = getDocument();
        Map<String, String> fields = new LinkedHashMap<String, String>();
        for (String name : doc.getUserFieldNames()) {
            fields.put(name, doc.getUserField(name));
        }
        assertEquals("customer.entity.firstName", fields.get("firstName"));
        assertEquals("customer.entity.lastName", fields.get("lastName"));
        assertEquals("lowTotal", fields.get("lowTotal"));
        assertEquals("startTime", fields.get("startTime"));
        assertEquals("[1 + 1]", fields.get("expression"));
        assertEquals(
                "[party:getBillingAddress(openvpms:get(., 'customer.entity'))]",
                fields.get("address"));
        assertEquals("invalid", fields.get("invalid"));

        checkUpdateUserField(doc, "firstName", "Foo");
        checkUpdateUserField(doc, "lastName", "Bar");
        checkUpdateUserField(doc, "lowTotal", "1.0");
        checkUpdateUserField(doc, "startTime", "1/1/2008");
        checkUpdateUserField(doc, "expression", "2");
        checkUpdateUserField(doc, "address", "1000 Settlement Road Cowes");
        checkUpdateUserField(doc, "invalid", "Still invalid");
    }

    /**
     * Tests the {@link OpenOfficeDocument#getInputFields()},
     * {@link OpenOfficeDocument#getInputField(String)} and
     * {@link OpenOfficeDocument#setInputField(String, String)} methods.
     */
    public void testInputFields() {
        OpenOfficeDocument doc = getDocument();
        Map<String, ParameterType> input = doc.getInputFields();
        assertEquals(3, input.size());
        String input1 = checkParameter("Enter input1 text", input);
        String input2 = checkParameter("Enter input2 text", input);
        String input3 = checkParameter("Enter input3 text", input);

        checkUpdateInputField(doc, input1, "input1 new value");
        checkUpdateInputField(doc, input2, "input2 new value");
        checkUpdateInputField(doc, input3, "input3 new value");
    }

    /**
     * Loads the document to test agaisnt.
     *
     * @return the document
     */
    private OpenOfficeDocument getDocument() {
        Document document
                = getDocument("src/test/reports/act.customerEstimation.odt",
                              DocFormats.ODT_TYPE);
        return new OpenOfficeDocument(document, getConnection(), getHandlers());
    }

}
