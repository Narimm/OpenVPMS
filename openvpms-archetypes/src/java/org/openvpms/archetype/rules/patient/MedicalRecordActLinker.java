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

package org.openvpms.archetype.rules.patient;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NamedQuery;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Tool to associate <em>act.patientMedication</em> and
 * <em>act.patientInvestigation*</em> acts to <em>act.patientClinicalEvent</em>
 * acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MedicalRecordActLinker {


    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Medical record rules.
     */
    private final MedicalRecordRules rules;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(
            MedicalRecordActLinker.class);

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";


    /**
     * Constructs a new <tt>MedicalRecordActLinker</tt>.
     *
     * @param service the archetype service
     */
    public MedicalRecordActLinker(IArchetypeService service) {
        this.service = service;
        this.rules = new MedicalRecordRules(service);
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

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }
                IArchetypeService service
                        = (IArchetypeService) context.getBean(
                        "archetypeService");
                MedicalRecordActLinker linker = new MedicalRecordActLinker(
                        service);
                linker.link();
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
    }

    /**
     * Links medical records for all patients.
     */
    public void link() {
        long start = System.currentTimeMillis();
        int total = 0;
        int count;
        while ((count = doLink()) != 0) {
            total += count;
        }
        long end = System.currentTimeMillis();
        double elapsed = (end - start) / 1000;
        double rate = (elapsed != 0) ? total / elapsed : 0;
        log.info(String.format(
                "Linked %d acts in %.2f seconds (%.2f objects/sec)",
                total, elapsed, rate));
    }

    /**
     * Links unlinked medical records.
     * Note that this method must be called repeatedly until the returned
     * count is zero. This is required as linking the medical records
     * affects query pagination.
     *
     * @return the no. of medical records linked
     */
    private int doLink() {
        NamedQuery query = new NamedQuery("getUnlinkedMedicalRecords");
        query.setMaxResults(100);
        Iterator<Act> iterator = new IMObjectQueryIterator<Act>(service,
                                                                query);
        List<Act> acts = new ArrayList<Act>();
        Date current = null;

        int count = 0;
        while (iterator.hasNext()) {
            Act act = iterator.next();
            Date date = DateRules.getDate(act.getActivityStartTime());
            if (current != null && !current.equals(date)) {
                rules.addToHistoricalEvents(acts, current);
                acts.clear();
            }
            current = date;
            acts.add(act);
            ++count;
        }
        if (!acts.isEmpty()) {
            rules.addToHistoricalEvents(acts, current);
        }
        return count;
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
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
                + MedicalRecordActLinker.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }
}
