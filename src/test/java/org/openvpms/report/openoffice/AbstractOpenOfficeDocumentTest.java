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

import static org.junit.Assert.*;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.ParameterType;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for {@link OpenOfficeDocument} tests.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractOpenOfficeDocumentTest extends AbstractOpenOfficeTest {

    /**
     * Returns the user fields in a document.
     *
     * @param document an OpenOffice document
     * @return a map of user field names and their corresponding values
     */
    protected Map<String, String> getUserFields(Document document) {
        Map<String, String> fields = new HashMap<String, String>();
        OOConnection connection = getConnection();
        try {
            OpenOfficeDocument doc = getDocument(document, connection);
            for (String name : doc.getUserFieldNames()) {
                fields.put(name, doc.getUserField(name));
            }
            doc.close();
        } finally {
            OpenOfficeHelper.close(connection);
        }
        return fields;
    }

    /**
     * Returns the input fields in a document.
     *
     * @param document an OpenOffice document
     * @return a map of user field names and their corresponding values
     */
    protected Map<String, String> getInputFields(Document document) {
        Map<String, String> fields = new HashMap<String, String>();
        OOConnection connection = getConnection();
        try {
            OpenOfficeDocument doc = getDocument(document, connection);
            for (String name : doc.getInputFields().keySet()) {
                fields.put(name, doc.getInputField(name));
            }
            doc.close();
        } finally {
            OpenOfficeHelper.close(connection);
        }
        return fields;
    }

    /**
     * Creates a new {@link OpenOfficeDocument} wrapping a {@link Document}.
     *
     * @param document   the document
     * @param connection the connection to use
     * @return a new OpenOffice document
     */
    protected OpenOfficeDocument getDocument(Document document,
                                             OOConnection connection) {
        return new OpenOfficeDocument(document, connection, getHandlers());
    }

    /**
     * Verifies that a user field can be updated.
     *
     * @param doc   the document
     * @param name  the user field name
     * @param value the value
     */
    protected void checkUpdateUserField(OpenOfficeDocument doc,
                                        String name, String value) {
        doc.setUserField(name, value);
        assertEquals(value, doc.getUserField(name));
    }

    /**
     * Verifies that an input field can be updated.
     *
     * @param doc   the document
     * @param name  the input field name
     * @param value the value
     */
    protected void checkUpdateInputField(OpenOfficeDocument doc,
                                         String name, String value) {
        doc.setInputField(name, value);
        assertEquals(value, doc.getInputField(name));
    }

    /**
     * Verifies that there is a parameter with the expected description.
     *
     * @param description the expected description
     * @param input       the input parameters
     * @return the parameter name
     */
    protected String checkParameter(String description,
                                    Map<String, ParameterType> input) {
        for (ParameterType param : input.values()) {
            if (description.equals(param.getDescription())) {
                assertEquals(String.class, param.getType());
                assertNull(param.getDefaultValue());
                return param.getName();
            }
        }
        fail("Parameter not found with description=" + description);
        return null;
    }

}
