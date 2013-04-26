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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;


/**
 * Generates a report for a collection of objects.
 *
 * @author Tim Anderson
 */
public interface IMReport<T> extends Report {

    /**
     * Generates a report for a collection of objects.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param objects the objects to report on
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Document generate(Iterator<T> objects);

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects  the objects to report on
     * @param mimeType the output format of the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Document generate(Iterator<T> objects, String mimeType);

    /**
     * Generates a report for a collection of objects.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Document generate(Iterator<T> objects, Map<String, Object> parameters);

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param mimeType   the output format of the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Document generate(Iterator<T> objects, Map<String, Object> parameters, String mimeType);

    /**
     * Generates a report for a collection of objects to the specified stream.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param mimeType   the output format of the report
     * @param stream     the stream to write to
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    void generate(Iterator<T> objects, Map<String, Object> parameters, String mimeType, OutputStream stream);

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param properties the print properties
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    void print(Iterator<T> objects, PrintProperties properties);

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param properties the print properties
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    void print(Iterator<T> objects, Map<String, Object> parameters, PrintProperties properties);


}
