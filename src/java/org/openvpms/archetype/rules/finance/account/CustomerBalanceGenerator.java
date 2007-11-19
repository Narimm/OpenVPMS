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

package org.openvpms.archetype.rules.finance.account;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes.ACCOUNT_ALLOCATION_SHORTNAME;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountActTypes.ACCOUNT_BALANCE_SHORTNAME;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.ruleengine.DroolsRuleEngine;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Tool to generate account balances for customers. This is intended to be
 * used when:
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
public class CustomerBalanceGenerator {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Determines if account balances should be regenerated.
     */
    private boolean regenerate;

    /**
     * The customer account rules.
     */
    private final CustomerAccountRules rules;

    /**
     * Determines if posted acts should be processed.
     */
    private final boolean posted;

    /**
     * Determines if unposted acts should be processed.
     */
    private final boolean unposted;

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
            CustomerBalanceGenerator.class);

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";


    /**
     * Constructs a new <tt>CustomerBalanceGenerator</tt>, including
     * both posted and unposted acts.
     *
     * @param service the archetype service
     * @throws IllegalStateException if rules are enabled on the archetype
     *                               service
     */
    public CustomerBalanceGenerator(IArchetypeService service) {
        this(service, true, true);
    }

    /**
     * Constructs a new <tt>CustomerBalanceGenerator</tt>.
     *
     * @param service  the archetype service
     * @param posted   if <tt>true</tt> include posted acts
     * @param unposted if <tt>true</tt> include unposted acts
     * @throws IllegalStateException if rules are enabled on the archetype
     *                               service
     */
    public CustomerBalanceGenerator(IArchetypeService service,
                                    boolean posted, boolean unposted) {
        if (service instanceof Advised) {
            Advised advised = (Advised) service;
            for (Advisor advisor : advised.getAdvisors()) {
                if (advisor.getAdvice() instanceof DroolsRuleEngine) {
                    throw new IllegalStateException(
                            "Rules must be disabled to run "
                                    + CustomerBalanceGenerator.class.getName());
                }
            }
        }
        this.service = service;
        rules = new CustomerAccountRules(service);
        this.posted = posted;
        this.unposted = unposted;
    }

    /**
     * Determines if account balances should be regenerated.
     *
     * @param regenerate if <tt>true</tt> regenerate balances
     */
    public void setRegenerate(boolean regenerate) {
        this.regenerate = regenerate;
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
        log.info("Generating account balance for " + customer.getName());
        Generator generator = new Generator(customer, posted, unposted);
        generator.apply();
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
                + ", ID=" + customer.getUid());
        BalanceCalculator calc = new BalanceCalculator(service);
        BigDecimal expected = calc.getDefinitiveBalance(customer);
        BigDecimal actual = calc.getBalance(customer);
        boolean result = expected.compareTo(actual) == 0;
        if (!result) {
            log.error("Failed to check account balance for "
                    + customer.getName() + ", ID=" + customer.getUid()
                    + ": expected balance=" + expected
                    + ", actual balance=" + actual);
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
                boolean regenerate = config.getBoolean("regenerate");
                boolean posted = config.getBoolean("posted");
                boolean unposted = config.getBoolean("unposted");
                boolean check = config.getBoolean("check");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }
                IArchetypeService service = (IArchetypeService) context.getBean(
                        "archetypeService");
                CustomerBalanceGenerator generator
                        = new CustomerBalanceGenerator(service, posted,
                                                       unposted);
                if (check) {
                    if (id == -1) {
                        generator.check(name);
                    } else {
                        generator.check(id);
                    }
                } else if (generate || regenerate) {
                    generator.setRegenerate(regenerate);
                    generator.setFailOnError(config.getBoolean("failOnError"));
                    if (id == -1) {
                        generator.generate(name);
                    } else {
                        generator.generate(id);
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
        ArchetypeQuery query = new ArchetypeQuery("party.customer*", true,
                                                  false);
        query.setMaxResults(1000);
        if (!StringUtils.isEmpty(name)) {
            query.add(new NodeConstraint("name", name));
        }
        query.add(new NodeSortConstraint("name"));
        query.add(new NodeSortConstraint("uid"));
        return query;
    }

    /**
     * Creates a query on customer id.
     *
     * @param id the customer identifier
     * @return a new query
     */
    private ArchetypeQuery createIdQuery(long id) {
        ArchetypeQuery query = new ArchetypeQuery("party.customer*", true,
                                                  false);
        query.add(new NodeConstraint("uid", id));
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
        parser.registerParameter(new FlaggedOption("posted").setShortFlag('p')
                .setLongFlag("posted")
                .setStringParser(JSAP.BOOLEAN_PARSER).setDefault("true")
                .setHelp("Process POSTED acts."));
        parser.registerParameter(new FlaggedOption("unposted").setShortFlag('u')
                .setLongFlag("unposted")
                .setStringParser(JSAP.BOOLEAN_PARSER).setDefault("true")
                .setHelp("Process acts that aren't POSTED."));
        parser.registerParameter(new Switch("check").setShortFlag('c')
                .setLongFlag("check").setDefault("false")
                .setHelp("Check account balances."));
        parser.registerParameter(new Switch("generate").setShortFlag('g')
                .setLongFlag("generate").setDefault("false")
                .setHelp("Check account balances."));
        parser.registerParameter(new Switch("regenerate").setShortFlag('r')
                .setLongFlag("regenerate").setDefault("false")
                .setHelp("Regenerate account balances."));
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

    /**
     * Generates account balances for a specific customer.
     */
    private class Generator {

        /**
         * Iterator over the customer's financial acts.
         */
        private final Iterator<FinancialAct> iterator;

        /**
         * The unallocated acts.
         */
        private final List<FinancialAct> unallocated
                = new ArrayList<FinancialAct>();

        /**
         * The modified acts, with associated versions, to determine if
         * they need to be saved.
         */
        private final Map<FinancialAct, Long> modified
                = new HashMap<FinancialAct, Long>();

        /**
         * Total no. of acts
         */
        private int acts;


        /**
         * Constructs a new <tt>Generator</tt> for a customer.
         *
         * @param customer the customer
         * @param posted   if <tt>true</tt> include posted acts
         * @param unposted if <tt>true</tt> include unposted acts
         */
        public Generator(Party customer, boolean posted, boolean unposted) {
            iterator = getActs(customer, posted, unposted);
        }

        /**
         * Generate (or regenerate) the balance for the customer.
         */
        public void apply() {
            if (regenerate) {
                regenerate();
            } else {
                generate();
            }
            log.info("\tprocessed " + modified.size() + " of " + acts
                    + " acts");
        }

        /**
         * Generates the balance for the customer.
         */
        private void generate() {
            FinancialAct act;
            while ((act = getNext()) != null) {
                if (act.getAllocatedAmount() == null) {
                    act.setAllocatedAmount(Money.ZERO);
                    modified(act);
                }
                if (act.getTotal() == null) {
                    act.setTotal(Money.ZERO);
                    modified(act);
                }
                ActBean bean = new ActBean(act, service);
                for (ActRelationship relationship : bean.getRelationships(
                        "actRelationship.customerAccountAllocation")) {
                    // early versions may not have defaulted the allocatedAmount
                    // to zero
                    IMObjectBean relBean = new IMObjectBean(relationship,
                                                            service);
                    if (relBean.getMoney("allocatedAmount") == null) {
                        relBean.setValue("allocatedAmount", Money.ZERO);
                        modified(act);
                    }
                }
                if (!rules.inBalance(act)) {
                    rules.addToBalance(act);
                    modified(act);
                } else if (act.getTotal().compareTo(
                        act.getAllocatedAmount()) != 0) {
                    // act partially allocated
                    unallocated.add(act);
                }
            }
            save();
        }

        /**
         * Regenerates the balance for the customer.
         */
        private void regenerate() {
            FinancialAct act;
            while ((act = getNext()) != null) {
                Money allocated = act.getAllocatedAmount();
                if (allocated == null || allocated.compareTo(Money.ZERO) != 0) {
                    act.setAllocatedAmount(Money.ZERO);
                    modified(act);
                }
                if (act.getTotal() == null) {
                    act.setTotal(Money.ZERO);
                    modified(act);
                }
                ActBean bean = new ActBean(act, service);
                if (bean.removeParticipation(ACCOUNT_BALANCE_SHORTNAME)
                        != null) {
                    modified(act);
                }
                for (ActRelationship relationship
                        : bean.getRelationships(ACCOUNT_ALLOCATION_SHORTNAME)) {
                    bean.removeRelationship(relationship);
                    modified(act);
                }
                if (!rules.inBalance(act)) { // false for 0 totals
                    rules.addToBalance(act);
                    unallocated.add(act);
                }
            }
            save();
        }

        /**
         * Returns the next available act.
         *
         * @return the next available act, or <tt>null</tt>
         */
        private FinancialAct getNext() {
            if (iterator.hasNext()) {
                ++acts;
                return iterator.next();
            }
            return null;
        }

        /**
         * Marks an act as being modified, and adds to the list of unallocated
         * acts.
         *
         * @param act the act
         */
        private void modified(FinancialAct act) {
            if (modified.put(act, act.getVersion()) == null) {
                unallocated.add(act);
            }
        }

        /**
         * Saves unallocated acts.
         */
        private void save() {
            if (!unallocated.isEmpty()) {
                // Update the customer balance. This will save any acts
                // that it changes. Need to check versions to determine if
                // the acts that this method has changed also need to be
                // saved
                List<FinancialAct> updated = rules.updateBalance(
                        null, unallocated.iterator());
                Set<FinancialAct> unsaved
                        = new HashSet<FinancialAct>(modified.keySet());
                unsaved.removeAll(updated);
                for (FinancialAct act : unsaved) {
                    service.save(act);
                }
            }
        }

        /**
         * Returns an iterator over the debit/credit acts for a customer.
         *
         * @param customer the customer
         * @param posted   if <tt>true</tt> include posted acts
         * @param unposted if <tt>true</tt> include unposted acts
         * @return an iterator of debit/credit acts
         */
        private Iterator<FinancialAct> getActs(Party customer, boolean posted,
                                               boolean unposted) {
            String[] shortNames
                    = CustomerAccountActTypes.DEBIT_CREDIT_SHORT_NAMES;
            ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
            if (!(posted && unposted)) {
                RelationalOp op = (posted) ? RelationalOp.EQ : RelationalOp.NE;
                query.add(new NodeConstraint("status", op,
                                             FinancialActStatus.POSTED));
            }
            CollectionNodeConstraint constraint = new CollectionNodeConstraint(
                    "customer", "participation.customer", true, true);
            constraint.add(new ObjectRefNodeConstraint(
                    "entity", customer.getObjectReference()));
            query.add(constraint);
            query.add(new NodeSortConstraint("startTime", true));
            query.add(new NodeSortConstraint("uid", true));
            OrConstraint or = new OrConstraint();
            for (String shortName : shortNames) {
                // duplicate the act short names onto the participation act
                // references, to force utilisation of the (faster)
                // participation index. Ideally would only need to specify the
                // act short names on participations, but this isn't supported
                // by ArchetypeQuery.
                ObjectRefNodeConstraint ref = new ObjectRefNodeConstraint(
                        "act", new ArchetypeId(shortName));
                or.add(ref);
            }
            constraint.add(or);
            query.setMaxResults(1000);
            return new IMObjectQueryIterator<FinancialAct>(service, query);
        }

    }
}
