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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.account;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.statement.StatementRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.print.PrintException;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReportContextFactory;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.im.report.TemplatedReporter;
import org.openvpms.web.component.print.AbstractPrinter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Prints account statements.
 * <p>
 * This supports printing:
 * <ul>
 * <li>the current incomplete statement</li>
 * <li>acts between an opening and closing balance.<br/>On completion the print flag will be set.</li>
 * <li>acts between two dates</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class StatementPrinter extends AbstractPrinter {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The reporter factory.
     */
    private final ReporterFactory factory;

    /**
     * The statement template.
     */
    private final DocumentTemplate template;

    /**
     * The statement rules.
     */
    private final StatementRules statementRules;

    /**
     * The account rules.
     */
    private CustomerAccountRules rules;

    /**
     * Determines if an actual statement being printed, or a preview.
     */
    private boolean current;

    /**
     * Determines if completed charges should be included when printing the current statement.
     */
    private boolean includeCompletedCharges;

    /**
     * Determines if an overdue fee should be included when printing the current statement.
     */
    private boolean includeFee;

    /**
     * The closing balance.
     */
    private FinancialAct closingBalance;

    /**
     * The from date.
     */
    private Date from;

    /**
     * The to date.
     */
    private Date to;

    /**
     * The reporter.
     */
    private TemplatedReporter<FinancialAct> reporter;


    /**
     * Constructs a {@link StatementPrinter}.
     *
     * @param context the context. Must contain the customer to print the statement for
     * @param rules   the customer account rules
     * @param factory the reporter factory
     * @param service the archetype service
     */
    public StatementPrinter(Context context, CustomerAccountRules rules, ReporterFactory factory,
                            IArchetypeService service) {
        super(service);
        this.rules = rules;
        this.context = context;
        this.factory = factory;
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(
                CustomerAccountArchetypes.OPENING_BALANCE, context);
        template = locator.getTemplate();
        statementRules = new StatementRules(context.getPractice(), service, rules);
    }

    /**
     * Returns the customer that the statement is for.
     *
     * @return the customer
     */
    public Party getCustomer() {
        return context.getCustomer();
    }

    /**
     * If invoked, indicates to print the current statement. <br/>
     * When {@link #print()} is invoked, a list of the acts from the last opening balance, will be printed, if any.<br/>
     * If there is no opening balance, prints all acts.
     *
     * @param includeCompletedCharges if {@code true}, include COMPLETED charges. By default, only POSTED charges
     *                                are included
     * @param includeFee              if {@code true}, include an accounting fee, if one is required
     */
    public void setPrintCurrent(boolean includeCompletedCharges, boolean includeFee) {
        initPrint(true, includeCompletedCharges, includeFee, null, null, null);
    }

    /**
     * If invoked, indicates to print the statement associated with the specified closing balance date.
     *
     * @param closingBalance the closing balance
     */
    public void setPrintStatement(FinancialAct closingBalance) {
        initPrint(false, false, false, closingBalance, null, null);
    }

    /**
     * If invoked, indicates to print all acts within the specified range.
     *
     * @param from the from date. May be {@code null}
     * @param to   the to date. If {@code null}, the current date will be used. All acts on the date will be included
     */
    public void setPrintRange(Date from, Date to) {
        initPrint(false, false, false, null, from, to);
    }

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be {@code null}
     * @throws PrintException    if {@code printer} is null and {@link #getDefaultPrinter()} also returns {@code null}
     * @throws OpenVPMSException for any error
     */
    public void print(String printer) {
        if (printer == null) {
            printer = getDefaultPrinter();
        }
        if (printer == null) {
            throw new PrintException(PrintException.ErrorCode.NoPrinter);
        }
        getReporter().print(getProperties(printer));
        if (closingBalance != null && !closingBalance.isPrinted()) {
            closingBalance.setPrinted(true);
            getService().save(closingBalance);
        }
    }

    /**
     * Returns the default printer for the object.
     *
     * @return the default printer for the object, or {@code null} if none is defined
     */
    @Override
    public String getDefaultPrinter() {
        String printer;
        if (template != null) {
            printer = getDefaultPrinter(template, context);
        } else {
            printer = getDefaultLocationPrinter(context.getLocation());
        }
        return printer;
    }

    /**
     * Returns a document corresponding to that which would be printed.
     *
     * @return a document
     * @throws OpenVPMSException for any error
     */
    @Override
    public Document getDocument() {
        return getReporter().getDocument();
    }

    /**
     * Returns a document corresponding to that which would be printed.
     * <p>
     * If the document cannot be converted to the specified mime-type, it will be returned unchanged.
     *
     * @param mimeType the mime type. If {@code null} the default mime type associated with the report will be used.
     * @param email    if {@code true} indicates that the document will be emailed. Documents generated from templates
     *                 can perform custom formatting
     * @return a document
     * @throws OpenVPMSException for any error
     */
    @Override
    public Document getDocument(String mimeType, boolean email) {
        return getReporter().getDocument(mimeType, email);
    }

    /**
     * Returns a display name for the objects being printed.
     *
     * @return a display name for the objects being printed
     */
    @Override
    public String getDisplayName() {
        return getReporter().getDisplayName();
    }

    /**
     * Sets the print parameters.
     *
     * @param current                 if {@code true} print the current statement
     * @param includeCompletedCharges if {@code true}, include COMPLETED charges. By default, only POSTED charges
     *                                are included. Only applies when {@code current == true}
     * @param includeFee              if {@code true}, include an accounting fee, if one is required.
     *                                Only applies when {@code current == true}
     * @param closingBalance          if not {@code null}, print the statement associated with the closing balance
     * @param from                    the from date, when printing a date range. May be {@code null}
     * @param to                      the to date, when printing a date range. If {@code null}, the current date will be
     *                                used. All acts on the date will be included
     */
    private void initPrint(boolean current, boolean includeCompletedCharges, boolean includeFee,
                           FinancialAct closingBalance, Date from, Date to) {
        this.current = current;
        this.includeCompletedCharges = includeCompletedCharges;
        this.includeFee = includeFee;
        this.closingBalance = closingBalance;
        this.from = from;
        this.to = to;
        this.reporter = null; // force the reporter to be created
    }

    /**
     * Returns the reporter, creating it if needed.
     *
     * @return the reporter
     */
    private TemplatedReporter<FinancialAct> getReporter() {
        if (reporter == null) {
            Iterable<FinancialAct> iterable;
            Party customer = getCustomer();
            Date date;
            if (current) {
                date = new Date();
                iterable = statementRules.getStatementPreview(customer, date, includeCompletedCharges, includeFee);
            } else if (closingBalance != null) {
                date = closingBalance.getActivityStartTime();
                iterable = statementRules.getStatement(customer, date);
            } else {
                date = (to != null) ? to : new Date();
                Date end = (to != null) ? DateRules.getNextDate(to) : null;
                iterable = statementRules.getStatementRange(customer, from, end);
            }
            // create an iterable that will insert a dummy opening balance if the statement is empty
            iterable = new NonEmptyStatement(iterable, date);
            reporter = factory.create(iterable, template, TemplatedReporter.class);
            reporter.setFields(ReportContextFactory.create(context));
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("statementDate", date);
            reporter.setParameters(parameters);
        }
        return reporter;
    }

    /**
     * An {@link Iterable} that inserts a dummy opening balance if the underlying iterator has no results.
     */
    private class NonEmptyStatement implements Iterable<FinancialAct> {

        private final Iterable<FinancialAct> iterable;

        private final Date date;

        /**
         * Constructs a {@link NonEmptyStatement}.
         *
         * @param iterable the underlying iterable
         * @param date     the date to use to create a dummy opening balance, if the iterator has no results
         */
        public NonEmptyStatement(Iterable<FinancialAct> iterable, Date date) {
            this.iterable = iterable;
            this.date = date;
        }

        /**
         * Returns an iterator over the query results.
         *
         * @return an iterator.
         */
        @Override
        public Iterator<FinancialAct> iterator() {
            Iterator<FinancialAct> iterator = iterable.iterator();
            if (!iterator.hasNext()) {
                BigDecimal balance = rules.getBalance(getCustomer(), date);
                FinancialAct openingBalance = rules.createOpeningBalance(getCustomer(), date, balance);
                iterator = Collections.singletonList(openingBalance).iterator();
            }
            return iterator;
        }
    }
}
