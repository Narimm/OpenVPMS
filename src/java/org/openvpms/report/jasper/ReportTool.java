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
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
                    + " " + object.getName());
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
     * Generates a report for an object and displays it on-screen.
     *
     * @param object the object
     */
    public void view(IMObject object) throws JRException {
        JasperPrint report = generate(object);
        JasperViewer viewer = new JasperViewer(report, true);
        viewer.setVisible(true);
    }

    /**
     * Generates a report for an object and saves it as a PDF.
     *
     * @param object the object
     * @param path   the PDF output path
     */
    public void generatePDF(IMObject object, String path) throws JRException {
        JasperPrint report = generate(object);
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
                String list = config.getString("list");
                String[] report = config.getStringArray("report");
                String pdf = config.getString("pdf");

                if (list != null) {
                    ReportTool reporter = new ReportTool();
                    reporter.list(config.getString("list"));
                } else if (report != null && report.length == 2) {
                    ReportTool reporter = new ReportTool();
                    IMObject object = reporter.get(report[0], report[1]);
                    if (object != null) {
                        if (pdf != null) {
                            reporter.generatePDF(object, pdf);
                        } else {
                            reporter.view(object);
                        }
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
     * @param object the object
     * @return a report for <code>object</code>
     * @throws JRException for any error
     */
    private JasperPrint generate(IMObject object) throws JRException {
        ArchetypeDescriptor archetype
                = _service.getArchetypeDescriptor(object.getArchetypeId());
        IMObjectReportGenerator generator
                = new IMObjectReportGenerator(archetype, _service);
        return generator.generate(object);
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();

        FlaggedOption report = new FlaggedOption("report") {
            public String getSyntax() {
                return "[-r <shortName>,<name>]";
            }
        };
        report.setShortFlag('r').setList(true).setListSeparator(',')
                .setHelp("Generate a report for the specified archetype");

        parser.registerParameter(report);

        FlaggedOption list = new FlaggedOption("list") {
            public String getSyntax() {
                return "[-l <shortName>]";
            }
        };
        list.setShortFlag('l')
                .setUsageName("shortName")
                .setHelp("List archetypes with the specified short name");
        parser.registerParameter(list);

        parser.registerParameter(new FlaggedOption("pdf").setShortFlag('p')
                .setLongFlag("pdf")
                .setHelp("Generate a PDF file. Use wuth -r"));
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
