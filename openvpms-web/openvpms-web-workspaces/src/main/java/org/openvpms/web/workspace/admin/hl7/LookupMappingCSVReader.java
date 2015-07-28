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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import org.openvpms.archetype.csv.AbstractCSVReader;
import org.openvpms.archetype.csv.CSVReaderException;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads mapping data from a CSV document in the format written by {@link LookupMappingCSVWriter}.
 *
 * @author Tim Anderson
 */
public class LookupMappingCSVReader extends AbstractCSVReader {

    /**
     * The 'map from type' column.
     */
    private static final int FROM_TYPE = 0;

    /**
     * The 'map from code' column.
     */
    private static final int FROM_CODE = FROM_TYPE + 1;

    /**
     * The 'map from code' column.
     */
    private static final int FROM_NAME = FROM_CODE + 1;

    /**
     * The 'map from type' column.
     */
    private static final int TO_TYPE = FROM_NAME + 1;

    /**
     * The 'map to code' column.
     */
    private static final int TO_CODE = TO_TYPE + 1;

    /**
     * The 'map to name' column.
     */
    private static final int TO_NAME = TO_CODE + 1;


    /**
     * Constructs a {@link LookupMappingCSVReader}.
     *
     * @param handlers  the document handlers
     * @param separator the field separator
     */
    public LookupMappingCSVReader(DocumentHandlers handlers, char separator) {
        super(handlers, LookupMappingCSVWriter.HEADER, separator);
    }

    /**
     * Reads a document.
     *
     * @param document the document to read
     * @return the read mappings
     */
    public LookupMappings read(Document document) {
        List<LookupMapping> mappings = new ArrayList<>();
        List<LookupMapping> errors = new ArrayList<>();

        List<String[]> lines = readLines(document);

        int lineNo = 2; // line 1 is the header

        for (String[] line : lines) {
            parse(line, mappings, errors, lineNo);
            ++lineNo;
        }
        return new LookupMappings(mappings, errors);
    }

    /**
     * Parses a line.
     *
     * @param line   the line to parse
     * @param data   the parsed stock data
     * @param errors the stock data with errors
     * @param lineNo the line number
     */
    private void parse(String[] line, List<LookupMapping> data, List<LookupMapping> errors, int lineNo) {
        String fromType = null;
        String fromCode = null;
        String fromName = null;
        String toType = null;
        String toCode = null;
        String toName = null;

        try {
            checkFields(line, lineNo);
            fromType = getString(line, FROM_TYPE, lineNo, true);
            fromCode = getString(line, FROM_CODE, lineNo, true);
            fromName = getString(line, FROM_NAME, lineNo, true);
            toType = getString(line, TO_TYPE, lineNo, true);
            toCode = getString(line, TO_CODE, lineNo, true);
            toName = getString(line, TO_NAME, lineNo, true);
            data.add(new LookupMapping(fromType, fromCode, fromName, toType, toCode, toName, lineNo));
        } catch (CSVReaderException exception) {
            LookupMapping invalid = new LookupMapping(fromType, fromCode, fromName, toType, toCode, toName, lineNo);
            invalid.setError(exception.getMessage());
            errors.add(invalid);
        }
    }

}
