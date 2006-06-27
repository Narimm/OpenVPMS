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

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import net.sf.jasperreports.view.JasperViewer;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.PrintStream;
import java.util.List;


/**
 * Simple tool for listing and reporting on <code>IMObject</code> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportTool {

    /**
     * The archetype service.
     */
    IArchetypeService _service;


    /**
     * Construct a new <code>ReportTool</code>.
     */
    public ReportTool() {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "applicationContext.xml");
        _service = (IArchetypeService) context.getBean("archetypeService");
    }

    /**
     * Lists instances of with a particular short name.
     *
     * @param shortName the short name
     */
    public void list(String shortName) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true)
                .setFirstRow(0)
                .setNumOfRows(ArchetypeQuery.ALL_ROWS);
        IPage<IMObject> rows = _service.get(query);
        for (IMObject object : rows.getRows()) {
            System.out.println(object.getArchetypeId().getShortName()
                    + " " + object.getUid() + " " + object.getName());
        }
    }

    /**
     * Returns the first instance with the specified short name, and name
     *
     * @param shortName the archetype short name
     * @param name      the archetype name
     * @return the corresponding object, or <code>null</code> if none was found
     */
    public IMObject get(String shortName, String name) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true)
                .setFirstRow(0)
                .setNumOfRows(ArchetypeQuery.ALL_ROWS);
        query.add(new NodeConstraint("name", name));
        IPage<IMObject> rows = _service.get(query);
        List<IMObject> objects = rows.getRows();
        return (!objects.isEmpty()) ? objects.get(0) : null;
    }

    /**
     * Returns the first instance with the specified short name and uid
     *
     * @param shortName the archetype short name
     * @param uid       the instance identifier
     * @return the corresponding object, or <code>null</code> if none was found
     */
    public IMObject get(String shortName, long uid) {
        ArchetypeId id = _service.getArchetypeDescriptor(
                shortName).getType();
        return ArchetypeQueryHelper.getByUid(_service, id, uid);
    }

    /**
     * Generates a report for an object and displays it on-screen.
     *
     * @param object  the object
     * @param showXML if  <code>true</code> display the .jrxml
     */
    public void view(IMObject object, boolean showXML) throws JRException {
        JasperPrint report = generate(object, showXML);
        JasperViewer viewer = new JasperViewer(report, true);
        viewer.setVisible(true);
    }

    /**
     * Generates a report for an object and saves it as a PDF.
     *
     * @param object  the object
     * @param path    the PDF output path
     * @param showXML if  <code>true</code> display the .jrxml
     */
    public void generatePDF(IMObject object, String path, boolean showXML)
            throws JRException {
        JasperPrint report = generate(object, showXML);
        JasperExportManager.exportReportToPdfFile(report, path);
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
                boolean list = config.getBoolean("list");
                boolean report = config.getBoolean("report");
                String shortName = config.getString("shortName");
                long id = config.getLong("id", -1);
                String name = config.getString("name");
                String pdf = config.getString("pdf");

                if (list && shortName != null) {
                    ReportTool reporter = new ReportTool();
                    reporter.list(shortName);
                } else if (report && shortName != null) {
                    ReportTool reporter = new ReportTool();
                    IMObject object;
                    if (id == -1) {
                        object = reporter.get(shortName, name);
                    } else {
                        object = reporter.get(shortName, id);
                    }
                    boolean xml = config.getBoolean("xml");
                    if (object != null) {
                        if (pdf != null) {
                            reporter.generatePDF(object, pdf, xml);
                        } else {
                            reporter.view(object, xml);
                        }
                    } else {
                        System.out.println("No match found");
                    }
                } else {
                    displayUsage(parser);
                }
            }
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Generates a report for an object.
     *
     * @param object  the object
     * @param showXML if  <code>true</code> display the .jrxml
     * @return a report for <code>object</code>
     * @throws JRException for any error
     */
    private JasperPrint generate(IMObject object, boolean showXML) throws
                                                                   JRException {
        ArchetypeDescriptor archetype
                = _service.getArchetypeDescriptor(object.getArchetypeId());
        IMObjectReport report = IMObjectReportFactory.create(
                archetype.getShortName(), _service);
        if (showXML) {
            JRXmlWriter.writeReport(report.getReport(),
                                    new PrintStream(System.out), "UTF-8");
            for (JasperReport subreport : report.getSubreports()) {
                JRXmlWriter.writeReport(subreport,
                                        new PrintStream(System.out), "UTF-8");
            }
        }
        return report.generate(object);
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();

        parser.registerParameter(new Switch("report").setShortFlag('r')
                .setHelp("Generate a report for the specified archetype"));
        parser.registerParameter(new Switch("list").setShortFlag('l')
                .setHelp("List archetypes with the specified short name"));
        parser.registerParameter(new FlaggedOption("shortName")
                .setShortFlag('s').setLongFlag("shortName")
                .setHelp("The archetype short name"));
        parser.registerParameter(new FlaggedOption("name")
                .setShortFlag('n').setLongFlag("name")
                .setHelp("The archetype name. Use with -r"));
        parser.registerParameter(new FlaggedOption("id")
                .setShortFlag('i').setLongFlag("id")
                .setStringParser(JSAP.LONG_PARSER)
                .setHelp("The archetype id. Use with -r"));
        parser.registerParameter(new FlaggedOption("pdf").setShortFlag('p')
                .setLongFlag("pdf")
                .setHelp("Generate a PDF file. Use with -r"));
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
                .println("Usage: java " + ReportTool.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
