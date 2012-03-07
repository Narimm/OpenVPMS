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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import static net.sf.jasperreports.engine.query.JRJdbcQueryExecuterFactory.QUERY_LANGUAGE_SQL;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.ParameterType;
import org.openvpms.report.ReportException;

import java.sql.Connection;
import java.util.Map;
import java.util.Set;


/**
 * A {@link JasperIMReport} that uses pre-defined templates.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractTemplatedJasperIMReport<T>
        extends AbstractJasperIMReport<T> {

    /**
     * The template loader.
     */
    private JasperTemplateLoader template;


    /**
     * Constructs a new <tt>AbstractTemplatedJasperIMReport</tt>.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param handlers the document handlers
     * @throws ReportException if the report cannot be created
     */
    public AbstractTemplatedJasperIMReport(Document template,
                                           IArchetypeService service,
                                           DocumentHandlers handlers) {
        super(service, handlers);
        this.template = new JasperTemplateLoader(template, service, handlers);
    }

    /**
     * Returns the set of parameter types that may be supplied to the report.
     * If the report specifies an SQL query, includes a Connection parameter
     * type in the result.
     *
     * @return the parameter types
     * @throws ReportException if a parameter expression can't be evaluated
     */
    @Override
    public Set<ParameterType> getParameterTypes() {
        Set<ParameterType> result = super.getParameterTypes();
        JRQuery query = getReport().getQuery();
        if (query != null
                && QUERY_LANGUAGE_SQL.equalsIgnoreCase(query.getLanguage())) {
            ParameterType type = new ParameterType(
                    JRParameter.REPORT_CONNECTION, Connection.class,
                    "JDBC connection", true);
            result.add(type);
        }
        return result;
    }

    /**
     * Constructs a new <code>AbstractTemplatedJasperIMReport</code>.
     *
     * @param design   the master report design
     * @param service  the archetype service
     * @param handlers the document handlers
     * @throws ReportException if the report cannot be created
     */
    public AbstractTemplatedJasperIMReport(JasperDesign design,
                                           IArchetypeService service,
                                           DocumentHandlers handlers) {
        super(service, handlers);
        this.template = new JasperTemplateLoader(design, service, handlers);
    }

    /**
     * Returns the master report.
     *
     * @return the master report
     */
    public JasperReport getReport() {
        return template.getReport();
    }

    /**
     * Returns the sub-reports.
     *
     * @return the sub-reports
     */
    public JasperReport[] getSubreports() {
        return template.getSubReports();
    }

    /**
     * Returns the default report parameters to use when filling the report.
     *
     * @return the report parameters
     */
    protected Map<String, Object> getDefaultParameters() {
        Map<String, Object> result = super.getDefaultParameters();
        result.putAll(template.getParameters());
        return result;
    }

}
