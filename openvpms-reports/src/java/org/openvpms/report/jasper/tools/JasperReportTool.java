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

import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToCreateReport;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import net.sf.jasperreports.view.JasperViewer;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.jasper.DynamicJasperIMObjectReport;
import org.openvpms.report.jasper.JasperIMObjectReport;
import org.openvpms.report.jasper.TemplateHelper;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.tools.ReportTool;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;


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
    private final boolean _showXML;

    /**
     * Construct a new <code>JasperReportTool</code>.
     *
     * @param service the archetype service
     * @param showXML if  <code>true</code> display the .jrxml
     */
    public JasperReportTool(IArchetypeService service, boolean showXML) {
        super(service);
        _showXML = showXML;
    }

    /**
     * Generates a report for an object and displays it on-screen.
     *
     * @param object the object
     */
    public void view(IMObject object) throws JRException {
        IMObjectReport report = getReport(object);
        if (report instanceof JasperIMObjectReport) {
            JasperIMObjectReport r = (JasperIMObjectReport) report;
            JasperViewer viewer = new JasperViewer(r.report(object), true);
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
    protected IMObjectReport getReport(IMObject object) {
        IArchetypeService service = getArchetypeService();
        String shortName = object.getArchetypeId().getShortName();
        Document doc = TemplateHelper.getDocumentForArchetype(
                shortName, service);
        JasperIMObjectReport report = null;
        String[] mimeTypes = {DocFormats.PDF_TYPE};
        try {
            if (doc != null) {
                if (doc.getName().endsWith(".jrxml")) {
                    ByteArrayInputStream stream
                            = new ByteArrayInputStream(doc.getContents());
                    JasperDesign design = JRXmlLoader.load(stream);
                    report = new TemplatedJasperIMObjectReport(
                            design, mimeTypes, service);
                } else {
                    System.err.println("Warning:" + doc.getName()
                            + " not a recognised jasper extension. "
                            + "Using dynamic report");
                }
            }
            if (report == null) {
                report = new DynamicJasperIMObjectReport(
                        service.getArchetypeDescriptor(shortName), mimeTypes,
                        service);
            }
        } catch (JRException exception) {
            throw new IMObjectReportException(FailedToCreateReport, exception,
                                              exception.getMessage());
        }

        if (_showXML) {
            try {
                JRXmlWriter.writeReport(report.getReport(),
                                        new PrintStream(System.out), "UTF-8");
                for (JasperReport subreport : report.getSubreports()) {
                    JRXmlWriter.writeReport(subreport,
                                            new PrintStream(System.out),
                                            "UTF-8");
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
        return new JasperReportTool(initArchetypeService(contextPath), showXML);
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
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err
                .println("Usage: java " + JasperReportTool.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
