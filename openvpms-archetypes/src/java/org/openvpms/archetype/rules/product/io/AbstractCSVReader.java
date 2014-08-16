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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product.io;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;

/**
 * Base class for CSV readers.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCSVReader {

    /**
     * The mime type of the exported document.
     */
    public static final String MIME_TYPE = "text/csv";

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The expected CSV header.
     */
    private final String[] header;

    /**
     * Constructs an {@link AbstractCSVReader}.
     *
     * @param handlers the document handlers
     * @param header   the expected CSV header
     */
    public AbstractCSVReader(DocumentHandlers handlers, String[] header) {
        this.handlers = handlers;
        this.header = header;
    }

    /**
     * Reads the document into an array of lines.
     *
     * @param document the document to read
     * @return the lines
     */
    protected List<String[]> readLines(Document document) {
        try {
            DocumentHandler documentHandler = handlers.get(document);
            CSVReader reader = new CSVReader(new InputStreamReader(documentHandler.getContent(document)));
            String[] first = reader.readNext();
            if (first.length < header.length) {
                throw new ProductIOException(ProductIOException.ErrorCode.UnrecognisedDocument, 1, document.getName());
            }
            for (int i = 0; i < first.length; ++i) {
                if (!first[i].equalsIgnoreCase(header[i])) {
                    throw new ProductIOException(ProductIOException.ErrorCode.InvalidColumn, 1, first[i]);
                }
            }
            return reader.readAll();
        } catch (IOException exception) {
            throw new ProductIOException(ProductIOException.ErrorCode.ReadError, -1, exception);
        }
    }

    /**
     * Returns the long value at the specified index.
     *
     * @param line     the line
     * @param index    the index of the value
     * @param lineNo   the line no., for error reporting purposes
     * @param required if {@code true}, the id is required
     * @return the identifier, or {@code -1} if it is optional and not present
     */
    protected long getLong(String[] line, int index, int lineNo, boolean required) {
        String value = getString(line, index, lineNo, required);
        long result = -1;
        if (!StringUtils.isEmpty(value)) {
            try {
                result = Long.valueOf(value);
            } catch (NumberFormatException exception) {
                reportInvalid(header[index], value, lineNo);
            }
        }
        return result;
    }

    /**
     * Returns the decimal value at the specified line.
     *
     * @param line     the line
     * @param index    the index of the value
     * @param lineNo   the line no., for error reporting purposes
     * @param required if {@code true}, the value is required
     * @return the value, or {@code null} if there is no value
     */
    protected BigDecimal getDecimal(String[] line, int index, int lineNo, boolean required) {
        String value = getString(line, index, lineNo, required);
        BigDecimal result = null;
        try {
            if (!StringUtils.isEmpty(value)) {
                result = new BigDecimal(value);
            }
        } catch (NumberFormatException exception) {
            reportInvalid(header[index], value, lineNo);
        }
        return result;
    }

    /**
     * Returns the boolean value at the specified line.
     *
     * @param line   the line
     * @param index  the index of the value
     * @param lineNo the line no, for error reporting purposes
     * @return the value, or {@code false} if there is no value
     */
    protected boolean getBoolean(String[] line, int index, int lineNo) {
        String value = getString(line, index, lineNo, false);
        return value != null ? Boolean.valueOf(value) : false;
    }

    /**
     * Returns a string value from a line.
     *
     * @param line     the line
     * @param index    the index of the value
     * @param lineNo   the line no., for error reporting purposes
     * @param required if {@code true}, the value must be present
     * @return the string value. May be {@code null} if {@code required} is {@code false}
     * @throws ProductIOException if the value is required but not present
     */
    protected String getString(String[] line, int index, int lineNo, boolean required) {
        String value = StringUtils.trimToNull(line[index]);
        if (value == null && required) {
            throw new ProductIOException(ProductIOException.ErrorCode.RequiredValue, lineNo, header[index]);
        }
        return value;
    }

    /**
     * Reports an invalid value.
     *
     * @param name   the column name
     * @param value  the invalid value
     * @param lineNo the line no.
     * @throws ProductIOException
     */
    protected void reportInvalid(String name, String value, int lineNo) {
        throw new ProductIOException(ProductIOException.ErrorCode.InvalidValue, lineNo, name, value);
    }

}
