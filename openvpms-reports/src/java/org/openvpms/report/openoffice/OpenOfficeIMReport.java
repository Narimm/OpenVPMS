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

package org.openvpms.report.openoffice;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ExpressionEvaluator;
import org.openvpms.report.ExpressionEvaluatorFactory;
import org.openvpms.report.IMReport;
import org.openvpms.report.ParameterType;
import org.openvpms.report.PrintProperties;
import org.openvpms.report.ReportException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.report.ReportException.ErrorCode.FailedToGenerateReport;
import static org.openvpms.report.ReportException.ErrorCode.FailedToPrintReport;


/**
 * Generates a report using an OpenOffice document as the template.
 *
 * @author Tim Anderson
 */
public class OpenOfficeIMReport<T> implements IMReport<T> {

    /**
     * The document template.
     */
    private final Document template;

    /**
     * Cache of parameters.
     */
    private Map<String, ParameterType> parameters;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(OpenOfficeIMReport.class);

    /**
     * Report parameter enabling a boolean flag to be supplied to indicate if the report is being emailed.
     * This enables different formatting to be used when emailing as opposed to printing. e.g. if letterhead is
     * used when printing, this can be included when emailing.
     */
    private static final String IS_EMAIL = "IsEmail";


    /**
     * Constructs an {@link OpenOfficeIMReport}.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param lookups  the lookup service
     * @param handlers the document handlers
     */
    public OpenOfficeIMReport(Document template, IArchetypeService service, ILookupService lookups,
                              DocumentHandlers handlers) {
        this.template = template;
        this.service = service;
        this.lookups = lookups;
        this.handlers = handlers;
    }

    /**
     * Returns the set of parameter types that may be supplied to the report.
     *
     * @return the parameter types
     */
    public Set<ParameterType> getParameterTypes() {
        Map<String, ParameterType> params = getParameters();
        return new LinkedHashSet<ParameterType>(params.values());
    }

    /**
     * Determines if the report accepts the named parameter.
     *
     * @param name the parameter name
     * @return {@code true} if the report accepts the parameter, otherwise {@code false}
     */
    public boolean hasParameter(String name) {
        return getParameters().containsKey(name);
    }

    /**
     * Returns the default mime type for report documents.
     *
     * @return the default mime type
     */
    public String getDefaultMimeType() {
        String mimeType = template.getMimeType();
        if (!ArrayUtils.contains(getMimeTypes(), mimeType)) {
            mimeType = DocFormats.PDF_TYPE;
        }
        return mimeType;
    }

    /**
     * Returns the supported mime types for report documents.
     *
     * @return the supported mime types
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    public String[] getMimeTypes() {
        return new String[]{DocFormats.ODT_TYPE, DocFormats.DOC_TYPE,
                            DocFormats.PDF_TYPE, DocFormats.TEXT_TYPE};
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException if invoked
     */
    public Document generate(Map<String, Object> parameters, Map<String, Object> fields) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException if invoked
     */
    public Document generate(Map<String, Object> parameters, Map<String, Object> fields, String mimeType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Generates a report for a collection of objects.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param objects the objects to report on
     * @return a document containing the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document generate(Iterator<T> objects) {
        return generate(objects, getDefaultMimeType());
    }

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
    public Document generate(Iterator<T> objects, String mimeType) {
        Map<String, Object> empty = Collections.emptyMap();
        return generate(objects, empty, null, mimeType);
    }

    /**
     * Generates a report for a collection of objects.
     * <p/>
     * The default mime type will be used to select the output format.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param fields     a map of additional field names and their values, to pass to the report. May be {@code null}
     * @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    public Document generate(Iterator<T> objects, Map<String, Object> parameters, Map<String, Object> fields) {
        return generate(objects, parameters, fields, getDefaultMimeType());
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param fields     a map of additional field names and their values, to pass to the report. May be {@code null}
     * @param mimeType   the output format of the report  @return a document containing the report
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    public Document generate(Iterator<T> objects, Map<String, Object> parameters, Map<String, Object> fields,
                             String mimeType) {
        OpenOfficeDocument doc = null;
        OOConnection connection = null;
        try {
            OOConnectionPool pool = OpenOfficeHelper.getConnectionPool();
            connection = pool.getConnection();
            doc = create(objects, parameters, fields, connection);
            return export(doc, mimeType);
        } finally {
            close(doc, connection);
        }
    }

    /**
     * Generates a report for a collection of objects to the specified stream.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param fields     a map of additional field names and their values, to pass to the report. May be {@code null}
     * @param mimeType   the output format of the report
     * @param stream     the stream to write to
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    public void generate(Iterator<T> objects, Map<String, Object> parameters, Map<String, Object> fields,
                         String mimeType, OutputStream stream) {
        OpenOfficeDocument doc = null;
        OOConnection connection = null;
        try {
            OOConnectionPool pool = OpenOfficeHelper.getConnectionPool();
            connection = pool.getConnection();
            doc = create(objects, parameters, fields, connection);
            byte[] content = doc.export(mimeType);
            try {
                stream.write(content);
            } catch (IOException exception) {
                throw new ReportException(exception, FailedToGenerateReport, exception.getMessage());
            }
        } finally {
            close(doc, connection);
        }
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void print(Map<String, Object> parameters, Map<String, Object> fields, PrintProperties properties) {
        throw new UnsupportedOperationException();
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param properties the print properties
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void print(Iterator<T> objects, PrintProperties properties) {
        print(objects, Collections.<String, Object>emptyMap(), null, properties);
    }

    /**
     * Prints a report directly to a printer.
     *
     * @param objects    the objects to report on
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param fields     a map of additional field names and their values, to pass to the report. May be {@code null}
     * @param properties the print properties
     * @throws ReportException               for any report error
     * @throws ArchetypeServiceException     for any archetype service error
     * @throws UnsupportedOperationException if this operation is not supported
     */
    public void print(Iterator<T> objects, Map<String, Object> parameters, Map<String, Object> fields,
                      PrintProperties properties) {
        OpenOfficeDocument doc = null;
        OOConnection connection = null;
        try {
            PrintService service = OpenOfficeHelper.getPrintService();
            connection = OpenOfficeHelper.getConnectionPool().getConnection();
            doc = create(objects, parameters, fields, connection);
            service.print(doc, properties, true);
        } catch (OpenOfficeException exception) {
            throw new ReportException(exception, FailedToPrintReport, exception.getMessage());
        } finally {
            close(doc, connection);
        }
    }

    /**
     * Creates an openoffice document from a collection of objects.
     * Note that the collection is limited to a single object.
     *
     * @param objects    the objects to generate the document from
     * @param parameters a map of parameter names and their values, to pass to the report. May be {@code null}
     * @param fields     a map of additional field names and their values, to pass to the report. May be {@code null}
     * @param connection a connection to the OpenOffice service  @return a new openoffice document
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected OpenOfficeDocument create(Iterator<T> objects, Map<String, Object> parameters, Map<String, Object> fields,
                                        OOConnection connection) {
        OpenOfficeDocument doc = null;
        T object = null;
        if (objects.hasNext()) {
            object = objects.next();
        }
        if (object == null || objects.hasNext()) {
            throw new ReportException(FailedToGenerateReport, "Can only report on single objects");
        }

        try {
            doc = createDocument(template, connection, handlers);
            if (parameters != null) {
                populateInputFields(doc, parameters);
            }
            populateUserFields(doc, object, parameters, fields);

            // refresh the text fields
            doc.refresh();
        } catch (OpenVPMSException exception) {
            if (doc != null) {
                doc.close();
            }
            throw exception;
        }
        return doc;
    }

    /**
     * Returns the name of the user fields that should be prompted for.
     *
     * @param document the document
     * @return the user field names
     */
    protected Set<String> getInputFields(OpenOfficeDocument document) {
        Map<String, ParameterType> fields = document.getInputFields();
        return fields.keySet();
    }

    /**
     * Creates a new document.
     *
     * @param template   the document template
     * @param connection the connection to the OpenOffice service
     * @param handlers   the document handlers
     * @return a new document
     * @throws OpenOfficeException for any error
     */
    protected OpenOfficeDocument createDocument(Document template,
                                                OOConnection connection,
                                                DocumentHandlers handlers) {
        return new OpenOfficeDocument(template, connection, handlers);
    }

    /**
     * Populates input fields with parameters of the same name.
     *
     * @param document   the document
     * @param parameters the parameters
     */
    protected void populateInputFields(OpenOfficeDocument document,
                                       Map<String, Object> parameters) {
        for (Map.Entry<String, Object> p : parameters.entrySet()) {
            String name = p.getKey();
            if (document.hasInputField(name)) {
                String value = (p.getValue() != null) ? p.getValue().toString() : null;
                document.setInputField(name, value);
            }
        }
    }

    /**
     * Populates user fields in a document.
     * <p/>
     * If a field exists with the same name as a parameter, then this will be populated with the parameter value.
     *
     * @param document   the document
     * @param object     the object to evaluate expressions with
     * @param parameters the parameters. May be {@code null}
     * @param fields     a map of additional field names and their values, to pass to the report. May be {@code null}
     */
    protected void populateUserFields(OpenOfficeDocument document, T object, Map<String, Object> parameters,
                                      Map<String, Object> fields) {
        ExpressionEvaluator eval = ExpressionEvaluatorFactory.create(object, fields, service, lookups);
        List<String> userFields = document.getUserFieldNames();
        for (String name : userFields) {
            String value = getParameter(name, parameters);
            if (value == null) {
                value = document.getUserField(name);
                if (value != null) {
                    value = eval.getFormattedValue(value);
                }
            }
            document.setUserField(name, value);
        }
    }

    /**
     * Helper to return the string value of a parameter, if it exists.
     *
     * @param name       the parameter name
     * @param parameters the parameters. May be {@code null}
     * @return the parameter value, or {@code null} if it is not found
     */
    private String getParameter(String name, Map<String, Object> parameters) {
        String result = null;
        if (parameters != null) {
            Object value = parameters.get(name);
            if (value != null) {
                result = value.toString();
            }
        }
        return result;
    }

    /**
     * Returns the document parameters.
     * NOTE: this is an expensive operation as the document needs to be loaded then subsequently discarded. TODO
     *
     * @return the document parameters
     */
    private Map<String, ParameterType> getParameters() {
        if (parameters == null) {
            OpenOfficeDocument doc = null;
            OOConnection connection = null;
            try {
                OOConnectionPool pool = OpenOfficeHelper.getConnectionPool();
                connection = pool.getConnection();
                doc = createDocument(template, connection, handlers);
                parameters = doc.getInputFields();
                if (doc.hasUserField(IS_EMAIL)) {
                    // enable the IsEmail user field to be specified as a parameter
                    // Note: MS-Word documents can specify IsEmail, but can't do anything with them as OpenOffice
                    // won't process MS-Word field expressions.
                    parameters.put(IS_EMAIL, new ParameterType(IS_EMAIL, boolean.class, IS_EMAIL, true, false));
                }
            } finally {
                close(doc, connection);
            }
        }
        return parameters;
    }

    /**
     * Helper to close a document and connection.
     *
     * @param doc        the document. May be {@code null}
     * @param connection the connection. May be {@code null}
     */
    private void close(OpenOfficeDocument doc, OOConnection connection) {
        if (doc != null) {
            try {
                doc.close();
            } catch (Throwable exception) {
                log.warn(exception, exception);
            }
        }
        OpenOfficeHelper.close(connection);
    }

    /**
     * Exports a document, serializing to a {@link Document}.
     *
     * @param doc      the document to export
     * @param mimeType the mime-type of the document     *
     * @return a new document
     * @throws OpenOfficeException for any error
     */
    private Document export(OpenOfficeDocument doc, String mimeType) {
        String name = template.getName();
        if (!DocFormats.ODT_TYPE.equals(mimeType)) {
            name = FilenameUtils.removeExtension(name);
        }
        return doc.export(mimeType, name);
    }
}
