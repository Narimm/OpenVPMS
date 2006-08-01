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

package org.openvpms.report.tools;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportFactory;
import org.openvpms.report.DocFormats;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private IArchetypeService _service;

    /**
     * Construct a new <code>ReportTool</code>.
     *
     * @param service the archetype service
     */
    public ReportTool(IArchetypeService service) {
        _service = service;
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
        ArchetypeId id = _service.getArchetypeDescriptor(shortName).getType();
        return ArchetypeQueryHelper.getByUid(_service, id, uid);
    }

    /**
     * Generates a report for an object and saves it to disk.
     *
     * @param object the object
     * @param path   the output path
     * @throws IOException for any I/O error
     */
    public void save(IMObject object, String path) throws IOException {
        IMObjectReport report = getReport(object);
        Document doc = report.generate(object);
        path = new File(path, doc.getName()).getPath();
        FileOutputStream stream = new FileOutputStream(path);
        stream.write(doc.getContents());
        stream.close();
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
                    ReportTool reporter = create(contextPath);
                    reporter.list(shortName);
                } else if (report && shortName != null && output != null) {
                    ReportTool reporter = ReportTool.create(contextPath);
                    IMObject object;
                    if (id == -1) {
                        object = reporter.get(shortName, name);
                    } else {
                        object = reporter.get(shortName, id);
                    }
                    if (object != null) {
                        reporter.save(object, output);
                    } else {
                        System.out.println("No match found");
                    }
                } else {
                    displayUsage(parser);
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * Gets a report for an object.
     *
     * @param object the object
     * @return a report for the object
     */
    protected IMObjectReport getReport(IMObject object) {
        String shortName = object.getArchetypeId().getShortName();
        String[] mimeTypes = {DocFormats.ODT_TYPE, DocFormats.PDF_TYPE};
        return IMObjectReportFactory.create(shortName, mimeTypes, _service);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return _service;
    }

    /**
     * Helper to initialise the archetype service.
     *
     * @param contextPath the application context path
     */
    protected static IArchetypeService initArchetypeService(
            String contextPath) {
        ApplicationContext context;
        if (!new File(contextPath).exists()) {
            context = new ClassPathXmlApplicationContext(contextPath);
        } else {
            context = new FileSystemXmlApplicationContext(contextPath);
        }

        return (IArchetypeService) context.getBean("archetypeService");
    }

    /**
     * Parses the command line.
     *
     * @param args the command line arguments
     * @return the parsed results
     * @throws JSAPException if the arguments can't be parsed
     */
    protected JSAPResult parse(String[] args) throws JSAPException {
        JSAP parser = createParser();
        return parser.parse(args);
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    protected static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();

        parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                .setLongFlag("context")
                .setDefault("applicationContext.xml")
                .setHelp("Application context path"));
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
        parser.registerParameter(new FlaggedOption("output").setShortFlag('o')
                .setLongFlag("output")
                .setHelp("Save report to file. Use with -r"));
        return parser;
    }

    /**
     * Creates the report tool.
     *
     * @param contextPath the application context path
     * @return a new report tool
     */
    private static ReportTool create(String contextPath) {
        return new ReportTool(initArchetypeService(contextPath));
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err.println("Usage: java " + ReportTool.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
