/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.reporting.statement;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceSummaryQuery;
import org.openvpms.archetype.rules.finance.statement.Statement;
import org.openvpms.archetype.rules.finance.statement.StatementProcessor;
import org.openvpms.archetype.rules.finance.statement.StatementProcessorException;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Statement generator.
 *
 * @author Tim Anderson
 */
class StatementGenerator extends AbstractStatementGenerator {

    /**
     * The statement processor progress bar.
     */
    private StatementProgressBarProcessor progressBarProcessor;

    /**
     * The processor.
     */
    private StatementProcessor processor;

    /**
     * Constructs a new {@code StatementGenerator} for a single customer.
     *
     * @param customer    the customer reference
     * @param date        the statement date
     * @param printOnly   if {@code true} only print statements
     * @param context     the context
     * @param mailContext the mail context
     * @param help        the help context
     */
    public StatementGenerator(IMObjectReference customer, Date date, boolean printOnly, Context context,
                              MailContext mailContext, HelpContext help) {
        super(Messages.get("reporting.statements.run.title"),
              Messages.get("reporting.statements.run.cancel.title"),
              Messages.get("reporting.statements.run.cancel.message"),
              Messages.get("reporting.statements.run.retry.title"));
        List<Party> customers = new ArrayList<Party>();
        Party party = (Party) IMObjectHelper.getObject(customer, context);
        if (party != null) {
            customers.add(party);
        }
        init(customers, date, printOnly, context, mailContext, help);
    }

    /**
     * Constructs a new {@code StatementGenerator} for statements returned by a query.
     *
     * @param query       the query
     * @param context     the context
     * @param mailContext the mail context
     * @param help        the help context
     */
    public StatementGenerator(CustomerBalanceQuery query, Context context, MailContext mailContext, HelpContext help) {
        super(Messages.get("reporting.statements.run.title"),
              Messages.get("reporting.statements.run.cancel.title"),
              Messages.get("reporting.statements.run.cancel.message"),
              Messages.get("reporting.statements.run.retry.title"));
        List<ObjectSet> balances = query.getObjects();
        List<Party> customers = new ArrayList<Party>();
        for (ObjectSet set : balances) {
            BigDecimal balance
                = set.getBigDecimal(CustomerBalanceSummaryQuery.BALANCE);
            if (BigDecimal.ZERO.compareTo(balance) != 0) {
                // only include customers with non-zero balances
                IMObjectReference ref = set.getReference(
                    CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
                Party customer = (Party) IMObjectHelper.getObject(ref, context);
                if (customer != null) {
                    customers.add(customer);
                }
            }
        }
        init(customers, query.getDate(), false, context, mailContext, help);
    }

    /**
     * Determines if statements that have been printed should be reprinted.
     * A statement is printed if the printed flag of its
     * <em>act.customerAccountOpeningBalance</em> is {@code true}.
     * Defaults to {@code false}.
     *
     * @param reprint if {@code true}, process statements that have been
     *                printed.
     */
    public void setReprint(boolean reprint) {
        processor.setReprint(reprint);
    }

    /**
     * Returns the processor.
     *
     * @return the processor
     */
    protected StatementProgressBarProcessor getProcessor() {
        return progressBarProcessor;
    }

    /**
     * Initialises this.
     *
     * @param customers   the customers to generate statements for
     * @param date        the statement date
     * @param printOnly   if {@code true}, only print statements
     * @param context     the context
     * @param mailContext the mail context
     * @param help        the help context
     * @throws ArchetypeServiceException   for any archetype service error
     * @throws StatementProcessorException for any statement processor exception
     */
    private void init(List<Party> customers, Date date, boolean printOnly, Context context, MailContext mailContext,
                      HelpContext help) {
        Party practice = context.getPractice();
        if (practice == null) {
            throw new StatementProcessorException(StatementProcessorException.ErrorCode.InvalidConfiguration,
                                                  "Context has no practice");
        }
        Contact email = getEmail(practice);
        if (email == null) {
            throw new StatementProcessorException(StatementProcessorException.ErrorCode.InvalidConfiguration,
                                                  "Practice " + practice.getName()
                                                  + " has no email contact for statements");
        }
        IMObjectBean bean = new IMObjectBean(email);
        String address = bean.getString("emailAddress");
        String name = practice.getName();
        if (StringUtils.isEmpty(address)) {
            throw new StatementProcessorException(StatementProcessorException.ErrorCode.InvalidConfiguration,
                                                  "Practice " + practice.getName() + " email contact address is empty");
        }

        processor = new StatementProcessor(date, practice);
        progressBarProcessor = new StatementProgressBarProcessor(processor, customers);

        StatementPrintProcessor printer = new StatementPrintProcessor(progressBarProcessor, getCancelListener(),
                                                                      practice, context, mailContext, help);
        if (printOnly) {
            processor.addListener(printer);
            printer.setUpdatePrinted(false);
        } else {
            StatementEmailProcessor mailer = new StatementEmailProcessor(ServiceHelper.getMailSender(), address, name,
                                                                         practice);
            processor.addListener(new StatementDelegator(printer, mailer));
        }
    }

    /**
     * Returns an email contact for the practice.
     *
     * @param practice the practice
     * @return an email contact, or {@code null} if none is configured
     */
    private Contact getEmail(Party practice) {
        PartyRules rules = new PartyRules(ServiceHelper.getArchetypeService());
        return rules.getContact(practice, ContactArchetypes.EMAIL, "BILLING");
    }

    private class StatementDelegator implements ProcessorListener<Statement> {

        private ProcessorListener<Statement> printer;
        private ProcessorListener<Statement> mailer;

        public StatementDelegator(ProcessorListener<Statement> printer,
                                  ProcessorListener<Statement> mailer) {
            this.printer = printer;
            this.mailer = mailer;
        }

        /**
         * Process a statement.
         *
         * @param statement the statement to process
         * @throws OpenVPMSException for any error
         */
        public void process(Statement statement) {
            ProcessorListener<Statement> listener = printer;
            List<Contact> contacts = statement.getContacts();
            if (contacts.size() >= 1) {
                Contact contact = contacts.get(0);
                if (TypeHelper.isA(contact, ContactArchetypes.EMAIL)) {
                    listener = mailer;
                }
            }
            listener.process(statement);
        }
    }

}
