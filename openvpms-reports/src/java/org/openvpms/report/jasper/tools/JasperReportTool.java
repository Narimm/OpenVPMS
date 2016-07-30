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

package org.openvpms.report.jasper.tools;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.view.JasperViewer;
import org.openvpms.archetype.function.factory.ArchetypeFunctionsFactory;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import org.openvpms.report.jasper.JasperIMReport;
import org.openvpms.report.jasper.JasperReportHelper;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.tools.ReportTool;

import java.util.Collections;
import java.util.List;

import static org.openvpms.report.ReportException.ErrorCode.FailedToCreateReport;
import static org.openvpms.report.ReportException.ErrorCode.NoTemplateForArchetype;
import static org.openvpms.report.ReportException.ErrorCode.UnsupportedTemplate;


/**
 * {@link ReportTool} extension for Jasper based reports, providing facilities to view the generated report.
 *
 * @author Tim Anderson
 */
public class JasperReportTool extends ReportTool {

    /**
     * Constructs a {@link JasperReportTool}.
     *
     * @param contextPath the application context path
     */
    public JasperReportTool(String contextPath) {
        super(contextPath);
    }

    /**
     * Generates a report for an object and displays it on-screen.
     *
     * @param object the object
     * @throws JRException for any jasper reports error
     */
    public void view(IMObject object) throws JRException {
        IMReport<IMObject> report = getReport(object);
        if (report instanceof JasperIMReport) {
            JasperIMReport<IMObject> r = (JasperIMReport<IMObject>) report;
            List<IMObject> list = Collections.singletonList(object);
            JasperViewer viewer = new JasperViewer(r.report(list), true);
            viewer.setVisible(true);
        } else {
            System.out.println("Can't view reports of type "
                               + report.getClass().getName());
        }
    }

    /**
     * Main line.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            if (!config.success()) {
                displayUsage(parser);
            } else {
                String contextPath = config.getString("context");
                boolean list = config.getBoolean("list");
                boolean report = config.getBoolean("report");
                String shortName = config.getString("shortName");
                long id = config.getLong("id", -1);
                String name = config.getString("name");
                String output = config.getString("output");

                if (list && shortName != null) {
                    JasperReportTool reporter = new JasperReportTool(contextPath);
                    reporter.list(shortName);
                } else if (report && shortName != null) {
                    JasperReportTool reporter = new JasperReportTool(contextPath);
                    IMObject object;
                    if (id == -1) {
                        object = reporter.get(shortName, name);
                    } else {
                        object = reporter.get(shortName, id);
                    }
                    if (object != null) {
                        if (output != null) {
                            reporter.save(object, output);
                        } else {
                            reporter.view(object);
                        }
                    } else {
                        System.out.println("No match found");
                    }
                } else {
                    displayUsage(parser);
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Gets a report for an object.
     *
     * @param object the object
     * @return a report for the object
     */
    protected IMReport<IMObject> getReport(IMObject object) {
        IArchetypeService service = getArchetypeService();
        ILookupService lookups = getLookupService();
        DocumentHandlers handlers = getDocumentHandlers();
        String shortName = object.getArchetypeId().getShortName();
        TemplateHelper helper = new TemplateHelper(service);
        Document doc = helper.getDocumentForArchetype(shortName);
        ArchetypeFunctionsFactory factory = getFunctionsFactory();

        if (doc == null) {
            throw new ReportException(NoTemplateForArchetype, shortName);
        }

        JasperDesign design;
        JasperIMReport<IMObject> report;
        try {
            if (doc.getName().endsWith(DocFormats.JRXML_EXT)) {
                design = JasperReportHelper.getReport(doc, handlers, DefaultJasperReportsContext.getInstance());
                report = new TemplatedJasperIMObjectReport(design, service, lookups, handlers, factory.create());
            } else {
                throw new ReportException(UnsupportedTemplate, doc.getName());
            }
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToCreateReport, doc.getName(), exception.getMessage());
        }

        return report;
    }

    /**
     * Prints usage information.
     *
     * @param parser the command line parser
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err.println("Usage: java " + JasperReportTool.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
