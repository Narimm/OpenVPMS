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

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DEBITS;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.DISPENSING_ITEM_RELATIONSHIP;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_EVENT_CHARGE_ITEM;
import static org.openvpms.archetype.rules.patient.PatientArchetypes.CLINICAL_EVENT_ITEM;


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
     * Balance calculator.
     */
    private final BalanceCalculator calculator;


    /**
     * Constructs a {@code CustomerAccountRules}.
     *
     * @param service the archetype service
     */
    public CustomerAccountRules(IArchetypeService service) {
        this.service = service;
        calculator = new BalanceCalculator(service);
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
     * @param from           the from time. If {@code null}, indicates that
     *                       the time is unbounded
     * @param to             the to time. If {@code null}, indicates that the
     *                       time is unbounded
     * @param openingBalance the opening balance
     * @return the balance
     */
    public BigDecimal getBalance(Party customer, Date from, Date to,
                                 BigDecimal openingBalance) {
        return calculator.getBalance(customer, from, to, openingBalance);
    }

    /**
     * Calculates a definitive outstanding balance for a customer.
     * <p/>
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
    public BigDecimal getBalance(Party customer, BigDecimal total,
                                 boolean payment) {
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
     * <p/>
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
     * <p/>
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
     *         the day range past their standard terms.
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
        ObjectSetQueryIterator iterator
                = new ObjectSetQueryIterator(service, query);
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
        IMObjectBean bean = new IMObjectBean(customer, service);
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
     * Reverses an act.
     *
     * @param act       the act to reverse
     * @param startTime the start time of the reversal
     * @return the reversal of {@code act}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct reverse(FinancialAct act, Date startTime) {
        return reverse(act, startTime, null);
    }

    /**
     * Reverses an act.
     * <p/>
     * If the act to be reversed is an invoice, charge items and medication acts will be unlinked from patient history.
     * Reminders and investigations will be retained.
     *
     * @param act       the act to reverse
     * @param startTime the start time of the reversal
     * @param notes     notes indicating the reason for the reversal, to set the 'notes' node if the act has one.
     *                  May be {@code null}
     * @return the reversal of {@code act}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public FinancialAct reverse(FinancialAct act, Date startTime, String notes) {
        IMObjectCopier copier = new IMObjectCopier(new CustomerActReversalHandler(act));
        List<IMObject> objects = copier.apply(act);
        FinancialAct reversal = (FinancialAct) objects.get(0);
        ActBean bean = new ActBean(reversal, service);
        if (bean.hasNode("notes")) {
            bean.setValue("notes", notes);
        }
        reversal.setStatus(FinancialActStatus.POSTED);
        reversal.setActivityStartTime(startTime);
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE)) {
            removeInvoiceFromPatientHistory(act, objects);
        }
        service.save(objects);
        return reversal;
    }

    /**
     * Returns the latest {@code IN_PROGRESS} or {@code COMPLETED} invoice for a customer.
     * <p/>
     * Invoices with {@code IN_PROGRESS} will be returned in preference to {@code COMPLETED} ones.
     *
     * @param customer the customer
     * @return the customer invoice, or {@code null} if none is found
     */
    public FinancialAct getInvoice(Party customer) {
        FinancialAct result = getInvoice(customer, ActStatus.IN_PROGRESS);
        if (result == null) {
            result = getInvoice(customer, ActStatus.COMPLETED);
        }
        return result;
    }

    /**
     * Removes charge items and medications acts linked to an invoice from the patient history.
     * <p/>
     * NOTE: this removes the charge item relationship from the event but not the item itself; this is left up to
     * the archetype service. This is to avoid triggering the
     * archetypeService.save.act.customerAccountInvoiceItem.before and
     * archetypeService.save.act.customerAccountInvoiceItem.after rules that perform demographic updates and update
     * stock.
     * <br/>
     * This can cause problems if the item is being modified elsewhere in the same transaction,
     * but isn't likely for the purposes of invoice reversal.
     *
     * @param invoice the invoice
     * @param toSave  a list of objects to save
     */
    private void removeInvoiceFromPatientHistory(FinancialAct invoice, List<IMObject> toSave) {
        ActBean bean = new ActBean(invoice, service);
        Map<IMObjectReference, Act> events = new HashMap<IMObjectReference, Act>();
        for (Act item : bean.getNodeActs("items")) {
            ActBean itemBean = new ActBean(item, service);
            for (ActRelationship relationship : itemBean.getRelationships(CLINICAL_EVENT_CHARGE_ITEM)) {
                removeEventRelationship(events, itemBean, relationship);
            }
            for (ActRelationship relationship : itemBean.getRelationships(DISPENSING_ITEM_RELATIONSHIP)) {
                Act medication = (Act) service.get(relationship.getTarget());
                if (medication != null) {
                    boolean medicationChanged = false;
                    ActBean medicationBean = new ActBean(medication, service);
                    for (ActRelationship eventRelationship : medicationBean.getRelationships(CLINICAL_EVENT_ITEM)) {
                        medicationChanged = true;
                        removeEventRelationship(events, medicationBean, eventRelationship);
                    }
                    if (medicationChanged) {
                        toSave.add(medication);
                    }
                }
            }
        }
        toSave.addAll(events.values());
    }

    /**
     * Removes a relationship between an act and <em>act.patientClinicalEvent</em>.
     *
     * @param events       the cache of events
     * @param act          the act to remove the relationship from. It must be the target of the relationship
     * @param relationship the relationship to remove
     */
    private void removeEventRelationship(Map<IMObjectReference, Act> events, ActBean act,
                                         ActRelationship relationship) {
        act.removeRelationship(relationship);
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
     * Return the latest invoice for a customer with the given status.
     *
     * @param customer the customer
     * @param status   the act status
     * @return the invoice, or {@code null} if none can be found
     */
    private FinancialAct getInvoice(Party customer, String status) {
        ArchetypeQuery query = new ArchetypeQuery(CustomerAccountArchetypes.INVOICE, false, true);
        query.setMaxResults(1);

        query.add(Constraints.join("customer").add(Constraints.eq("entity", customer.getObjectReference())));
        query.add(Constraints.eq("status", status));
        query.add(Constraints.sort("startTime", false));
        IMObjectQueryIterator<FinancialAct> iterator = new IMObjectQueryIterator<FinancialAct>(service, query);
        return iterator.hasNext() ? iterator.next() : null;
    }

}
