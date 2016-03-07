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

package org.openvpms.archetype.rules.doc;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link DefaultDocumentHandler}.
 *
 * @author Tim Anderson
 */
public class DefaultDocumentHandlerTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link DefaultDocumentHandler#canHandle(String, String)} method.
     */
    @Test
    public void testCanHandle() {
        DefaultDocumentHandler handler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT,
                                                                    getArchetypeService());
        assertTrue(handler.canHandle("foo.pdf", "application/pdf"));
        assertTrue(handler.canHandle("foo.jpg", "image/jpg"));
        assertTrue(handler.canHandle("foo.txt", "text/plain"));
    }

    /**
     * Verifies a document can be created from a stream, and the contents can subsequently be read.
     */
    @Test
    public void testSerialisation() throws Exception {
        DefaultDocumentHandler handler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT,
                                                                    getArchetypeService());
        String name = "foo.txt";
        String content = "some text with unicode \u2202";
        String mimeType = "text/plain";

        // create the document. Use -1 to indicate the size is not known - it should be calculated
        Document document = handler.create(name, IOUtils.toInputStream(content, UTF_8), mimeType, -1);
        assertEquals(name, document.getName());
        assertEquals(mimeType, document.getMimeType());
        assertEquals(content.getBytes("UTF-8").length, document.getDocSize());
        assertEquals(40666662, document.getChecksum());

        save(document);
        document = get(document);

        // verify the content matches that expected
        assertTrue(handler.canHandle(document));
        String read1 = toString(handler, document);
        assertEquals(content, read1);

        // verify the document can be updated
        String newContent = "some new text with unicode \u2202";
        handler.update(document, IOUtils.toInputStream(newContent, UTF_8), mimeType, -1);
        assertEquals(name, document.getName());
        assertEquals(mimeType, document.getMimeType());
        assertEquals(newContent.getBytes(UTF_8).length, document.getDocSize());
        assertEquals(3675053849L, document.getChecksum());

        String read2 = toString(handler, document);
        assertEquals(newContent, read2);
    }

    /**
     * Verifies that if the content size is different to that read, an exception is thrown.
     */
    @Test
    public void testCreateWithIncorrectSize() {
        DefaultDocumentHandler handler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT,
                                                                    getArchetypeService());
        String name = "foo.html";
        String content = "<html><body>some text</body></html>";
        String mimeType = "text/html";

        try {
            handler.create(name, IOUtils.toInputStream(content), mimeType, content.length() - 1);
            fail("Expected DocumentException to be thrown");
        } catch (DocumentException expected) {
            assertEquals("Failed to read foo.html", expected.getMessage());
        }

        try {
            handler.create(name, IOUtils.toInputStream(content), mimeType, content.length() + 1);
            fail("Expected DocumentException to be thrown");
        } catch (DocumentException expected) {
            assertEquals("Failed to read foo.html", expected.getMessage());
        }
    }

    /**
     * Helper to deserialise a document to a string.
     *
     * @param handler  the handler
     * @param document the document
     * @return the document as a string
     * @throws IOException for any I/O error
     */
    private String toString(DefaultDocumentHandler handler, Document document) throws IOException {
        InputStream inputStream = handler.getContent(document);
        String read = IOUtils.toString(inputStream, UTF_8);
        inputStream.close();
        return read;
    }
}
