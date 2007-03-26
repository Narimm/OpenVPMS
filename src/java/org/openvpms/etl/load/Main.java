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

package org.openvpms.etl.load;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.etl.ETLValueDAO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.text.DecimalFormat;


/**
 * Main line for the {@link Loader}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Main {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(Main.class);

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";

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
                boolean validateOnly = config.getBoolean("validateOnly");
                String contextPath = config.getString("context");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }

                ETLValueDAO dao = (ETLValueDAO) context.getBean(
                        "ETLObjectDAO");
                IArchetypeService service = (IArchetypeService) context.getBean(
                        "archetypeService");
                Loader loader;
                if (validateOnly) {
                    loader = new ValidatingLoader(dao, service);
                } else {
                    loader = new Loader(dao, service);
                }
                long start = System.currentTimeMillis();
                int count = loader.load();
                long end = System.currentTimeMillis();
                dumpStats(count, start, end);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Dumps statistics.
     *
     * @param count the no. of objects saved
     * @param start the start time
     * @param end   the end time
     */
    private static void dumpStats(int count, long start, long end) {
        long elapsed = end - start;
        long hours = elapsed / DateUtils.MILLIS_IN_HOUR;
        long mins = (elapsed - hours * DateUtils.MILLIS_IN_HOUR)
                / DateUtils.MILLIS_IN_MINUTE;
        long secs = (elapsed - hours * DateUtils.MILLIS_IN_HOUR
                - mins * DateUtils.MILLIS_IN_MINUTE)
                / DateUtils.MILLIS_IN_SECOND;
        double rate = (double) count / (elapsed / DateUtils.MILLIS_IN_SECOND);
        String rateStr = new DecimalFormat("#,##0.00").format(rate);
        log.info("Processed " + count + " objects in " + hours + ":" +
                mins + ":" + secs + " (" + rateStr + " per sec)");
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new Switch("validateOnly")
                .setLongFlag("validateOnly").setDefault("false").setHelp(
                "Only validate the data file. Do not process."));
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
        System.err
                .println("Usage: java " + Loader.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }
}
