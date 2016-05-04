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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.AbstractReportTest;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class JRXMLDocumentHandlerTestCase extends AbstractReportTest {

    /**
     * The handler.
     */
    private JRXMLDocumentHandler handler;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        handler = new JRXMLDocumentHandler(getArchetypeService());
    }

    /**
     * Tests the {@link JRXMLDocumentHandler#canHandle(String, String)} method.
     */
    @Test
    public void testCanHandle() {
        assertTrue(handler.canHandle("foo.jrxml", "text/xml"));
        assertFalse(handler.canHandle("foo.xml", "text/xml"));
    }

    /**
     * Tests the {@link JRXMLDocumentHandler#create(String, InputStream, String, int)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreateFromStream() throws Exception {
        handler = new JRXMLDocumentHandler(getArchetypeService());
        String name = "/reports/valid.jrxml";
        InputStream report = getClass().getResourceAsStream(name);
        Document document = handler.create(name, report, "text/xml", -1);
        assertNotNull(document);
        assertEquals("valid.jrxml", document.getName());
        assertEquals("text/xml", document.getMimeType());
        assertEquals(1521, document.getDocSize()); // file is recoded, so the length changes

        // doc size represents the uncompressed length
        assertNotEquals(document.getDocSize(), document.getContents().length);

        InputStream stream = handler.getContent(document);
        JRXmlLoader.load(stream);
        stream.close();
    }

    /**
     * Verifies that a {@link JRXMLDocumentException} is thrown with an error message prompting to .jxrml version
     * compatibility if the file is invalid.
     */
    @Test
    public void testCreateFromInvalidStream() {
        handler = new JRXMLDocumentHandler(getArchetypeService());
        String name = "/reports/invalid.jrxml";
        InputStream report = getClass().getResourceAsStream(name);
        try {
            handler.create(name, report, "text/xml", -1);
            fail("Expected " + JRXMLDocumentException.class.getSimpleName() + " to be thrown");
        } catch (JRXMLDocumentException expected) {
            assertEquals(JRXMLDocumentException.ErrorCode.ReadError, expected.getErrorCode());
            assertEquals("Failed to read /reports/invalid.jrxml. Please ensure it is saved to be compatible with "
                         + "JasperReports 6.2.0", expected.getMessage());
        }
    }
}
