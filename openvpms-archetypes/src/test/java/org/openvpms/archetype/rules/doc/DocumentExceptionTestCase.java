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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.doc;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link DocumentException} class.
 *
 * @author Tim Anderson
 */
public class DocumentExceptionTestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     11, DocumentException.ErrorCode.values().length);
        checkException(DocumentException.ErrorCode.UnsupportedDoc,
                       "File 'foo' has an unsupported content type: bar", "foo", "bar");
        checkException(DocumentException.ErrorCode.ReadError, "Failed to read foo", "foo");
        checkException(DocumentException.ErrorCode.WriteError, "Failed to write foo", "foo");
        checkException(DocumentException.ErrorCode.NotFound, "Document not found");
        checkException(DocumentException.ErrorCode.InvalidUnits, "Invalid units: foo", "foo");
        checkException(DocumentException.ErrorCode.InvalidOrientation, "Invalid orientation: foo", "foo");
        checkException(DocumentException.ErrorCode.InvalidMediaTray, "Invalid media tray: foo", "foo");
        checkException(DocumentException.ErrorCode.InvalidPaperSize, "Invalid paper size: foo", "foo");
        checkException(DocumentException.ErrorCode.InvalidSides, "Invalid sides: foo", "foo");
        checkException(DocumentException.ErrorCode.TemplateHasNoDocument, "Document Template 'foo' has no document",
                       "foo");
        checkException(DocumentException.ErrorCode.DocumentHasNoTemplate, "Document has no template");
    }

    /**
     * Creates an {@link DocumentException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void checkException(DocumentException.ErrorCode code,
                                String expected, Object... args) {
        DocumentException exception = new DocumentException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }

}
