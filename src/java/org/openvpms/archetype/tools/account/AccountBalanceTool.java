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

package org.openvpms.archetype.tools.account;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.account.BalanceCalculator;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRuleException;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceGenerator;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


/**
 * Tool to check and generate account balances for customers. This is intended
 * to be used when:
 * <ul>
 * <li>customer financial acts have been imported into the database that
 * don't contain account balance information</li>
 * <li>acccunt balance information must be regenerated for one or more
 * customers</li>
 * </ul>
 * This must be run with rules disabled.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AccountBalanceTool {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Determines if the generator should fail on error.
     */
    private boolean failOnError = false;

    /**
     * The no. of errors encountered.
     */
    private int errors = 0;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(
            AccountBalanceTool.class);

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";

    /**
     * The short names to query.
     */
    private static final String[] SHORT_NAMES = new String[]{
            CustomerArchetypes.PERSON, CustomerArchetypes.OTC};


    /**
     * Constructs a new <tt>AccountBalanceTool</tt>.
     *
     * @param service the archetype service
     * @throws IllegalStateException if rules are enabled on the archetype
     *                               service
     */
    public AccountBalanceTool(IArchetypeService service) {
        if (service instanceof IArchetypeRuleService) {
            throw new IllegalStateException(
                    "Rules must be disabled to run "
                            + AccountBalanceTool.class.getName());
        }
        this.service = service;
    }

    /**
     * Generates account balance information for all customers matching
     * the specified name.
     *
     * @param name the customer name. May be <tt>null</tt>
     */
    public void generate(String name) {
        ArchetypeQuery query = createNameQuery(name);
        generate(query);
    }

    /**
     * Generates account balance information for the customer with the specified
     * id.
     *
     * @param id the customer identifier
     */
    public void generate(long id) {
        ArchetypeQuery query = createIdQuery(id);
        generate(query);
    }

    /**
     * Generates account balance information for the specified customer.
     *
     * @param customer the customer
     */
    public void generate(Party customer) {
        log.info("Generating account balance for " + customer.getName()
                + ", ID=" + customer.getId());
        BalanceCalculator calc = new BalanceCalculator(service);
        BigDecimal oldBalance = calc.getBalance(customer);
        Generator generator = new Generator(customer);
        BigDecimal balance = generator.generate();
        log.info("\tProcessed " + generator.getModified() + " of "
                + generator.getProcessed() + " acts");
        log.info("\tUpdated account balance from " + oldBalance + " to "
                + balance);
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
     * Checks the account balance for all customers matching the specified name.
     *
     * @param name the customer name. May be <tt>null</tt>
     * @return <tt>true</tt> if the account balance is correct,
     *         otherwise <tt>false</tt>
     */
    public boolean check(String name) {
        ArchetypeQuery query = createNameQuery(name);
        return check(query);
    }

    /**
     * Checks the account balance for the customer with the specified id.
     *
     * @param id the customer identifier
     * @return <tt>true</tt> if the account balance is correct,
     *         otherwise <tt>false</tt>
     */
    public boolean check(long id) {
        ArchetypeQuery query = createIdQuery(id);
        return check(query);
    }

    /**
     * Checks the account balance for the specified customer.
     *
     * @param customer the customer
     * @return <tt>true</tt> if the account balance is correct,
     *         otherwise <tt>false</tt>
     */
    public boolean check(Party customer) {
        log.info("Checking account balance for " + customer.getName()
                + ", ID=" + customer.getId());
        BalanceCalculator calc = new BalanceCalculator(service);
        boolean result = false;
        try {
            BigDecimal expected = calc.getDefinitiveBalance(customer);
            BigDecimal actual = calc.getBalance(customer);
            result = expected.compareTo(actual) == 0;
            if (!result) {
                log.error("Failed to check account balance for "
                        + customer.getName() + ", ID=" + customer.getId()
                        + ": expected balance=" + expected
                        + ", actual balance=" + actual);
            }
        } catch (CustomerAccountRuleException exception) {
            // thrown when an opening or closing balance doesn't match
            log.error("Failed to check account balance for "
                    + customer.getName() + ", ID=" + customer.getId()
                    + ": " + exception.getMessage());
        }
        return result;
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
                long id = config.getLong("id");
                boolean generate = config.getBoolean("generate");
                boolean check = config.getBoolean("check");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }
                IArchetypeService service = (IArchetypeService) context.getBean(
                        "archetypeService");
                AccountBalanceTool tool = new AccountBalanceTool(service);
                if (check) {
                    if (id == -1) {
                        tool.check(name);
                    } else {
                        tool.check(id);
                    }
                } else if (generate) {
                    tool.setFailOnError(config.getBoolean("failOnError"));
                    if (id == -1) {
                        tool.generate(name);
                    } else {
                        tool.generate(id);
                    }
                } else {
                    displayUsage(parser);
                }
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
    }

    /**
     * Generates account balances for all customers matching the specified
     * query.
     *
     * @param query the query
     */
    private void generate(ArchetypeQuery query) {
        Collection<String> nodes = Arrays.asList("name");
        Iterator<Party> iterator
                = new IMObjectQueryIterator<Party>(service, query, nodes);
        int count = 0;
        while (iterator.hasNext()) {
            Party customer = iterator.next();
            try {
                generate(customer);
                ++count;
            } catch (OpenVPMSException exception) {
                if (failOnError) {
                    throw exception;
                } else {
                    ++errors;
                    log.error("Failed to generate account balance for "
                            + customer.getName(), exception);
                }
            }
        }
        log.info("Generated account balances for " + count + " customers");
        if (errors != 0) {
            log.warn("There were " + errors + " errors");
        } else {
            log.info("There were no errors");
        }
    }

    /**
     * Checks account balances for all customers matching the specified
     * query.
     *
     * @param query the query
     * @return <tt>true</tt> if the account balances are correct otherwise <tt>false</tt>
     */
    private boolean check(ArchetypeQuery query) {
        boolean result = true;
        Collection<String> nodes = Arrays.asList("name");
        Iterator<Party> iterator
                = new IMObjectQueryIterator<Party>(service, query, nodes);
        int count = 0;
        while (iterator.hasNext()) {
            Party customer = iterator.next();
            try {
                boolean ok = check(customer);
                ++count;
                if (!ok) {
                    result = false;
                    if (failOnError) {
                        return result;
                    } else {
                        ++errors;
                    }
                }
            } catch (OpenVPMSException exception) {
                if (failOnError) {
                    throw exception;
                } else {
                    ++errors;
                    log.error("Failed to check account balance for "
                            + customer.getName(), exception);
                }
            }
        }
        log.info("Checked account balances for " + count + " customers");
        if (errors != 0) {
            log.warn("There were " + errors + " errors");
        } else {
            log.info("There were no errors");
        }
        return result;
    }


    /**
     * Creates a query on customer name.
     *
     * @param name the customer name/ May be <tt>null</tt>
     * @return a new query
     */
    private ArchetypeQuery createNameQuery(String name) {
        ArchetypeQuery query = new ArchetypeQuery(SHORT_NAMES, true, false);
        query.setMaxResults(1000);
        if (!StringUtils.isEmpty(name)) {
            query.add(new NodeConstraint("name", name));
        }
        query.add(new NodeSortConstraint("name"));
        query.add(new NodeSortConstraint("id"));
        return query;
    }

    /**
     * Creates a query on customer id.
     *
     * @param id the customer identifier
     * @return a new query
     */
    private ArchetypeQuery createIdQuery(long id) {
        ArchetypeQuery query = new ArchetypeQuery(SHORT_NAMES, true, false);
        query.add(new NodeConstraint("id", id));
        return query;
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
        parser.registerParameter(new FlaggedOption("id").setShortFlag('i')
                .setLongFlag("id")
                .setStringParser(JSAP.LONG_PARSER).setDefault("-1")
                .setHelp("Customer identifier."));
        parser.registerParameter(new Switch("check").setShortFlag('c')
                .setLongFlag("check").setDefault("false")
                .setHelp("Check account balances."));
        parser.registerParameter(new Switch("generate").setShortFlag('g')
                .setLongFlag("generate").setDefault("false")
                .setHelp("Generate account balances."));
        parser.registerParameter(new FlaggedOption("failOnError")
                .setShortFlag('e')
                .setLongFlag("failOnError")
                .setDefault("false")
                .setStringParser(BooleanStringParser.getParser())
                .setHelp("Fail on error"));
        parser.registerParameter(new FlaggedOption("context")
                .setLongFlag("context")
                .setDefault(APPLICATION_CONTEXT)
                .setHelp("Application context path"));
        return parser;
    }

    /**
     * Prints usage information.
     *
     * @param parser the command line parser
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err.println("Usage: java "
                + AccountBalanceTool.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

    private class Generator extends CustomerBalanceGenerator {
        /**
         * Constructs a new <tt>Generator</tt> for a customer.
         *
         * @param customer the customer
         */
        public Generator(Party customer) {
            super(customer, service);
        }

        /**
         * Invoked when an act is changed.
         * <p/>
         * This method is a no-op.
         *
         * @param act       the act
         * @param fromTotal the original total
         * @param toTotal   the new total
         */
        @Override
        protected void changed(FinancialAct act, BigDecimal fromTotal,
                               BigDecimal toTotal) {
            String displayName = DescriptorHelper.getDisplayName(act, service);
            log.warn("Updated " + displayName
                    + " dated " + act.getActivityStartTime()
                    + " from " + fromTotal + " to " + toTotal);
        }
    }

}
