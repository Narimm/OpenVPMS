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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.tools.data.loader;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This tool reads the specified XML document and process all of the
 * elements. It creates a spring application context to load the appropriate
 * services.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class StaxArchetypeDataLoader {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Caches loaded objects.
     */
    private final LoadCache cache = new LoadCache();

    /**
     * Maintains a list of archetypes and count to indicate the number of
     * each saved or validated
     */
    private Map<String, Long> statistics = new HashMap<String, Long>();

    /**
     * Determines if verbose logging will be performed.
     */
    private boolean verbose;

    /**
     * Determines if objects will be validated only, and not saved.
     */
    private boolean validateOnly;

    /**
     * The batch size.
     */
    private int batchSize;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(StaxArchetypeDataLoader.class);

    /**
     * The default name of the application context file.
     */
    private final static String DEFAULT_APP_CONTEXT_FNAME
            = "application-context.xml";

    /**
     * The file extension to filter.
     */
    private static final String EXTENSION = "xml";


    /**
     * Creates a new <tt>StaxArchetypeDataLoader</tt>.
     *
     * @param service the archetype service
     */
    public StaxArchetypeDataLoader(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Determines if logging will be verbose.
     *
     * @param verbose if <tt>true</tt> perform verbose logging.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Determines if objects will be validated only, and not saved.
     *
     * @param validateOnly if <tt>true</tt> only perform validation
     */
    public void setValidateOnly(boolean validateOnly) {
        this.validateOnly = validateOnly;
    }

    /**
     * Sets the batch size for saving objects.
     *
     * @param batchSize the batch size
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Loads data from the specified paths.
     *
     * @param paths the file or directory paths to load from
     * @throws FileNotFoundException if a file cannot be found
     * @throws OpenVPMSException for any archetype service exception
     * @throws XMLStreamException if a file cannot be parsed
     */
    public void load(String... paths) throws XMLStreamException, FileNotFoundException {
        Date start = new Date();
        DataLoader loader = new DataLoader(cache, service, verbose, validateOnly, batchSize, statistics);
        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                processDir(file, loader);
            } else {
                processFile(file, loader);
            }
        }
        loader.close();

        // dump the statistics
        dumpStatistics(start);
    }

    /**
     * The main line
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            String[] files = config.getStringArray("file");
            String dir = config.getString("dir");
            if (!config.success() || (files.length == 0 && dir == null)) {
                displayUsage(parser, config);
            } else {
                String contextPath = config.getString("context");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }

                IArchetypeService service = (IArchetypeService) context.getBean("archetypeService");
                StaxArchetypeDataLoader loader = new StaxArchetypeDataLoader(service);
                loader.setVerbose(config.getBoolean("verbose"));
                loader.setValidateOnly(config.getBoolean("validateOnly"));
                loader.setBatchSize(config.getInt("batchSaveSize"));
                if (files.length != 0) {
                    loader.load(files);
                } else if (dir != null) {
                    loader.load(dir);
                }
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
    }

    /**
     * Process all the files in the directory
     *
     * @param dir    the directory
     * @param loader the loader
     * @throws FileNotFoundException if the file cannot be found
     * @throws XMLStreamException if the file cannot be parsed
     */
    private void processDir(File dir, DataLoader loader) throws XMLStreamException, FileNotFoundException {
        String[] extensions = {EXTENSION};
        Collection collection = FileUtils.listFiles(dir, extensions, false);
        File[] files = FileUtils.convertFileCollectionToFileArray(collection);
        Arrays.sort(files);
        for (int i = 0, n = files.length; i < n; i++) {
            File file = files[i];
            processFile(file, loader);
            loader.flush();
        }
    }

    /**
     * Process the data elements.
     *
     * @param file   the file to process
     * @param loader the loader
     * @throws FileNotFoundException if the file cannot be found
     * @throws XMLStreamException if the file cannot be parsed
     */
    private void processFile(File file, DataLoader loader) throws XMLStreamException, FileNotFoundException {
        log.info("\n[PROCESSING FILE : " + file + "]\n");

        XMLStreamReader reader = getReader(file);
        loader.load(reader, file.getPath());
    }

    /**
     * Dump the statistics using the logger.
     *
     * @param start the start timestamp
     */
    private void dumpStatistics(Date start) {
        Date end = new Date();
        double elapsed = (end.getTime() - start.getTime()) / 1000;
        log.info("\n\n\n[STATISTICS]\n");
        int total = 0;
        for (String shortName : statistics.keySet()) {
            Long count = statistics.get(shortName);
            total += count;
            log.info(String.format("%42s %6d", shortName, count));
        }
        double rate = (elapsed != 0) ? total / elapsed : 0;
        log.info(String.format(
                "Processed %d objects in %.2f seconds (%.2f objects/sec)",
                total, elapsed, rate));
    }

    /**
     * Return the XMLReader for the specified file
     *
     * @param file the file
     * @return XMLStreamReader
     * @throws FileNotFoundException if the file cannot be found
     * @throws XMLStreamException if the file cannot be read
     */
    private XMLStreamReader getReader(File file) throws FileNotFoundException, XMLStreamException {
        FileInputStream stream = new FileInputStream(file);
        XMLInputFactory factory = XMLInputFactory.newInstance();

        return factory.createXMLStreamReader(stream);
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws com.martiansoftware.jsap.JSAPException
     *          if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                .setLongFlag("context").setDefault(DEFAULT_APP_CONTEXT_FNAME)
                .setHelp("Application context for the data loader"));
        parser.registerParameter(new FlaggedOption("dir").setShortFlag('d')
                .setLongFlag("dir").setHelp(
                "Directory where data files reside."));
        parser.registerParameter(new Switch("subdir").setShortFlag('s')
                .setLongFlag("subdir").setDefault("false").setHelp(
                "Search the subdirectories as well."));
        parser.registerParameter(new FlaggedOption("file").setShortFlag('f')
                .setList(true).setListSeparator(',')
                .setLongFlag("file").setHelp("Name of file containing data"));
        parser.registerParameter(new Switch("verbose").setShortFlag('v')
                .setLongFlag("verbose").setDefault("false").setHelp(
                "Displays verbose info to the console."));
        parser.registerParameter(new Switch("validateOnly")
                .setLongFlag("validateOnly").setDefault("false").setHelp(
                "Only validate the data file. Do not process."));
        parser.registerParameter(new FlaggedOption("batchSaveSize")
                .setStringParser(JSAP.INTEGER_PARSER).setDefault("0")
                .setShortFlag('b').setLongFlag("batchSaveSize")
                .setHelp("The batch size for saving objects."));
        return parser;
    }

    /**
     * Prints usage information and exits.
     *
     * @param parser the parser
     * @param result the parse result
     */
    private static void displayUsage(JSAP parser, JSAPResult result) {
        Iterator iter = result.getErrorMessageIterator();
        while (iter.hasNext()) {
            System.err.println(iter.next());
        }
        System.err.println();
        System.err.println("Usage: java " + StaxArchetypeDataLoader.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
