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

package org.openvpms.web.app.reporting;

import org.openvpms.archetype.rules.doc.DocumentException;
import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.NotFound;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ParameterType;
import org.openvpms.report.Report;
import org.openvpms.report.ReportFactory;
import static org.openvpms.web.app.reporting.SQLReportException.ErrorCode.ConnectionError;
import static org.openvpms.web.app.reporting.SQLReportException.ErrorCode.NoQuery;
import org.openvpms.web.component.print.AbstractPrinter;
import org.openvpms.web.system.ServiceHelper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Printer for reports that contain embedded SQL queries.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SQLReportPrinter extends AbstractPrinter {

    /**
     * The document template entity.
     */
    private final Entity template;

    /**
     * The report.
     */
    private final Report report;

    /**
     * The report parameters.
     */
    private Map<String, Object> parameters = Collections.emptyMap();

    /**
     * The connection parameter name.
     */
    private final String connectionName;


    /**
     * Constructs a new <tt>SQLReportPrinter</tt> to print a report.
     *
     * @param template the template
     * @throws SQLReportException        for any report error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the document template can't be found
     */
    public SQLReportPrinter(Entity template) {
        TemplateHelper helper = new TemplateHelper();
        Document document = helper.getDocumentFromTemplate(template);
        if (document == null) {
            throw new DocumentException(NotFound);
        }
        report = ReportFactory.createReport(
                document, ArchetypeServiceHelper.getArchetypeService(),
                ServiceHelper.getDocumentHandlers());
        this.template = template;
        ParameterType connectionParam = getConnectionParameter();
        if (connectionParam == null) {
            throw new SQLReportException(NoQuery);
        }
        connectionName = connectionParam.getName();

        setInteractive(getInteractive(template, getDefaultPrinter()));
    }

    /**
     * Returns the report name.
     *
     * @return the report name
     */
    public String getName() {
        return template.getName();
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
     * @param parameters the parameters. May be <tt>null</tt>
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
     * @param printer the printer name. May be <tt>null</tt>
     * @throws OpenVPMSException for any error
     */
    public void print(String printer) {
        Connection connection = null;
        Map<String, Object> params = new HashMap<String, Object>(parameters);
        try {
            connection = getConnection();
            params.put(connectionName, connection);
            report.print(params, getProperties(printer));
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Returns the default printer for the object.
     *
     * @return the default printer for the object, or <tt>null</tt> if none
     *         is defined
     * @throws OpenVPMSException for any error
     */
    public String getDefaultPrinter() {
        return getDefaultPrinter(template);
    }

    /**
     * Returns a document for the object, corresponding to that which would be
     * printed.
     *
     * @return a document
     * @throws OpenVPMSException for any error
     */
    public Document getDocument() {
        Map<String, Object> params = new HashMap<String, Object>(parameters);
        Connection connection = null;
        try {
            connection = getConnection();
            params.put(connectionName, connection);
            return report.generate(params, DocFormats.PDF_TYPE);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * Returns the connection parameter.
     *
     * @return the connection parameter, or <tt>null</tt> if none is found
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
     * Helper to return a new connection.
     *
     * @return a new connection
     * @throws SQLReportException if the connection can't be established
     */
    private Connection getConnection() {
        try {
            DataSource ds = ServiceHelper.getDataSource();
            return ds.getConnection();
        } catch (SQLException exception) {
            throw new SQLReportException(ConnectionError, exception);
        }
    }

    /**
     * Helper to close a connection.
     *
     * @param connection the connection. May be <tt>null</tt>
     */
    private void closeConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignore) {
            // do nothing
        }
    }
}
