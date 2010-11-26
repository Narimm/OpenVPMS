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

package org.openvpms.report.jasper.tools;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import net.sf.jasperreports.view.JasperViewer;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import static org.openvpms.report.ReportException.ErrorCode.*;
import org.openvpms.report.jasper.JasperIMReport;
import org.openvpms.report.jasper.JasperReportHelper;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.tools.ReportTool;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;


/**
 * {@link ReportTool} extension for Jasper based reports, providing facilities
 * to view the generated report and XML.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class JasperReportTool extends ReportTool {

    /**
     * If <code>true</code> display the generated .jrxml
     */
    private final boolean showXML;


    /**
     * Constructs a new <code>JasperReportTool</code>.
     *
     * @param contextPath the application context path
     * @param showXML     if  <code>true</code> display the .jrxml
     */
    public JasperReportTool(String contextPath, boolean showXML) {
        super(contextPath);
        this.showXML = showXML;
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
            List<IMObject> list = Arrays.asList(object);
            JasperViewer viewer = new JasperViewer(r.report(list.iterator()),
                                                   true);
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
                boolean xml = config.getBoolean("xml");

                if (list && shortName != null) {
                    JasperReportTool reporter = create(contextPath, xml);
                    reporter.list(shortName);
                } else if (report && shortName != null) {
                    JasperReportTool reporter = create(contextPath, xml);
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
        DocumentHandlers handlers = getDocumentHandlers();
        String shortName = object.getArchetypeId().getShortName();
        TemplateHelper helper = new TemplateHelper(service);
        Document doc = helper.getDocumentForArchetype(shortName);
        if (doc == null) {
            throw new ReportException(NoTemplateForArchetype, shortName);
        }

        JasperIMReport<IMObject> report;
        try {
            if (doc.getName().endsWith(DocFormats.JRXML_EXT)) {
                JasperDesign design = JasperReportHelper.getReport(doc, handlers);
                report = new TemplatedJasperIMObjectReport(design, service, handlers);
            } else {
                throw new ReportException(UnsupportedTemplate, doc.getName());
            }
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToCreateReport, exception.getMessage());
        }

        if (showXML) {
            try {
                JRXmlWriter.writeReport(report.getReport(), new PrintStream(System.out), "UTF-8");
                for (JasperReport subreport : report.getSubreports()) {
                    JRXmlWriter.writeReport(subreport, new PrintStream(System.out), "UTF-8");
                }
            } catch (JRException exception) {
                exception.printStackTrace();
            }
        }
        return report;
    }

    /**
     * Creates the report tool.
     *
     * @param contextPath the application context path
     * @param showXML     if <code>true</code> display the generated .jrxml
     * @return a new report tool
     */
    private static JasperReportTool create(String contextPath,
                                           boolean showXML) {
        return new JasperReportTool(contextPath, showXML);
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    protected static JSAP createParser() throws JSAPException {
        JSAP parser = ReportTool.createParser();

        parser.registerParameter(new Switch("xml").setShortFlag('x')
                .setLongFlag("xml")
                .setHelp("Display generated XML. Use with -r"));
        return parser;
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
