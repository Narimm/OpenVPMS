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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRQuery;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.fill.JREvaluator;
import net.sf.jasperreports.repo.RepositoryService;
import org.apache.commons.jxpath.Functions;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.report.ParameterType;
import org.openvpms.report.ReportException;

import java.sql.Connection;
import java.util.Collections;
import java.util.Set;

import static net.sf.jasperreports.engine.query.JRJdbcQueryExecuterFactory.QUERY_LANGUAGE_SQL;


/**
 * A {@link JasperIMReport} that uses pre-defined templates.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTemplatedJasperIMReport<T> extends AbstractJasperIMReport<T> {

    /**
     * The report name.
     */
    private final String name;

    /**
     * The template loader.
     */
    private final JasperTemplateLoader loader;


    /**
     * Constructs an {@link AbstractTemplatedJasperIMReport}.
     *
     * @param template  the document template
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param handlers  the document handlers
     * @param functions the JXPath extension functions
     * @throws ReportException if the report cannot be created
     */
    public AbstractTemplatedJasperIMReport(Document template, IArchetypeService service, ILookupService lookups,
                                           DocumentHandlers handlers, Functions functions) {
        super(service, lookups, handlers, functions);
        SimpleJasperReportsContext context = getJasperReportsContext();
        loader = init(new JasperTemplateLoader(template, service, handlers, context), context);
        this.name = template.getName();
    }

    /**
     * Constructs an {@link AbstractTemplatedJasperIMReport}.
     *
     * @param design    the master report design
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param handlers  the document handlers
     * @param functions the JXPath extension functions
     * @throws ReportException if the report cannot be created
     */
    public AbstractTemplatedJasperIMReport(JasperDesign design, IArchetypeService service, ILookupService lookups,
                                           DocumentHandlers handlers, Functions functions) {
        super(service, lookups, handlers, functions);
        SimpleJasperReportsContext context = getJasperReportsContext();
        loader = init(new JasperTemplateLoader(design, service, handlers, context), context);
        this.name = design.getName();
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
        if (query != null && QUERY_LANGUAGE_SQL.equalsIgnoreCase(query.getLanguage())) {
            ParameterType type = new ParameterType(JRParameter.REPORT_CONNECTION, Connection.class,
                                                   "JDBC connection", true);
            result.add(type);
        }
        return result;
    }

    /**
     * Returns the report name.
     *
     * @return the report name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the master report.
     *
     * @return the master report
     */
    public JasperReport getReport() {
        return loader.getReport();
    }

    /**
     * Returns the expression evaluator.
     *
     * @return the expression evaluator
     * @throws JRException if the evaluator can't be loaded
     */
    @Override
    protected JREvaluator getEvaluator() throws JRException {
        return loader.getEvaluator();
    }

    /**
     * Returns the template loader.
     *
     * @return the loader
     */
    protected JasperTemplateLoader getLoader() {
        return loader;
    }

    /**
     * Initialises the context with the loader, in order to lazily load sub-reports.
     *
     * @param loader  the loader
     * @param context the context
     * @return the loader
     */
    private JasperTemplateLoader init(JasperTemplateLoader loader, SimpleJasperReportsContext context) {
        // register the loader to lazily load sub-reports
        context.setExtensions(RepositoryService.class, Collections.singletonList(loader));
        return loader;
    }

}
