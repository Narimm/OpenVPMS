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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

import java.util.Map;
import java.util.Set;


/**
 * Generates a report.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface Report {

    /**
     * Returns the set of parameter types that may be supplied to the report.
     *
     * @return the parameter types
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Set<ParameterType> getParameterTypes();

    /**
     * Determines if the report accepts the named parameter.
     *
     * @param name the parameter name
     * @return <tt>true</tt> if the report accepts the parameter, otherwise <tt>false</tt>
     */
    boolean hasParameter(String name);

    /**
     * Returns the default mime type for report documents.
     *
     * @return the default mime type
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    String getDefaultMimeType();

    /**
     * Returns the supported mime types for report documents.
     *
     * @return the supported mime types
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    String[] getMimeTypes();

    /**
     * Generates a report.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Document generate(Map<String, Object> parameters);

    /**
     * Generates a report.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @param mimeType   the output format of the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    Document generate(Map<String, Object> parameters, String mimeType);

    /**
     * Generates a report.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @param mimeTypes  a list of mime-types, used to select the preferred
     *                   output format of the report
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     * @deprecated use either of other generate() methods
     */
    @Deprecated
    Document generate(Map<String, Object> parameters, String[] mimeTypes);

    /**
     * Prints a report directly to a printer.
     *
     * @param parameters a map of parameter names and their values, to pass to
     *                   the report
     * @param properties the print properties
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    void print(Map<String, Object> parameters, PrintProperties properties);
}
