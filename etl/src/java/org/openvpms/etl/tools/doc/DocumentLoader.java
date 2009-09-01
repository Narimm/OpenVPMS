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
 * Loads documents from the file system, attaching them to existing document acts.
 * Documents may be loaded by name or by identifier.
 * <p/>
 * To load documents by name, the document file name is used to locate a document act with the same name.
 * <p/>
 * To load documents by identifier, the identifier is parsed from the document file name and used to retrieve the
 * corresponding document act.
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
    static final Log log = LogFactory.getLog(DocumentLoader.class);

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
                displayUsage(parser, null);
            } else {
                File source = config.getFile("source");
                File target = config.getFile("dest");
                checkDirs(source, target, parser);
                String contextPath = config.getString("context");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }
                Loader loader;
                IArchetypeService service = (IArchetypeService) context.getBean("archetypeService");
                DocumentFactory factory = new DefaultDocumentFactory();
                LoaderListener listener = (config.getBoolean("verbose"))
                                          ? new LoggingLoaderListener(log, target)
                                          : new DefaultLoaderListener(target);

                Date start = new Date();
                log.info("Starting load at: " + start);

                if (byId) {
                    boolean recurse = config.getBoolean("recurse");
                    boolean overwrite = config.getBoolean("overwrite");
                    loader = new IdLoader(source, service, factory, recurse, overwrite);
                } else {
                    String type = config.getString("type");
                    loader = new NameLoader(source, type, service, factory);
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

    /**
     * Verifies that the source and target directories are valid.
     *
     * @param source the source directory
     * @param target the target directory
     * @param parser the command line parser
     */
    private static void checkDirs(File source, File target, JSAP parser) {
        if (source == null) {
            displayUsage(parser, "No source directory specified");
        } else if (!source.isDirectory()) {
            displayUsage(parser, "Source is not a directory: " + source.getPath());
        } else {
            if (target != null) {
                if (!target.isDirectory()) {
                    displayUsage(parser, "Destination is not a directory: " + target.getPath());
                }
                if (target.equals(source)) {
                    displayUsage(parser, "Destination directory is the same as the source");
                }
                File parent = target.getParentFile();
                while (parent != null) {
                    if (parent.equals(source)) {
                        displayUsage(parser, "Destination directory cannot be a child of the source directory");
                    }
                    parent = parent.getParentFile();
                }
            }
        }
    }

    /**
     * Dumps statistics.
     *
     * @param listener the loader listener
     * @param start    the start time
     */
    private static void dumpStats(LoaderListener listener, Date start) {
        Date end = new Date();
        log.info("Ending load at: " + start);

        double elapsed = (end.getTime() - start.getTime()) / 1000;
        int total = listener.getProcessed();
        double rate = (elapsed != 0) ? total / elapsed : 0;
        log.info("Loaded: " + listener.getLoaded());
        log.info("Errors: " + listener.getErrors());
        log.info("Total:  " + total);
        log.info(String.format("Processed %d files in %.2f seconds (%.2f files/sec)", total, elapsed, rate));
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
                .setHelp("Load files by matching their names with document acts"));
        parser.registerParameter(new FlaggedOption("source").setShortFlag('s')
                .setLongFlag("source")
                .setStringParser(dirParser)
                .setDefault("./")
                .setHelp("The directory to load files from. "));
        parser.registerParameter(new Switch("recurse").setShortFlag('r')
                .setLongFlag("recurse")
                .setDefault("false")
                .setHelp("Recursively scan the source directory"));
        parser.registerParameter(new Switch("overwrite").setShortFlag('o')
                .setLongFlag("overwrite")
                .setDefault("false")
                .setHelp("Overwrite existing attachments. Ony applies when --byid is used"));
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
                .setLongFlag("verbose").setDefault("false").setHelp("Displays verbose info to the console."));
        parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                .setLongFlag("context")
                .setDefault(APPLICATION_CONTEXT)
                .setHelp("Application context path"));
        return parser;
    }

    /**
     * Prints usage information.
     *
     * @param parser the parser
     * @param error  the error. May be <tt>null</tt>
     */
    private static void displayUsage(JSAP parser, String error) {
        if (error != null) {
            System.err.println(error);
        }
        System.err.println("Usage: java " + DocumentLoader.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }
}
