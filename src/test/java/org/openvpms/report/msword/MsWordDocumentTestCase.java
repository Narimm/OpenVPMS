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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;
import org.openvpms.report.openoffice.AbstractOpenOfficeDocumentTest;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeDocument;
import org.openvpms.report.openoffice.OpenOfficeHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Tests the {@link MsWordDocument} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MsWordDocumentTestCase extends AbstractOpenOfficeDocumentTest {

    /**
     * The OpenOffice connection.
     */
    private OOConnection connection;


    /**
     * Tests the {@link MsWordDocument#getUserFieldNames()},
     * {@link MsWordDocument#getUserField(String)}
     * and {@link MsWordDocument#setUserField(String, String)} methods.
     */
    @Test
    public void testUserFields() {
        OpenOfficeDocument doc = getDocument();
        Set<String> fields = new HashSet<String>();
        for (String name : doc.getUserFieldNames()) {
            fields.add(doc.getUserField(name));
        }
        String firstName = "customer.entity.firstName";
        String lastName = "customer.entity.lastName";
        String lowTotal = "lowTotal";
        String startTime = "startTime";
        String sum = "[1 + 1]";
        String billing = "[party:getBillingAddress(openvpms:get(., 'customer.entity'))]";
        String invalid = "invalid";
        assertTrue(fields.contains(firstName));
        assertTrue(fields.contains(lastName));
        assertTrue(fields.contains(lowTotal));
        assertTrue(fields.contains(startTime));
        assertTrue(fields.contains(sum));
        assertTrue(fields.contains(billing));
        assertTrue(fields.contains(invalid));

        checkUpdateUserField(doc, firstName, "Foo");
        checkUpdateUserField(doc, lastName, "Bar");
        checkUpdateUserField(doc, lowTotal, "1.0");
        checkUpdateUserField(doc, startTime, "1/1/2008");
        checkUpdateUserField(doc, sum, "2");
        checkUpdateUserField(doc, billing, "1000 Settlement Road Cowes");
        checkUpdateUserField(doc, invalid, "Still invalid");
    }

    /**
     * Tests the {@link MsWordDocument#getInputFields()},
     * {@link MsWordDocument#getInputField(String)} and
     * {@link MsWordDocument#setInputField(String, String)} methods.
     */
    @Test
    public void testInputFields() {
        OpenOfficeDocument doc = getDocument();
        Map<String, ParameterType> input = doc.getInputFields();
        assertEquals(3, input.size());
        String input1 = checkParameter("Enter Field 1", input);
        String input2 = checkParameter("Enter Field 2", input);
        String input3 = checkParameter("Enter Field 3", input);

        checkUpdateInputField(doc, input1, "input1 new value");
        checkUpdateInputField(doc, input2, "input2 new value");
        checkUpdateInputField(doc, input3, "input3 new value");
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        connection = getConnection();
    }

    /**
     * Tears down the test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
        OpenOfficeHelper.close(connection);
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
        return new MsWordDocument(document, connection, getHandlers());
    }

}
