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

package org.openvpms.insurance.internal.claim;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.bean.Policies;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.Reference;
import org.openvpms.insurance.claim.Condition;
import org.openvpms.insurance.claim.Invoice;
import org.openvpms.insurance.claim.Item;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the {@link Condition} interface.
 *
 * @author Tim Anderson
 */
public class ConditionImpl implements Condition {

    /**
     * The condition.
     */
    private final ActBean condition;

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * The invoices being claimed for the condition.
     */
    private List<Invoice> invoices;

    /**
     * Constructs a {@link ConditionImpl}
     *
     * @param act     the condition act
     * @param service the archetype service
     */
    public ConditionImpl(Act act, IArchetypeRuleService service) {
        this.condition = new ActBean(act, service);
        this.service = service;
    }

    /**
     * The date when treatment for the condition was started.
     *
     * @return date when treatment for the condition was started
     */
    @Override
    public Date getTreatedFrom() {
        return condition.getAct().getActivityStartTime();
    }

    /**
     * The date when treatment for the condition was ended.
     *
     * @return date when treatment for the condition was ended
     */
    @Override
    public Date getTreatedTo() {
        return condition.getAct().getActivityEndTime();
    }

    /**
     * Returns the diagnosis.
     *
     * @return the diagnosis
     */
    @Override
    public Lookup getDiagnosis() {
        return condition.getLookup("reason");
    }

    /**
     * Returns the status of the animal as a result of this condition.
     *
     * @return the status of the animal
     */
    @Override
    public Status getStatus() {
        return Status.valueOf(condition.getStatus());
    }

    /**
     * Returns the reason for euthanasing the animal, if {@link #getStatus()} is {@code EUTHANASED}.
     *
     * @return the reason for euthanasing the animal
     */
    @Override
    public String getEuthanasiaReason() {
        return condition.getString("euthanasiaReason");
    }

    /**
     * Returns the discount amount, including tax.
     *
     * @return the discount amount
     */
    @Override
    public BigDecimal getDiscount() {
        BigDecimal discount = BigDecimal.ZERO;
        for (Invoice invoice : getInvoices()) {
            discount = discount.add(invoice.getDiscount());
        }
        return discount;
    }

    /**
     * Returns the discount tax amount.
     *
     * @return the discount tax amount
     */
    @Override
    public BigDecimal getDiscountTax() {
        BigDecimal tax = BigDecimal.ZERO;
        for (Invoice invoice : getInvoices()) {
            tax = tax.add(invoice.getDiscountTax());
        }
        return tax;
    }

    /**
     * Returns the total amount, including tax.
     *
     * @return the total amount
     */
    @Override
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Invoice invoice : getInvoices()) {
            total = total.add(invoice.getTotal());
        }
        return total;
    }

    /**
     * Returns the total tax amount.
     *
     * @return the tax amount
     */
    @Override
    public BigDecimal getTotalTax() {
        BigDecimal tax = BigDecimal.ZERO;
        for (Invoice invoice : getInvoices()) {
            tax = tax.add(invoice.getTotalTax());
        }
        return tax;
    }

    /**
     * Returns the invoices being claimed.
     *
     * @return the invoices being claimed
     */
    @Override
    public List<Invoice> getInvoices() {
        if (invoices == null) {
            invoices = collectInvoices();
        }
        return invoices;
    }

    /**
     * Collects the invoices associated with the condition.
     *
     * @return the invoices
     */
    private List<Invoice> collectInvoices() {
        List<Invoice> result = new ArrayList<>();
        List<Act> claimItems = condition.getTargets("items", Act.class, Policies.any());
        Map<Reference, Act> invoicesByRef = new HashMap<>();
        Map<Reference, List<Item>> itemsByInvoice = new HashMap<>();
        for (Act item : claimItems) {
            ActBean bean = new ActBean(item, service);
            Reference ref = bean.getSourceRef("invoice");
            Act invoice = invoicesByRef.get(ref);
            if (invoice == null) {
                invoice = (Act) service.get(ref);
                if (invoice == null) {
                    throw new IllegalStateException("Invoice item=" + item.getObjectReference()
                                                    + " has no invoice");
                }
                invoicesByRef.put(ref, invoice);
            }
            List<Item> itemsForInvoice = itemsByInvoice.get(ref);
            if (itemsForInvoice == null) {
                itemsForInvoice = new ArrayList<>();
                itemsByInvoice.put(ref, itemsForInvoice);
            }
            itemsForInvoice.add(new ItemImpl(item, service));
        }

        for (Map.Entry<Reference, Act> entry : invoicesByRef.entrySet()) {
            Act invoice = entry.getValue();
            List<Item> items = itemsByInvoice.get(entry.getKey());
            Collections.sort(items, (o1, o2) -> DateRules.compareTo(o1.getDate(), o2.getDate()));
            result.add(new InvoiceImpl(invoice, items));
        }
        Collections.sort(result, (o1, o2) -> DateRules.compareTo(o1.getDate(), o2.getDate()));
        return result;
    }
}
