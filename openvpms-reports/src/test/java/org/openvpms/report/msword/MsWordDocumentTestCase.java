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

package org.openvpms.report.msword;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;
import org.openvpms.report.openoffice.AbstractOpenOfficeTest;
import org.openvpms.report.openoffice.OpenOfficeDocument;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MsWordDocumentTestCase extends AbstractOpenOfficeTest {

    /**
     * Tests the {@link MsWordDocument#getUserFieldNames()},
     * {@link MsWordDocument#getUserField(String)}
     * and {@link MsWordDocument#setUserField(String, String)} methods.
     */
    public void testUserFields() {
        OpenOfficeDocument doc = getDocument();
        Map<String, String> fields = new LinkedHashMap<String, String>();
        for (String name : doc.getUserFieldNames()) {
            fields.put(name, doc.getUserField(name));
        }
        assertEquals("customer.entity.firstName", fields.get("userField1"));
        assertEquals("customer.entity.lastName", fields.get("userField2"));
        assertEquals("lowTotal", fields.get("userField3"));
        assertEquals("startTime", fields.get("userField4"));
        assertEquals("[1 + 1]", fields.get("userField5"));
        assertEquals(
                "[party:getBillingAddress(openvpms:get(., 'customer.entity'))]",
                fields.get("userField6"));
        assertEquals("invalid", fields.get("userField7"));

        checkUpdateUserField(doc, "userField1", "Foo");
        checkUpdateUserField(doc, "userField2", "Bar");
        checkUpdateUserField(doc, "userField3", "1.0");
        checkUpdateUserField(doc, "userField4", "1/1/2008");
        checkUpdateUserField(doc, "userField5", "2");
        checkUpdateUserField(doc, "userField6", "1000 Settlement Road Cowes");
        checkUpdateUserField(doc, "userField7", "Still invalid");
    }

    public void testInputFields() {
        OpenOfficeDocument doc = getDocument();
        Map<String, ParameterType> input = doc.getInputFields();
        assertEquals(3, input.size());
        checkParameter("inputField1", "Enter Field 1", input);
        checkParameter("inputField2", "Enter Field 3", input);
        checkParameter("inputField3", "Enter Field 2", input);

        checkUpdateInputField(doc, "inputField1", "input 1 new value");
        checkUpdateInputField(doc, "inputField2", "input 3 new value");
        checkUpdateInputField(doc, "inputField3", "input 2 new value");
    }

    private void checkUpdateUserField(OpenOfficeDocument doc,
                                      String name, String value) {
        doc.setUserField(name, value);
        assertEquals(value, doc.getUserField(name));
    }

    private void checkUpdateInputField(OpenOfficeDocument doc,
                                      String name, String value) {
        doc.setInputField(name, value);
        assertEquals(value, doc.getInputField(name));
    }

    private void checkParameter(String name, String description,
                                Map<String, ParameterType> input) {
        ParameterType param = input.get(name);
        assertNotNull(param);
        assertEquals(name, param.getName());
        assertEquals(String.class, param.getType());
        assertEquals(description, param.getDescription());
        assertNull(param.getDefaultValue());
    }

    /**
     * Loads the document to test agaisnt.
     *
     * @return the document
     */
    private OpenOfficeDocument getDocument() {
        Document document
                = getDocument("src/test/reports/act.customerEstimation.doc",
                              DocFormats.DOC_TYPE);
        return new MsWordDocument(document, getConnection(), getHandlers());
    }

}
