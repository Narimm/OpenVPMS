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
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.fill.JREvaluator;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.repo.ReportResource;
import net.sf.jasperreports.repo.RepositoryService;
import net.sf.jasperreports.repo.Resource;
import org.apache.commons.io.IOUtils;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.ReportException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.openvpms.report.ReportException.ErrorCode.FailedToCreateReport;
import static org.openvpms.report.ReportException.ErrorCode.FailedToFindSubReport;


/**
 * Helper for loading and compiling jasper report templates.
 * <p/>
 * This implements {@link RepositoryService} in order to support loading of sub-reports during report evaluation.
 * <br/>
 * This allows sub-reports to be loaded based on data in the report, rather than statically defined.
 *
 * @author Tim Anderson
 */
public class JasperTemplateLoader implements RepositoryService {

    /**
     * The report name, used in error reporting.
     */
    private final String name;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The jasper reports context.
     */
    private final JasperReportsContext context;

    /**
     * The compiled report.
     */
    private final JasperReport report;

    /**
     * The sub-reports, keyed on name.
     */
    private final Map<String, JasperReport> subReports = new HashMap<>();

    /**
     * Cached expression evaluator.
     */
    private JREvaluator evaluator;

    /**
     * Constructs a {@link JasperTemplateLoader}.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param handlers the document handlers
     * @param context  the jasper reports context
     * @throws ReportException if the report cannot be created
     */
    public JasperTemplateLoader(Document template, IArchetypeService service, DocumentHandlers handlers,
                                JasperReportsContext context) {
        this.name = template.getName();
        this.service = service;
        this.context = context;
        this.handlers = handlers;

        InputStream stream = null;
        try {
            DocumentHandler handler = handlers.get(template);
            stream = handler.getContent(template);
            JasperDesign design = JRXmlLoader.load(context, stream);
            report = compile(design);
        } catch (DocumentException | JRException exception) {
            throw new ReportException(exception, FailedToCreateReport, template.getName(), exception.getMessage());
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * Constructs a {@link JasperTemplateLoader}.
     *
     * @param design   the master report design
     * @param service  the archetype service
     * @param handlers the document handlers
     * @param context  the jasper reports context
     * @throws ReportException if the report cannot be created
     */
    public JasperTemplateLoader(JasperDesign design, IArchetypeService service, DocumentHandlers handlers,
                                JasperReportsContext context) {
        this.name = design.getName();
        this.service = service;
        this.handlers = handlers;
        this.context = context;
        report = compile(design);
    }

    /**
     * Returns the master report.
     *
     * @return the master report
     */
    public JasperReport getReport() {
        return report;
    }

    /**
     * Returns the expression evaluator.
     * <p/>
     * NOTE: this only supports evaluation of simple expressions. Expressions that invoke JasperReport functions aren't
     * supported as these require a constructed JasperReport.
     *
     * @return the expression evaluator
     * @throws JRException if the evaluator can't be loaded
     */
    public JREvaluator getEvaluator() throws JRException {
        if (evaluator == null) {
            evaluator = JasperCompileManager.loadEvaluator(report);
        }
        return evaluator;
    }

    /**
     * Returns a resource given its URI.
     *
     * @param uri the resource URI
     * @return {@code null}
     */
    @Override
    public Resource getResource(final String uri) {
        return null;
    }

    /**
     * Returns the sub-reports.
     * <p/>
     * These are only available after the report has been evaluated, as they are loaded as required.
     *
     * @return the sub-reports
     */
    public Map<String, JasperReport> getSubReports() {
        return subReports;
    }

    /**
     * Saves a resource to the repository.
     *
     * @param uri      the resource URI
     * @param resource the resource to save
     * @throws UnsupportedOperationException if invoked
     */
    @Override
    public void saveResource(final String uri, final Resource resource) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a resource given its URI.
     * <p/>
     * Note that this implementation only supports {@code ReportResource} resource types.
     *
     * @param uri          the resource URI
     * @param resourceType the resource type. Must be a {@code ReportResource} in this implementation
     * @return the corresponding resource, or {@code null} if {@code resourceType} is not supported
     * @throws ReportException if the report cannot be found
     */
    @Override
    public <K extends Resource> K getResource(final String uri, final Class<K> resourceType) {
        if (resourceType.isAssignableFrom(ReportResource.class)) {
            JasperReport report = getSubreport(uri);
            ReportResource resource = new ReportResource();
            resource.setReport(report);
            return resourceType.cast(resource);
        }
        return null;
    }

    /**
     * Compiles the master report.
     *
     * @param design the report design
     * @return the compiled report
     */
    protected JasperReport compile(JasperDesign design) {
        JasperReport result;
        try {
            result = JasperCompileManager.compileReport(design);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToCreateReport, name, exception.getMessage());
        }
        return result;
    }

    /**
     * Gets a sub-report, given its name.
     *
     * @param name the sub-report name
     * @return the corresponding report
     */
    protected JasperReport getSubreport(String name) {
        JasperReport compiled = subReports.get(name);
        if (compiled == null) {
            try {
                JasperDesign report = JasperReportHelper.getReport(name, service, handlers, context);
                if (report == null) {
                    throw new ReportException(FailedToFindSubReport, name, this.name);
                }
                compiled = JasperCompileManager.compileReport(report);
                subReports.put(name, compiled);
            } catch (JRException exception) {
                throw new ReportException(FailedToFindSubReport, exception, name, this.name);
            }
        }
        return compiled;
    }

}
