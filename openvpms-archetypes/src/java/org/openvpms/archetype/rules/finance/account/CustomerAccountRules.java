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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.account;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.archetype.rules.finance.till.TillBalanceRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.openvpms.archetype.rules.act.ActStatus.IN_PROGRESS;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DEBITS;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.OPENING_BALANCE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND;


/**
 * Customer account rules.
 *
 * @author Tim Anderson
 */
public class CustomerAccountRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The rule based archetype service.
     */
    private final IArchetypeRuleService ruleService;

    /**
     * The transaction manager.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * Balance calculator.
     */
    private final BalanceCalculator calculator;


    /**
     * Constructs a {@link CustomerAccountRules}.
     *
     * @param service            the archetype service
     * @param ruleService        the rule based archetype service
     * @param transactionManager the transaction manager
     */
    public CustomerAccountRules(IArchetypeService service, IArchetypeRuleService ruleService,
                                PlatformTransactionManager transactionManager) {
        // NOTE: need an IArchetypeRuleService as the reverse() methods need to fire rules to update the balance
        if (service instanceof IArchetypeRuleService) {
            throw new IllegalArgumentException("Argument 'service' should not implement IArchetypeRuleService");
        }
        this.service = service;
        this.ruleService = ruleService;
        this.transactionManager = transactionManager;
        calculator = new BalanceCalculator(service);
    }

    /**
     * Returns the opening balance before the specified date.
     *
     * @param date the date
     * @return the opening balance, or {@code null} if none is found
     */
    public FinancialAct getOpeningBalanceBefore(Party customer, Date date) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(customer, OPENING_BALANCE);
        query.add(Constraints.lt("startTime", date));
        query.add(Constraints.sort("startTime", false));
        query.add(Constraints.sort("id", false));
        query.setMaxResults(1);
        Iterator<FinancialAct> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Returns the opening balance after the specified date.
     *
     * @param date the date
     * @return the opening balance, or {@code null} if none is found
     */
    public FinancialAct getOpeningBalanceAfter(Party customer, Date date) {
        ArchetypeQuery query = CustomerAccountQueryFactory.createQuery(customer, OPENING_BALANCE);
        query.add(Constraints.gt("startTime", date));
        query.add(Constraints.sort("startTime", true));
        query.add(Constraints.sort("id", false));
        query.setMaxResults(1);
        Iterator<FinancialAct> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Creates a new opening balance for a customer.
     *
     * @param customer the customer
     * @param date     the act date
     * @param amount   the amount. May be negative
     * @return a new opening balance
     */
    public FinancialAct createOpeningBalance(Party customer, Date date, BigDecimal amount) {
        return createBalance(OPENING_BALANCE, customer, date, amount);
    }

    /**
     * Creates a new closing balance for a customer.
     *
     * @param customer the customer
     * @param date     the act date
     * @param amount   the amount. May be negative
     * @return a new closing balance
     */
    public FinancialAct createClosingBalance(Party customer, Date date, BigDecimal amount) {
        return createBalance(CustomerAccountArchetypes.CLOSING_BALANCE, customer, date, amount);
    }

    /**
     * Calculates the outstanding balance for a customer.
     *
     * @param customer the customer
     * @return the balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer) {
        return calculator.getBalance(customer);
    }

    /**
     * Calculates the outstanding balance for a customer, incorporating acts
     * up to the specified date.
     *
     * @param customer the customer
     * @param date     the date
     * @return the balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer, Date date) {
        return calculator.getBalance(customer, date);
    }

    /**
     * Calculates the balance for a customer for all POSTED acts
     * between two times, inclusive.
     *
     * @param customer       the customer
     * @param from           the from time. If {@code null}, indicates that the time is unbounded
     * @param to             the to time. If {@code null}, indicates that the time is unbounded
     * @param openingBalance the opening balance
     * @return the balance
     */
    public BigDecimal getBalance(Party customer, Date from, Date to, BigDecimal openingBalance) {
        return calculator.getBalance(customer, from, to, openingBalance);
    }

    /**
     * Calculates a definitive outstanding balance for a customer.
     * <p>
     * This sums total amounts for <em>all</em> POSTED acts associated with the
     * customer, rather than just using unallocated acts, and can be used
     * to detect account balance errors.
     *
     * @param customer the customer
     * @return the definitive balance
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws CustomerAccountRuleException if an opening or closing balance
     *                                      is incorrect
     */
    public BigDecimal getDefinitiveBalance(Party customer) {
        return calculator.getDefinitiveBalance(customer);
    }

    /**
     * Calculates a new balance for a customer from the current outstanding
     * balance and a running total.
     * If the new balance is:
     * <ul>
     * <li>&lt; 0 returns 0.00 for payments, or -balance for refunds</li>
     * <li>&gt; 0 returns 0.00 for refunds</li>
     * </ul>
     *
     * @param customer the customer
     * @param total    the running total
     * @param payment  if {@code true} indicates the total is for a payment,
     *                 if {@code false} indicates it is for a refund
     * @return the new balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getBalance(Party customer, BigDecimal total, boolean payment) {
        BigDecimal balance = getBalance(customer);
        BigDecimal result;
        if (payment) {
            result = balance.subtract(total);
        } else {
            result = balance.add(total);
        }
        if (result.signum() == -1) {
            result = (payment) ? BigDecimal.ZERO : result.negate();
        } else if (result.signum() == 1 && !payment) {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    /**
     * Calculates the current overdue balance for a customer.
     * This is the sum of unallocated amounts in associated debits that have a
     * date less than the specified date less the overdue days.
     * The overdue days are specified in the customer's type node.
     * <p>
     * NOTE: this method may not be used to determine an historical overdue
     * balance. For this, use {@link #getOverdueBalance(Party, Date, Date)
     * getOverdueBalance(Party customer, Date date, Date overdueDate)}.
     *
     * @param customer the customer
     * @param date     the date
     * @return the overdue balance
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getOverdueBalance(Party customer, Date date) {
        Date overdue = getOverdueDate(customer, date);
        return calculator.getOverdueBalance(customer, overdue);
    }

    /**
     * Calculates the overdue balance for a customer as of a particular date.
     * <p>
     * This sums any POSTED debits prior to <em>overdueDate</em> that had
     * not been fully allocated by credits as of <em>date</em>.
     *
     * @param customer    the customer
     * @param date        the date
     * @param overdueDate the date when amounts became overdue
     * @return the overdue balance
     */
    public BigDecimal getOverdueBalance(Party customer, Date date,
                                        Date overdueDate) {
        return calculator.getOverdueBalance(customer, date, overdueDate);
    }

    /**
     * Determines if a customer has an overdue balance within the nominated
     * day range past their standard terms.
     *
     * @param customer the customer
     * @param date     the date
     * @param from     the from day range
     * @param to       the to day range. Use {@code &lt;= 0} to indicate
     *                 all dates
     * @return {@code true} if the customer has an overdue balance within
     * the day range past their standard terms.
     */
    public boolean hasOverdueBalance(Party customer, Date date, int from,
                                     int to) {
        Date overdue = getOverdueDate(customer, date);
        Date overdueFrom = overdue;
        Date overdueTo = null;
        if (from > 0) {
            overdueFrom = DateRules.getDate(overdueFrom, -from, DateUnits.DAYS);
        }
        if (to > 0) {
            overdueTo = DateRules.getDate(overdue, -to, DateUnits.DAYS);
        }

        // query all overdue debit acts
        ArchetypeQuery query
                = CustomerAccountQueryFactory.createUnallocatedObjectSetQuery(
                customer, DEBITS);

        NodeConstraint fromStartTime
                = new NodeConstraint("startTime", RelationalOp.LT, overdueFrom);
        if (overdueTo == null) {
            query.add(fromStartTime);
        } else {
            NodeConstraint toStartTime = new NodeConstraint("startTime",
                                                            RelationalOp.GT,
                                                            overdueTo);
            AndConstraint and = new AndConstraint();
            and.add(fromStartTime);
            and.add(toStartTime);
            query.add(and);
        }
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        return iterator.hasNext();
    }

    /**
     * Returns the overdue date relative to the specified date, for a
     * customer.
     *
     * @param customer the customer
     * @param date     the date
     * @return the overdue date
     */
    public Date getOverdueDate(Party customer, Date date) {
        IMObjectBean bean = service.getBean(customer);
        Date overdue = date;
        if (bean.hasNode("type")) {
            List<Lookup> types = bean.getValues("type", Lookup.class);
            if (!types.isEmpty()) {
                overdue = getOverdueDate(types.get(0), date);
            }
        }
        return overdue;
    }

    /**
     * Returns the overdue date relative to the specified date for a customer
     * type.
     *
     * @param type a <em>lookup.customerAccountType</em>
     * @param date the date
     * @return the overdue date
     */
    public Date getOverdueDate(Lookup type, Date date) {
        return new AccountType(type, service).getOverdueDate(date);
    }

    /**
     * Calculates the sum of all unallocated credits for a customer.
     *
     * @param customer the customer
     * @return the credit amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getCreditBalance(Party customer) {
        return calculator.getCreditBalance(customer);
    }

    /**
     * Calculates the sum of all unbilled charge acts for a customer.
     *
     * @param customer the customer
     * @return the unbilled amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getUnbilledAmount(Party customer) {
        return calculator.getUnbilledAmount(customer);
    }

    /**
     * Determines if an act has been reversed.
     *
     * @param act the act
     * @return {@code true} if the act has been reversed
     */
    public boolean isReversed(FinancialAct act) {
        IMObjectBean bean = service.getBean(act);
        return bean.hasNode("reversal") && !bean.getValues("reversal").isEmpty();
    }

    /**
     * Determines if an act is a reversal of another.
     *
     * @param act the act
     * @return {@code true} if the act is a reversal
     */
    public boolean isReversal(FinancialAct act) {
        IMObjectBean bean = service.getBean(act);
        return bean.hasNode("reverses") && !bean.getValues("reverses").isEmpty();
    }

    /**
     * Reverses an act.
     *
     * @param act       the act to reverse
     * @param startTime the start time of the reversal
     * @return the reversal of {@code act}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct reverse(FinancialAct act, Date startTime) {
        return reverse(act, startTime, null, null, false);
    }

    /**
     * Reverses an act.
     * <p>
     * If the act to be reversed is an invoice, charge items and medication acts will be unlinked from patient history.
     * Reminders and investigations will be retained.
     *
     * @param act       the act to reverse
     * @param startTime the start time of the reversal
     * @param notes     notes indicating the reason for the reversal, to set the 'notes' node if the act has one.
     *                  May be {@code null}
     * @param reference the reference. If {@code null}, the act identifier will be used
     * @param hide      if {@code true}, hide the reversal iff the act being reversed isn't already hidden
     * @return the reversal of {@code act}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct reverse(final FinancialAct act, Date startTime, String notes, String reference, boolean hide) {
        return reverse(act, startTime, notes, reference, hide, null);
    }

    /**
     * Reverses an act.
     * <p>
     * If the act to be reversed is an invoice, charge items and medication acts will be unlinked from patient history.
     * Reminders and investigations will be retained.
     *
     * @param act         the act to reverse
     * @param startTime   the start time of the reversal
     * @param notes       notes indicating the reason for the reversal, to set the 'notes' node if the act has one.
     *                    May be {@code null}
     * @param reference   the reference. If {@code null}, the act identifier will be used
     * @param hide        if {@code true}, hide the reversal iff the act being reversed isn't already hidden
     * @param tillBalance the till balance to add the reversal to. Only applies to payments and refunds, and
     *                    IN_PROGRESS till balance acts. May be {@code null}
     * @return the reversal of {@code act}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct reverse(final FinancialAct act, Date startTime, String notes, String reference, boolean hide,
                                final FinancialAct tillBalance) {
        IMObjectBean original = service.getBean(act);
        if (!original.getValues("reversal").isEmpty()) {
            throw new IllegalStateException("Act=" + act.getId() + " has already been reversed");
        }
        IMObjectCopier copier = new IMObjectCopier(new CustomerActReversalHandler(act), service);
        final List<IMObject> objects = copier.apply(act);
        FinancialAct reversal = (FinancialAct) objects.get(0);
        IMObjectBean bean = service.getBean(reversal);
        bean.setValue("reference", !StringUtils.isEmpty(reference) ? reference : act.getId());
        bean.setValue("notes", notes);
        reversal.setStatus(POSTED);
        reversal.setActivityStartTime(startTime);

        original.addTarget("reversal", reversal, "reverses");

        if (hide && !original.getBoolean("hide")) {
            bean.setValue("hide", true);
            original.setValue("hide", true);
        }

        boolean updateBalance = tillBalance != null && bean.isA(PAYMENT, REFUND);
        TillBalanceRules rules = (updateBalance) ? new TillBalanceRules(service) : null;
        if (updateBalance) {
            List<Act> changed = rules.addToBalance(reversal, tillBalance);
            objects.addAll(changed);
        }

        // This smells. The original acts needs to be saved without using the rule based archetype service, to avoid
        // triggering rules. The other acts need to be saved with rules enabled, in order to update the balance.
        // TODO
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                List<IMObject> noRules = new ArrayList<>();
                noRules.add(act);

                if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE)) {
                    removeInvoiceFromPatientHistory(act, noRules);
                }
                service.save(noRules);
                ruleService.save(objects);

                // can only update the till balance when all of the other objects have been saved
                if (rules != null) {
                    rules.updateBalance(tillBalance);
                }
            }
        });
        return reversal;
    }

    /**
     * Determines if the act has been fully allocated.
     *
     * @param act the act
     * @return {@code true} if the act has been full allocated
     */
    public boolean isAllocated(FinancialAct act) {
        return calculator.isAllocated(act);
    }

    /**
     * Sets the hidden state of a reversed/reversal act.
     *
     * @param act  the act
     * @param hide if {@code true}, hide the act in customer statements, else show it
     */
    public void setHidden(FinancialAct act, boolean hide) {
        if (canHide(act)) {
            IMObjectBean bean = service.getBean(act);
            // NOTE: must use non-rule based service to avoid balance recalculation
            if (hide != bean.getBoolean("hide")) {
                bean.setValue("hide", hide);
                // NOTE: must use non-rule based service to avoid balance recalculation
                service.save(act);
            }
        }
    }

    /**
     * Determines if an act is hidden in customer statements.
     *
     * @param act the act
     * @return {@code true} if the {@code hide} node is {@code true}
     */
    public boolean isHidden(Act act) {
        IMObjectBean bean = service.getBean(act);
        return bean.hasNode("hide") && bean.getBoolean("hide");
    }

    /**
     * Determines if an act can be hidden in customer statements.
     * <p>
     * Note that this doesn't take into account the hidden state of related acts.
     *
     * @param act the act
     * @return {@code true} if the act isn't hidden, and is reversed or a reversal
     */
    public boolean canHide(FinancialAct act) {
        return isReversed(act) || isReversal(act);
    }

    /**
     * Returns the latest {@code IN_PROGRESS} or {@code COMPLETED} invoice for a customer.
     * <p>
     * Invoices with {@code IN_PROGRESS} will be returned in preference to {@code COMPLETED} ones.
     *
     * @param customer the customer
     * @return the customer invoice, or {@code null} if none is found
     */
    public FinancialAct getInvoice(Party customer) {
        return getInvoice(customer.getObjectReference());
    }

    /**
     * Returns the latest {@code IN_PROGRESS} or {@code COMPLETED} invoice for a customer.
     * <p>
     * Invoices with {@code IN_PROGRESS} will be returned in preference to {@code COMPLETED} ones.
     *
     * @param customer the customer
     * @return the customer invoice, or {@code null} if none is found
     */
    public FinancialAct getInvoice(IMObjectReference customer) {
        return getCharge(CustomerAccountArchetypes.INVOICE, customer);
    }

    /**
     * Returns the latest {@code IN_PROGRESS} or {@code COMPLETED} credit for a customer.
     * <p>
     * Credits with {@code IN_PROGRESS} will be returned in preference to {@code COMPLETED} ones.
     *
     * @param customer the customer
     * @return the customer credit, or {@code null} if none is found
     */
    public FinancialAct getCredit(Party customer) {
        return getCredit(customer.getObjectReference());
    }

    /**
     * Returns the latest {@code IN_PROGRESS} or {@code COMPLETED} credit for a customer.
     * <p>
     * Credits with {@code IN_PROGRESS} will be returned in preference to {@code COMPLETED} ones.
     *
     * @param customer the customer
     * @return the customer credit, or {@code null} if none is found
     */
    public FinancialAct getCredit(IMObjectReference customer) {
        return getCharge(CustomerAccountArchetypes.CREDIT, customer);
    }

    /**
     * Determines if a customer has any account acts.
     *
     * @param customer the customer
     * @return {@code true} if the customer has any account acts
     */
    public boolean hasAccountActs(Party customer) {
        CustomerBalanceUpdater updater = new CustomerBalanceUpdater(service);
        return updater.hasAccountActs(customer.getObjectReference());
    }

    /**
     * Creates an opening/closing balance act.
     *
     * @param archetype the act archetype
     * @param customer  the customer
     * @param date      the date
     * @param amount    the total amount. Nay be negative
     * @return a new act
     */
    private FinancialAct createBalance(String archetype, Party customer, Date date, BigDecimal amount) {
        FinancialAct act = (FinancialAct) service.create(archetype);
        act.setActivityStartTime(date);
        if (amount.signum() == -1) {
            amount = amount.negate();
            act.setCredit(!act.isCredit());
        }
        act.setTotal(amount);
        IMObjectBean bean = service.getBean(act);
        bean.setTarget("customer", customer);
        return act;
    }

    /**
     * Creates a credit adjustment for a customer.
     *
     * @param customer the customer
     * @param total    the adjustment total
     * @param location the practice location. May be {@code null}
     * @param author   the author. May be {@code null}
     * @param practice the practice, used to determine tax rates
     * @param notes    optional notes. May be {@code null}
     * @return a new credit adjustment
     */
    public FinancialAct createCreditAdjustment(Party customer, BigDecimal total, Party location, User author,
                                               Party practice, String notes) {
        CustomerTaxRules taxRules = new CustomerTaxRules(practice, service);
        FinancialAct act = (FinancialAct) service.create(CustomerAccountArchetypes.CREDIT_ADJUST);
        act.setTotal(total);
        act.setStatus(ActStatus.POSTED); // status is derived, but derived values aren't automatically populated.
        IMObjectBean bean = ruleService.getBean(act);

        bean.setTarget("customer", customer);
        if (location != null) {
            bean.setTarget("location", location);
        }
        if (author != null) {
            bean.setTarget("author", author);
        }
        if (notes != null) {
            bean.setValue("notes", notes);
        }
        taxRules.calculateTax(act);
        return act;
    }

    /**
     * Removes charge items and medications acts linked to an invoice from the patient history.
     *
     * @param invoice the invoice
     * @param toSave  a list of objects to save
     */
    private void removeInvoiceFromPatientHistory(FinancialAct invoice, List<IMObject> toSave) {
        IMObjectBean bean = service.getBean(invoice);
        Map<IMObjectReference, Act> events = new HashMap<>();
        for (Act item : bean.getTargets("items", Act.class)) {
            IMObjectBean itemBean = service.getBean(item);
            for (ActRelationship relationship : itemBean.getValues("event", ActRelationship.class)) {
                toSave.add(item); // only one relationship to event
                removeEventRelationship(events, item, relationship);
            }
            for (Act medication : itemBean.getTargets("dispensing", Act.class)) {
                if (removeEventRelationship(events, medication)) {
                    toSave.add(medication);
                }
            }
            for (Act investigation : itemBean.getTargets("investigations", Act.class)) {
                if (removeEventRelationship(events, investigation)) {
                    toSave.add(investigation);
                }
            }
            for (Act document : itemBean.getTargets("documents", Act.class)) {
                if (removeEventRelationship(events, document)) {
                    toSave.add(document);
                }
            }
        }
        toSave.addAll(events.values());
    }

    /**
     * Removes a relationship between an act and <em>act.patientClinicalEvent</em>.
     *
     * @param events the cache of events
     * @param act    the act to remove the relationship from
     */
    private boolean removeEventRelationship(Map<IMObjectReference, Act> events, Act act) {
        boolean changed = false;
        IMObjectBean bean = service.getBean(act);
        for (ActRelationship eventRelationship : bean.getValues("event", ActRelationship.class)) {
            changed = true;
            removeEventRelationship(events, act, eventRelationship);
        }
        return changed;
    }

    /**
     * Removes a relationship between an act and <em>act.patientClinicalEvent</em>.
     *
     * @param events       the cache of events
     * @param act          the act to remove the relationship from. It must be the target of the relationship
     * @param relationship the relationship to remove
     */
    private void removeEventRelationship(Map<IMObjectReference, Act> events, Act act, ActRelationship relationship) {
        act.removeActRelationship(relationship);
        IMObjectReference ref = relationship.getSource();
        Act event = events.get(ref);
        if (event == null) {
            event = (Act) service.get(ref);
            events.put(ref, event);
        }
        if (event != null) {
            event.removeActRelationship(relationship);
        }
    }

    /**
     * Return the latest charge for a customer.
     *
     * @param shortName the charge archetype short name
     * @param customer  the customer
     * @return the invoice, or {@code null} if none can be found
     */
    private FinancialAct getCharge(String shortName, IMObjectReference customer) {
        FinancialAct result = getCharge(shortName, customer, IN_PROGRESS);
        if (result == null) {
            result = getCharge(shortName, customer, ActStatus.COMPLETED);
        }
        return result;
    }

    /**
     * Return the latest charge for a customer with the given status.
     *
     * @param shortName the charge archetype short name
     * @param customer  the customer
     * @param status    the act status
     * @return the invoice, or {@code null} if none can be found
     */
    private FinancialAct getCharge(String shortName, IMObjectReference customer, String status) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.setMaxResults(1);

        query.add(Constraints.join("customer").add(Constraints.eq("entity", customer)));
        query.add(Constraints.eq("status", status));
        query.add(Constraints.sort("startTime", false));
        IMObjectQueryIterator<FinancialAct> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
    }

}
