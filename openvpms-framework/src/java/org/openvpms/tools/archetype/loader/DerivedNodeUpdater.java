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

package org.openvpms.tools.archetype.loader;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import com.martiansoftware.jsap.stringparsers.IntegerStringParser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Updates derived nodes on {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DerivedNodeUpdater {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The batch size.
     */
    private int batchSize = DEFAULT_BATCH_SIZE;

    /**
     * Determines if the updater should fail when a validation error occurs.
     */
    private boolean failOnError = true;

    /**
     * The default name of the application context file.
     */
    private final static String APPLICATION_CONTEXT
            = "applicationContext.xml";

    /**
     * The default batch size.
     */
    private static final int DEFAULT_BATCH_SIZE = 100;

    /**
     * The logger.
     */
    private static final Logger log = Logger.getLogger(ArchetypeLoader.class);


    /**
     * Creates a new <tt>DerivedFieldUpdater</tt>.
     *
     * @param service the archetype service
     */
    public DerivedNodeUpdater(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Sets the batch size.
     *
     * @param batchSize the batch size
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Determines if updating should fail when an error occurs.
     * Defaults to <tt>true</tt>.
     *
     * @param failOnError if <tt>true</tt> fail when a validation error occurs
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }


    /**
     * Updates derived fields for all objects with the specified archetype
     * short name.
     *
     * @param shortName the archetype short name. May contain wildcards.
     * @return the no. of updated objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public int update(String shortName) {
        return update(new String[]{shortName});
    }

    /**
     * Updates derived fields for all objects with the specified archetype
     * short names.
     *
     * @param shortNames the archetype short names. May contain wildcards.
     * @return the no. of updated objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    public int update(String[] shortNames) {
        if (log.isInfoEnabled()) {
            StringBuffer buff = new StringBuffer();
            for (String shortName : shortNames) {
                if (buff.length() != 0) {
                    buff.append(", ");
                }
                buff.append(shortName);
            }
            log.info("Updating: " + buff);
        }
        int saved = 0;
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, false);
        Iterator<IMObject> iter
                = new IMObjectQueryIterator<IMObject>(service, query);
        List<IMObject> batch = new ArrayList<IMObject>();
        while (iter.hasNext()) {
            IMObject object = iter.next();
            service.deriveValues(object);
            batch.add(object);
            if (batch.size() >= batchSize) {
                saved += saveBatch(batch);
            }
        }
        if (!batch.isEmpty()) {
            saved += saveBatch(batch);
        }
        if (log.isInfoEnabled()) {
            log.info("Updated " + saved + " objects");
        }
        return saved;
    }

    /**
     * Main line.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();

        // set the root logger level to error
        Logger root = Logger.getRootLogger();
        root.setLevel(Level.ERROR);
        root.removeAllAppenders();
        root.addAppender(new ConsoleAppender(new PatternLayout("%m%n")));

        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            if (!config.success()) {
                displayUsage(parser, config);
            } else {
                String contextPath = config.getString("context");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }

                IArchetypeService service = (IArchetypeService) context.getBean(
                        "archetypeService");

                if (config.getBoolean("verbose")) {
                    log.setLevel(Level.INFO);
                }

                DerivedNodeUpdater updater = new DerivedNodeUpdater(service);
                updater.setBatchSize(config.getInt("batchSize"));
                updater.setFailOnError(config.getBoolean("failOnError"));
                String archetype = config.getString("archetype");
                if (StringUtils.isEmpty(archetype)) {
                    displayUsage(parser, config);
                } else {
                    updater.update(archetype);
                }
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
    }

    /**
     * Saves a batch of objects.
     *
     * @param batch the batch to save
     * @return the no. of saved objects
     * @throws ArchetypeServiceException if an archetype service error occurs
     *                                   and <tt>failOnError</tt> is
     *                                   <tt>true</tt>
     */
    private int saveBatch(List<IMObject> batch) {
        int saved = 0;
        try {
            service.save(batch);
            saved = batch.size();
        } catch (OpenVPMSException exception) {
            if (failOnError) {
                throw exception;
            }
            for (IMObject object : batch) {
                try {
                    service.save(object);
                    ++saved;
                } catch (OpenVPMSException error) {
                    log.error("Failed to save object: " + object, error);
                }
            }
        }
        batch.clear();
        return saved;
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("archetype")
                .setShortFlag('a')
                .setLongFlag("archetype")
                .setHelp("The short name of the archetype to update. "
                + "May contain wildcards"));
        parser.registerParameter(new FlaggedOption("batchSize")
                .setLongFlag("batchSize")
                .setDefault("" + DEFAULT_BATCH_SIZE)
                .setStringParser(IntegerStringParser.getParser())
                .setHelp("The batch size"));
        parser.registerParameter(new FlaggedOption("failOnError")
                .setShortFlag('e')
                .setLongFlag("failOnError")
                .setDefault("true")
                .setStringParser(BooleanStringParser.getParser())
                .setHelp("Fail on validation error"));
        parser.registerParameter(new Switch("verbose")
                .setShortFlag('v')
                .setLongFlag("verbose")
                .setDefault("false")
                .setHelp("Displays verbose info to the console."));
        parser.registerParameter(new FlaggedOption("context")
                .setLongFlag("context")
                .setDefault(APPLICATION_CONTEXT)
                .setHelp("The application context path"));
        return parser;
    }

    /**
     * Prints usage information and exits.
     *
     * @param parser the parser
     * @param result the parsed result
     */
    private static void displayUsage(JSAP parser, JSAPResult result) {
        Iterator iter = result.getErrorMessageIterator();
        while (iter.hasNext()) {
            System.err.println(iter.next());
        }
        System.err.println();
        System.err.println("Usage: java " + DerivedNodeUpdater.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

}
