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

package org.openvpms.archetype.rules.balance;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.Iterator;


/**
 * Generates balances for all customers.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerBalanceGenerator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The customer name. May be <code>null</code>
     */
    private final String name;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(
            CustomerBalanceGenerator.class);

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";


    /**
     * Constructs a new <code>CustomerBalanceGenerator</code>.
     *
     * @param service the archetype service
     * @param name    the customer name. May be <code>null</code>
     */
    public CustomerBalanceGenerator(IArchetypeService service, String name) {
        this.service = service;
        this.name = name;
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
                String name = config.getString("name");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }
                if (context.containsBean("ruleEngineProxyCreator")) {
                    throw new IllegalStateException(
                            "Rules must be disabled to run "
                                    + CustomerBalanceGenerator.class.getName());
                }
                IArchetypeService service
                        = (IArchetypeService) context.getBean(
                        "archetypeService");
                CustomerBalanceGenerator gen = new CustomerBalanceGenerator(
                        service, name);
                gen.generate();
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
    }

    /**
     * Generates account balance information for all customers.
     */
    public void generate() {
        ArchetypeQuery query = new ArchetypeQuery("party.customer*", true,
                                                  false);
        query.setMaxResults(100);
        if (name != null) {
            query.add(new NodeConstraint("name", name));
        }
        query.add(new NodeSortConstraint("name"));
        Iterator<Party> iterator = new IMObjectQueryIterator<Party>(service,
                                                                    query);
        while (iterator.hasNext()) {
            Party customer = iterator.next();
            log.info("Generating account balance for " + customer.getName());
            generate(customer);
        }
    }

    /**
     * Generates account balance information for a particular customer.
     *
     * @param customer the customer
     */
    private void generate(Party customer) {
        int acts = 0;
        int processed = 0;
        CustomerBalanceRules rules = new CustomerBalanceRules(service);
        ArchetypeQuery query = new ArchetypeQuery(
                CustomerBalanceRules.SHORT_NAMES, true, true);
        query.add(new NodeConstraint("status", ActStatus.POSTED));
        CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                "customer", "participation.customer", true, true);
        constraint.add(new ObjectRefNodeConstraint(
                "entity", customer.getObjectReference()));
        query.add(constraint);
        query.add(new NodeSortConstraint("startTime", true));

        Iterator<FinancialAct> iterator
                = new IMObjectQueryIterator<FinancialAct>(service, query);
        while (iterator.hasNext()) {
            ++acts;
            FinancialAct act = iterator.next();
            if (!rules.inBalance(act)) {
                if (act.getAllocatedAmount() == null) {
                    act.setAllocatedAmount(new Money(0));
                }
                try {
                    rules.addToBalance(act);
                    service.save(act);
                    rules.updateBalance(act);
                } catch (OpenVPMSException exception) {
                    log.error(exception, exception);
                }
                ++processed;
            }
        }
        log.info("\tprocessed " + processed + " of " + acts + " acts");
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("name").setShortFlag('n')
                .setLongFlag("name")
                .setHelp("Customer name. May contain wildcards"));
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
                + CustomerBalanceGenerator.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }
}
