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


package org.openvpms.tools.archetype.loader;

// java core
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

//commons-lang
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

//log4j
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

// castor
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

// xml
import org.xml.sax.InputSource;

//jsap
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.Switch;

// hibernate
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.PropertyDescriptor;

/**
 * This utility will load all the archetypes in the specified directory
 * and load them in to the database. By default all files, which are in the
 * specified directory and have an 'adl' extension are processed. The user has 
 * the option of overriding the extension.
 * <p>
 * Additionally, the utility will check whether the specified archetype is 
 * already stored in the database. If it is and the overwrite flag is specified
 * then the version of the archetype is updated.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeLoader {
    /**
     * Define a logger for this class
     */
    private Logger logger;

    /**
     * The directory to search
     */
    private String dirName;
    
    /**
     * Indicates whether the directory search is recursive
     */
    private boolean subdir;
    
    /**
     * Specifies the file extension to filter. Defaults to adl
     */
    private String extension = "adl";
    
    /**
     * The file name to process. This option should not be used in 
     * conjunction with the directory.
     */
    private String fileName;
    
    /**
     * Indicates whether to display verbose information while loading the 
     * archetypes
     */
    private boolean verbose;
    
    /**
     * Indicates whether an archetype should override an existing
     * archetype (i.e. one that is already stored in the databse
     */
    private boolean overwrite;
    
    /**
     * Used to process the command line
     */
    private JSAP jsap = new JSAP();
    
    /**
     * The results once the commnad line has been processed
     */
    private JSAPResult config;
    
    /**
     * A reference to the hibernate session
     */
    private Session session;
    
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) 
    throws Exception {
        // create the loader
        ArchetypeLoader loader = new ArchetypeLoader(args);
        loader.load();
    }
    
    /**
     * Construct an instance of this class with the specified command
     * line arguments.
     * 
     * @param args
     *            the command line
     */
    protected ArchetypeLoader(String[] args) 
    throws RuntimeException {
        try {
            init();
            config = jsap.parse(args);
            if (!config.success()) {
                displayUsage();
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error in instantiating ArchetypeLoader", 
                    exception);
        }
    }
    

    /**
     * Execute the load. If a filename has been specified then it 
     * will simply load the specified file. If a directory is specified 
     * then it will load all the files the the specified extension
     * 
     * @throws Exception
     *            propagate to caller
     */
    protected void load() 
    throws Exception {
        if (config.getString("file") != null) {
            loadFromFile();
        } else if (config.getString("dir") != null) {
            loadFromDirectory();
        } else {
            displayUsage();
        }
    }

    /**
     * Scan the directory and optionally any sub directory and process all
     * ADL files
     * 
     * @throws Exception
     *            propagate exception to caller
     */
    private void loadFromDirectory() 
    throws Exception {
        // process options
        dirName = config.getString("dir");
        verbose = config.getBoolean("verbose");
        overwrite = config.getBoolean("overwrite");
        subdir = config.getBoolean("subdir");
        
        if (!StringUtils.isEmpty(dirName)) {
          File dir = new File(dirName);  
          if (dir.exists()) {
              String[] extensions = {extension};
              Collection collection = FileUtils.listFiles(dir, extensions, subdir);
              Iterator files = collection.iterator();
              while (files.hasNext()) {
                  processFile((File) files.next());
              }
          } else {
              throw new RuntimeException("Directory " + dirName 
                      + ", does not exist.");
          }
        } 
    }


    /**
     * Process the specified file and load all the defind archetypes
     * 
     * @throws Exception
     *            propagate the exception to the client
     */
    private void loadFromFile() 
    throws Exception {
        // process options
        fileName = config.getString("file");
        verbose = config.getBoolean("verbose");
        overwrite = config.getBoolean("overwrite");
        
        File file = new File(fileName);
        if (file.exists()) {
            processFile(file);
        } else {
            throw new RuntimeException("File " + fileName + " does not exist.");
        }
    }
    
    /**
     * Process the specified file and load all the defined archetypes in
     * the database.
     * 
     * @param file
     *            the file to process
     * @throws Exception
     *  `         propagate to caller            
     */
    private void processFile(File file) 
    throws Exception {
        if (verbose) {
            logger.info("Processing File: " + file);
        }
        
        // load the mapping file
        Mapping mapping = new Mapping();
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "org/openvpms/component/business/service/archetype/descriptor/archetype-mapping-file.xml"))));

        ArchetypeDescriptors adescs = (ArchetypeDescriptors)
            new Unmarshaller(mapping).unmarshal(new FileReader(file));
        for (ArchetypeDescriptor adesc : adescs.getArchetypeDescriptorsAsArray()) {
            if (verbose) {
                logger.info("Processing Archetype: " + adesc.getName());
            }
            
            Transaction tx = session.beginTransaction();
            try {
                Query query = session.getNamedQuery("archetypeDescriptor.getByName");
                query.setParameter("name", adesc.getName());
                if (query.list().size() > 0) {
                    // check if the overwtite flag has been specified
                    if (!overwrite) {
                        if (verbose) {
                            logger.info("Archetype " + adesc.getName()
                                    + " already exists. Not overwriting");
                        }
                        tx.commit();
                        continue;
                    }
                    
                    for (Object obj : query.list()) {
                        if (verbose) {
                            logger.info("Deleting Existing Archetype: " + 
                                    ((ArchetypeDescriptor)obj).getName());
                        }
                        session.delete(obj);
                    }
                } 
                
                if (verbose) {
                    logger.info("Creating Archetype: " + adesc.getName());
                }
                session.saveOrUpdate(adesc);
                tx.commit();
            } catch (Exception exception) {
                // rollback before rethrowing the exceptin
                tx.rollback();
                throw exception;
            }
        }
    }

    /**
     * Initialize the loader 
     * 
     * @throws Exception
     *            propagate exception to caller
     */
    private void init() 
    throws Exception {
        createLogger();
        createOptions();
        createSession();
    }
    
    /**
     * Configure the options for this applications
     * 
     * @throws Exception
     *            let the caller handle the error
     */
    private void createOptions() 
    throws Exception {
        jsap.registerParameter(new FlaggedOption("dir")
                .setShortFlag('d')
                .setHelp("Directory where ADL files reside."));
        jsap.registerParameter(new Switch("subdir")
                .setShortFlag('s')
                .setDefault("false")
                .setHelp("Search the subdirectories as well."));
        jsap.registerParameter(new FlaggedOption("file")
                .setShortFlag('f')
                .setHelp("Name of file containing archetypes"));
        jsap.registerParameter(new Switch("verbose")
                .setShortFlag('v')
                .setDefault("false")
                .setHelp("Displays verbose info to the console."));
        jsap.registerParameter(new Switch("overwrite")
                .setShortFlag('o')
                .setDefault("false")
                .setHelp("Overwrite archetype if it already exists"));
        
    }

    /**
     * Create a hibernate session, which can be used to load the
     * archetypes
     * 
     * @return Session
     */
    private void createSession() 
    throws Exception {
        // create the hibernate session factory
        Configuration config = new Configuration();
        config.addClass(ArchetypeDescriptor.class);
        config.addClass(NodeDescriptor.class);
        config.addClass(AssertionDescriptor.class);
        config.addClass(PropertyDescriptor.class);
        config.addClass(AssertionTypeDescriptor.class);
        
        session = config.buildSessionFactory().openSession();
    }
    
    /**
     * Creater the logger
     *
     * @throws Exception
     */
    private void createLogger() 
    throws Exception {
        BasicConfigurator.configure(); 

        // set the root logger level to error
        Logger.getRootLogger().setLevel(Level.ERROR);
        Logger.getRootLogger().removeAllAppenders();
        
        logger = Logger.getLogger(ArchetypeLoader.class);
        logger.setLevel(Level.INFO);
        logger.addAppender(new ConsoleAppender(new PatternLayout("%m%n")));
    }
    
    /**
     * Print usage information
     */
    private void displayUsage() {
        System.err.println();
        System.err.println("Usage: java "
                            + ArchetypeLoader.class.getName());
        System.err.println("                "
                            + jsap.getUsage());
        System.err.println();
        System.err.println(jsap.getHelp());
        System.exit(1);
        
    }
}
