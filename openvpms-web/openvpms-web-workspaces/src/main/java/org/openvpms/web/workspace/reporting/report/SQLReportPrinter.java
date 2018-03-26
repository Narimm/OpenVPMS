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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.report;

import org.apache.commons.io.FilenameUtils;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;
import org.openvpms.report.Report;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.doc.FileNameFormatter;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.ReportRunner;
import org.openvpms.web.component.im.report.Reporter;
import org.openvpms.web.component.print.AbstractPrinter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.TemplateHasNoDocument;
import static org.openvpms.web.workspace.reporting.report.SQLReportException.ErrorCode.ConnectionError;
import static org.openvpms.web.workspace.reporting.report.SQLReportException.ErrorCode.NoQuery;


/**
 * Printer for reports that contain embedded SQL queries.
 *
 * @author Tim Anderson
 */
public class SQLReportPrinter extends AbstractPrinter {

    /**
     * The document template.
     */
    private final DocumentTemplate template;

    /**
     * The report.
     */
    private final Report report;

    /**
     * The report runner.
     */
    private final ReportRunner runner;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The file name formatter.
     */
    private final FileNameFormatter formatter;

    /**
     * The data source.
     */
    private final DataSource dataSource;

    /**
     * The connection parameter name.
     */
    private final String connectionName;

    /**
     * The report parameters.
     */
    private Map<String, Object> parameters = Collections.emptyMap();


    /**
     * Constructs an {@link SQLReportPrinter} to print a report.
     *
     * @param template   the template
     * @param context    the context
     * @param factory    the report factory
     * @param formatter  the file name formatter
     * @param dataSource the data source
     * @param service    the archetype service
     * @throws SQLReportException        for any report error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document template can't be found
     */
    public SQLReportPrinter(DocumentTemplate template, Context context, ReportFactory factory,
                            FileNameFormatter formatter, DataSource dataSource, IArchetypeService service) {
        this(template, template.getDocument(), context, factory, formatter, dataSource, service);
    }

    /**
     * Constructs an {@link SQLReportPrinter}.
     *
     * @param template   the template
     * @param document   the document
     * @param context    the context
     * @param factory    the report factory
     * @param formatter  the file name formatter
     * @param dataSource the data source
     * @param service    the archetype service
     * @throws SQLReportException        for any report error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document template can't be found
     */
    protected SQLReportPrinter(DocumentTemplate template, Document document, Context context,
                               ReportFactory factory, FileNameFormatter formatter, DataSource dataSource,
                               IArchetypeService service) {
        super(service);
        if (document == null) {
            throw new DocumentException(TemplateHasNoDocument, template.getName());
        }
        report = factory.createReport(document);
        this.runner = new ReportRunner(report);
        this.template = template;
        this.context = context;
        this.formatter = formatter;
        this.dataSource = dataSource;
        ParameterType connectionParam = getConnectionParameter();
        if (connectionParam == null) {
            throw new SQLReportException(NoQuery);
        }
        connectionName = connectionParam.getName();

        setInteractive(getInteractive(template, getDefaultPrinter(), context));
    }

    /**
     * Returns the document template.
     *
     * @return the document template
     */
    public DocumentTemplate getTemplate() {
        return template;
    }

    /**
     * Returns the report parameter types.
     *
     * @return the report parameter types
     */
    public Set<ParameterType> getParameterTypes() {
        return report.getParameterTypes();
    }

    /**
     * Sets the report parameters.
     *
     * @param parameters the parameters. May be {@code null}
     */
    public void setParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        this.parameters = parameters;
    }

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be {@code null}
     * @throws OpenVPMSException for any error
     */
    public void print(String printer) {
        Map<String, Object> params = getParameters(false);
        try (Connection connection = dataSource.getConnection()) {
            params.put(connectionName, connection);
            runner.run(() -> report.print(params, ReportContextFactory.create(context), getProperties(printer)));
        } catch (SQLException exception) {
            throw new SQLReportException(ConnectionError, exception);
        }
    }

    /**
     * Returns the default printer for the object.
     *
     * @return the default printer for the object, or {@code null} if none
     * is defined
     * @throws OpenVPMSException for any error
     */
    public String getDefaultPrinter() {
        return getDefaultPrinter(template, context);
    }

    /**
     * Returns a document for the object, corresponding to that which would be printed.
     *
     * @return a document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument() {
        return getDocument(DocFormats.PDF_TYPE, true);
    }

    /**
     * Returns a document for the object, corresponding to that which would be printed.
     *
     * @param mimeType the mime type. If {@code null} the default mime type associated with the report will be used.
     * @param email    if {@code true} indicates that the document will be emailed. Documents generated from templates
     *                 can perform custom formatting
     * @return a document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument(String mimeType, boolean email) {
        Map<String, Object> params = getParameters(email);
        try (Connection connection = dataSource.getConnection()) {
            params.put(connectionName, connection);
            Supplier<Document> generator = () -> {
                Map<String, Object> fields = ReportContextFactory.create(context);
                return report.generate(params, fields, mimeType);
            };
            Document document = runner.run(generator);
            String fileName = formatter.format(template.getName(), null, template);
            String extension = FilenameUtils.getExtension(document.getName());
            document.setName(fileName + "." + extension);
            return document;
        } catch (SQLException exception) {
            throw new SQLReportException(ConnectionError, exception);
        }
    }

    /**
     * Returns a display name for the objects being printed.
     *
     * @return a display name for the objects being printed
     */
    public String getDisplayName() {
        return template.getName();
    }

    /**
     * Returns the connection parameter.
     *
     * @return the connection parameter, or {@code null} if none is found
     */
    private ParameterType getConnectionParameter() {
        for (ParameterType type : report.getParameterTypes()) {
            if (Connection.class.equals(type.getType())) {
                return type;
            }
        }
        return null;
    }

    /**
     * Returns the report parameters.
     *
     * @param email if {@code true} indicates that the document will be emailed. Documents generated from templates
     *              can perform custom formatting
     * @return the report parameters
     */
    private Map<String, Object> getParameters(boolean email) {
        Map<String, Object> result = new HashMap<>();
        if (parameters != null) {
            result.putAll(parameters);
        }
        result.put(Reporter.IS_EMAIL, email);
        return result;
    }

}
