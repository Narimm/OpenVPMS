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
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
     * The default name of the application context file.
     */
    private final static String DEFAULT_APP_CONTEXT_FNAME
            = "application-context.xml";

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * Caches loaded objects.
     */
    private final LoadCache cache = new LoadCache();

    /**
     * Specifies the file extension to filter. Defaults to xml.
     */
    private String extension = "xml";

    /**
     * Maintains a list of archetypes and count to indicate the number of
     * each saved or validated
     */
    private Map<String, Long> statistics = new HashMap<String, Long>();

    /**
     * Used to process the command line
     */
    private JSAP jsap = new JSAP();

    /**
     * The results once the commnad line has been processed
     */
    private JSAPResult config;


    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(StaxArchetypeDataLoader.class);


    /**
     * Process the records in the specified XML document
     *
     * @param args the command line
     */
    public StaxArchetypeDataLoader(String[] args) throws Exception {
        // set up the command line options and the logger 
        createOptions();

        // process the configuration 
        config = jsap.parse(args);
        if (!config.success()) {
            displayUsage();
        }
        // init
        init();
    }

    /**
     * The main line
     *
     * @param args the file where the data is stored is passed in as the first
     *             argument
     */
    public static void main(String[] args) throws Exception {
        StaxArchetypeDataLoader loader = new StaxArchetypeDataLoader(args);
        loader.load();
    }

    /**
     * Load all the elements
     *
     * @throws Exception
     */
    private void load() throws Exception {
        // set some of the configuration options
        boolean verbose = config.getBoolean("verbose");
        boolean validateOnly = config.getBoolean("validateOnly");
        int batchSize = config.getInt("batchSaveSize");

        Date start = new Date();
        DataLoader loader = new DataLoader(cache, service,
                                           verbose, validateOnly, batchSize,
                                           statistics);

        String file = config.getString("file");
        String dir = config.getString("dir");
        if (file != null || dir != null) {
            if (file != null) {
                processFile(file, loader);
            } else {
                // process the files
                processDir(dir, loader);
            }
            loader.close();

            // dump the statistics
            dumpStatistics(start);
        } else {
            displayUsage();
        }
    }

    /**
     * Process all the files in the directory
     *
     * @param dir the directory
     */
    private void processDir(String dir, DataLoader loader) throws Exception {
        String[] extensions = {extension};
        Collection collection = FileUtils.listFiles(new File(dir), extensions,
                                                    false);
        File[] files = FileUtils.convertFileCollectionToFileArray(collection);
        Arrays.sort(files);
        for (int i = 0, n = files.length; i < n; i++) {
            File file = files[i];
            processFile(file.getAbsolutePath(), loader);
            loader.flush();
        }
    }

    /**
     * Process the data elements.
     *
     * @param file the file to process
     *             if this is the first parse for this file
     * @throws Exception propagate all exceptions to client
     */
    private void processFile(String file, DataLoader loader) throws Exception {
        log.info("\n[PROCESSING FILE : " + file + "]\n");

        XMLStreamReader reader = getReader(file);
        loader.load(reader, file);
    }

    /**
     * Dump the statistics using the logger
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
     * Initialise and start the spring container
     */
    private void init() throws Exception {
        String contextFile = StringUtils.isEmpty(config.getString("context")) ?
                DEFAULT_APP_CONTEXT_FNAME : config.getString("context");

        log.info("Using application context [" + contextFile + "]");
        ApplicationContext context;
        if (!new File(contextFile).exists()) {
            context = new ClassPathXmlApplicationContext(contextFile);
        } else {
            context = new FileSystemXmlApplicationContext(contextFile);
        }

        service = (IArchetypeService) context.getBean("archetypeService");
    }

    /**
     * Return the XMLReader for the specified file
     *
     * @param file the file
     * @return XMLStreamReader
     * @throws Exception propagate to the caller
     */
    private XMLStreamReader getReader(String file) throws Exception {
        FileInputStream stream = new FileInputStream(file);
        XMLInputFactory factory = XMLInputFactory.newInstance();

        return factory.createXMLStreamReader(stream);
    }

    /**
     * Configure the options for this applications
     *
     * @throws Exception let the caller handle the error
     */
    private void createOptions() throws Exception {
        jsap.registerParameter(new FlaggedOption("context").setShortFlag('c')
                .setLongFlag("context").setDefault(DEFAULT_APP_CONTEXT_FNAME)
                .setHelp("Application context for the data loader"));
        jsap.registerParameter(new FlaggedOption("dir").setShortFlag('d')
                .setLongFlag("dir").setHelp(
                "Directory where data files reside."));
        jsap.registerParameter(new Switch("subdir").setShortFlag('s')
                .setLongFlag("subdir").setDefault("false").setHelp(
                "Search the subdirectories as well."));
        jsap.registerParameter(new FlaggedOption("file").setShortFlag('f')
                .setLongFlag("file").setHelp("Name of file containing data"));
        jsap.registerParameter(new Switch("verbose").setShortFlag('v')
                .setLongFlag("verbose").setDefault("false").setHelp(
                "Displays verbose info to the console."));
        jsap.registerParameter(new Switch("validateOnly")
                .setLongFlag("validateOnly").setDefault("false").setHelp(
                "Only validate the data file. Do not process."));
        jsap.registerParameter(new FlaggedOption("batchSaveSize")
                .setStringParser(JSAP.INTEGER_PARSER).setDefault("0")
                .setShortFlag('b').setLongFlag("batchSaveSize")
                .setHelp("The batch size for saving entities."));
    }

    /**
     * Print usage information
     */
    private void displayUsage() {
        System.err.println();
        System.err.println("Usage: java "
                + StaxArchetypeDataLoader.class.getName());
        System.err.println("                " + jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(1);
    }

}
