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

package org.openvpms.etl.tools.doc;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.Date;


/**
 * Document loader.
 * <p/>
 * This loads documents from the file system for all document acts
 * matching the specified short name that have a file name specified but no
 * corresponding document content.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentLoader {

    /**
     * The document creator.
     */
    private final Loader loader;

    /**
     * Determines if the generator should fail on error.
     */
    private boolean failOnError = true;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DocumentLoader.class);

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";


    /**
     * Constructs a new <tt>DocumentLoader</tt>.
     *
     * @param loader loader
     */
    public DocumentLoader(Loader loader) {
        this.loader = loader;
    }

    /**
     * Determines if generation should fail when an error occurs.
     * Defaults to <tt>true</tt>.
     *
     * @param failOnError if <tt>true</tt> fail when an error occurs
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Loads documents.
     */
    public void load() {
        while (loader.hasNext()) {
            if (!loader.loadNext() && failOnError) {
                break;
            }
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
            boolean byId = config.getBoolean("byid");
            boolean byName = config.getBoolean("byname");
            if (!config.success() || !(byId || byName)) {
                displayUsage(parser);
            } else {
                String contextPath = config.getString("context");
                File dir = config.getFile("source");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }
                Date start = new Date();
                Loader loader;
                IArchetypeService service = (IArchetypeService) context.getBean(
                        "archetypeService");
                DocumentFactory factory = new FileDocumentFactory();
                LoaderListener listener = (config.getBoolean("verbose"))
                        ? new LoggingLoaderListener(log)
                        : new DefaultLoaderListener();
                if (byId) {
                    loader = new IdLoader(dir, service, factory);
                } else {
                    String type = config.getString("type");
                    loader = new NameLoader(dir, type, service, factory);
                }
                loader.setListener(listener);
                DocumentLoader docLoader = new DocumentLoader(loader);
                docLoader.setFailOnError(config.getBoolean("failOnError"));
                docLoader.load();
                dumpStats(listener, start);
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
            System.exit(1);
        }
    }

    private static void dumpStats(LoaderListener listener, Date start) {
        Date end = new Date();
        double elapsed = (end.getTime() - start.getTime()) / 1000;
        log.info("\n\n\n[STATISTICS]\n");
        int total = listener.getProcessed();
        double rate = (elapsed != 0) ? total / elapsed : 0;
        log.info("Loaded: " + listener.getLoaded());
        log.info("Errors: " + listener.getErrors());
        log.info("Total:  " + total);
        log.info(String.format(
                "Processed %d files in %.2f seconds (%.2f files/sec)",
                total, elapsed, rate));
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        FileStringParser dirParser = FileStringParser.getParser();
        dirParser.setMustBeDirectory(true);
        dirParser.setMustExist(true);
        parser.registerParameter(new Switch("byid").setShortFlag('i')
                .setLongFlag("byid")
                .setHelp("Load files using the identifiers in their names"));
        parser.registerParameter(new Switch("byname").setShortFlag('n')
                .setLongFlag("byname")
                .setHelp(
                "Load files by matching their names with document acts"));
        parser.registerParameter(new FlaggedOption("source").setShortFlag('s')
                .setLongFlag("source")
                .setStringParser(dirParser)
                .setDefault("./")
                .setHelp("The directory to load files from. "
                + "Defaults to the current directory"));
        parser.registerParameter(new FlaggedOption("dest").setShortFlag('d')
                .setLongFlag("dest")
                .setStringParser(dirParser)
                .setHelp("The directory to move files to on successful load."));
        parser.registerParameter(new FlaggedOption("type").setShortFlag('t')
                .setLongFlag("type")
                .setHelp("The archetype short name. May contain wildcards. "
                + "If not specified, defaults to all document acts"));
        parser.registerParameter(new FlaggedOption("failOnError")
                .setShortFlag('e')
                .setLongFlag("failOnError")
                .setDefault("false")
                .setStringParser(BooleanStringParser.getParser())
                .setHelp("Fail on error"));
        parser.registerParameter(new Switch("verbose").setShortFlag('v')
                .setLongFlag("verbose").setDefault("false").setHelp(
                "Displays verbose info to the console."));
        parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                .setLongFlag("context")
                .setDefault(APPLICATION_CONTEXT)
                .setHelp("Application context path"));
        return parser;
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err.println("Usage: java "
                + DocumentLoader.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }
}
