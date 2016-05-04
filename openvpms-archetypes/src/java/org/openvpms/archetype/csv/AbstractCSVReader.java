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

package org.openvpms.archetype.csv;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;

import java.io.IOException;
import java.io.InputStream;
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
    private String[] header;

    /**
     * The possible field separators.
     */
    private final char[] separators;

    /**
     * Constructs an {@link AbstractCSVReader}.
     *
     * @param handlers  the document handlers
     * @param header    the expected CSV header
     * @param separator the field separator
     */
    public AbstractCSVReader(DocumentHandlers handlers, String[] header, char separator) {
        this(handlers, header, new char[]{separator});
    }

    /**
     * Constructs an {@link AbstractCSVReader}.
     *
     * @param handlers   the document handlers
     * @param header     the expected CSV header
     * @param separators the possible field separators
     */
    public AbstractCSVReader(DocumentHandlers handlers, String[] header, char[] separators) {
        if (separators.length == 0) {
            throw new IllegalArgumentException("Argument 'separators' cannot be empty");
        }
        this.handlers = handlers;
        this.header = header;
        this.separators = separators;
    }

    /**
     * Reads the document into an array of lines.
     *
     * @param document the document to read
     * @return the lines
     */
    protected List<String[]> readLines(Document document) {
        List<String[]> result = null;
        DocumentHandler documentHandler = handlers.get(document);
        CSVReader reader = null;
        try {
            for (int i = 0; i < separators.length; ++i) {
                InputStream content = documentHandler.getContent(document);
                reader = new CSVReader(new InputStreamReader(content), separators[i]);
                String[] first = reader.readNext();
                if ((first.length > 1) || (i + 1 == separators.length)) {
                    // if the first line is possibly valid, or there are no more separators, validate the header
                    checkHeader(first, document);
                    result = reader.readAll();
                    break;
                } else {
                    // try the next separator
                    reader.close();
                }
            }
            if (reader != null) {
                reader.close();
            }
        } catch (IOException exception) {
            throw new CSVReaderException(CSVReaderException.ErrorCode.ReadError, -1, exception);
        }
        return result;
    }

    /**
     * Registers the header.
     *
     * @param header the header
     */
    protected void setHeader(String[] header) {
        this.header = header;
    }

    /**
     * Verifies the header matches that expected.
     * <p/>
     * Subclasses that support multiple formats should override this method, and invoke {@link #setHeader(String[])}
     * with the actual header once the format is determined.
     *
     * @param line     the first line
     * @param document the document
     * @throws CSVReaderException if the header doesn't match that expected
     */
    protected void checkHeader(String[] line, Document document) {
        if (line.length < header.length) {
            throw new CSVReaderException(CSVReaderException.ErrorCode.UnrecognisedDocument, 1, document.getName());
        }
        for (int i = 0; i < header.length; ++i) {
            if (!line[i].equalsIgnoreCase(header[i])) {
                throw new CSVReaderException(CSVReaderException.ErrorCode.InvalidColumn, 1, line[i]);
            }
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
     * @throws CSVReaderException if the value is required but not present
     */
    protected String getString(String[] line, int index, int lineNo, boolean required) {
        String value = StringUtils.trimToNull(line[index]);
        if (value == null && required) {
            throw new CSVReaderException(CSVReaderException.ErrorCode.RequiredValue, lineNo, header[index]);
        }
        return value;
    }

    /**
     * Reports an invalid value.
     *
     * @param name   the column name
     * @param value  the invalid value
     * @param lineNo the line no.
     * @throws CSVReaderException
     */
    protected void reportInvalid(String name, String value, int lineNo) {
        throw new CSVReaderException(CSVReaderException.ErrorCode.InvalidValue, lineNo, name, value);
    }

    /**
     * Verifies the line has at least {@code header.length} fields.
     *
     * @param line   the line
     * @param lineNo the line number
     * @throws CSVReaderException if the line has the incorrect no. of fields
     */
    protected void checkFields(String[] line, int lineNo) {
        if (line.length < header.length) {
            throw new CSVReaderException(CSVReaderException.ErrorCode.InvalidLine, lineNo, lineNo, header.length,
                                         line.length);
        }
    }

}
