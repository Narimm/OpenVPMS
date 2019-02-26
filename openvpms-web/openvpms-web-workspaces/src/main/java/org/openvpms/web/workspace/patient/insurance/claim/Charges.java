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

package org.openvpms.web.workspace.patient.insurance.claim;

import net.sf.jasperreports.engine.util.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.CachingReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.component.system.common.cache.SoftRefIMObjectCache;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tracks charges across a claim.
 *
 * @author Tim Anderson
 */
class Charges {

    /**
     * The claim congtext.
     */
    private final ClaimContext claimContext;

    /**
     * Caches invoices.
     */
    private final IMObjectCache cache;

    /**
     * Caches objects returned by the archetype service.
     */
    private final IArchetypeService service;

    /**
     * The balance calculator.
     */
    private final CustomerAccountRules rules;

    /**
     * Claim helper.
     */
    private final ClaimHelper claimHelper;

    /**
     * The charges, keyed on reference.
     */
    private final Map<Reference, Act> charges = new HashMap<>();

    /**
     * Constructs a {@link Charges}.
     *
     * @param claimContext the claim context
     */
    public Charges(ClaimContext claimContext) {
        this.claimContext = claimContext;
        IArchetypeRuleService service = ServiceHelper.getArchetypeService();
        cache = new SoftRefIMObjectCache(service);
        this.service = new CachingReadOnlyArchetypeService(cache, service);
        rules = ServiceHelper.getBean(CustomerAccountRules.class);
        claimHelper = new ClaimHelper(this.service);
    }

    /**
     * Determines if charges must be paid or not.
     *
     * @return {@code true} if charges must be paid
     */
    public boolean getChargesMustBePaid() {
        return !claimContext.isGapClaim();
    }

    /**
     * Determines if gap claims are available.
     *
     * @return {@code true} if gap claims are available
     */
    public boolean isGapClaimAvailable() {
        return claimContext.supportsGapClaims();
    }

    /**
     * Adds a charge item.
     *
     * @param item the charge item
     */
    public void add(Act item) {
        charges.put(item.getObjectReference(), item);
    }

    /**
     * Removes an invoice item.
     *
     * @param item the invoice item
     */
    public void remove(Act item) {
        charges.remove(item.getObjectReference());
    }

    /**
     * Determines if an invoice item exists.
     *
     * @param item the invoice item
     * @return {@code true} if the invoice item exists
     */
    public boolean contains(Act item) {
        return contains(item.getObjectReference());
    }

    /**
     * Determines if an invoice item exists.
     *
     * @param reference the charge item reference
     * @return {@code true} if the invoice item exists
     */
    public boolean contains(Reference reference) {
        return charges.containsKey(reference);
    }

    /**
     * Returns the references of each invoice associated with a charge.
     *
     * @return the invoice references
     */
    public Set<Reference> getInvoiceRefs() {
        Set<Reference> invoices = new HashSet<>();
        for (Act item : charges.values()) {
            IMObjectBean bean = service.getBean(item);
            Reference invoiceRef = bean.getSourceRef("invoice");
            if (invoiceRef != null) {
                invoices.add(invoiceRef);
            }
        }
        return invoices;
    }

    /**
     * Returns the references to each item in an invoice.
     *
     * @param invoice the invoice
     * @return the item references
     */
    public List<Reference> getItemRefs(FinancialAct invoice) {
        return service.getBean(invoice).getTargetRefs("items");
    }

    /**
     * Determines if an invoice can be claimed.
     * <br/>
     * An invoice can be claimed if:
     * <ul>
     * <li>is POSTED; and</li>
     * <li>hasn't been reversed; and</li>
     * <li>is unpaid, for a gap claim; or</li>
     * <li>is paid for a standard claim</li>
     * </ul>
     *
     * @param invoice the invoice
     * @return {@code true} if the invoice can be claimed
     */
    public boolean canClaimInvoice(FinancialAct invoice) {
        boolean result = false;
        if (ActStatus.POSTED.equals(invoice.getStatus())) {
            if (!isReversed(invoice)) {
                if (claimContext.isGapClaim()) {
                    result = isUnpaid(invoice);
                } else {
                    result = isPaid(invoice);
                }
            }
        }
        return result;
    }

    /**
     * Determines if an invoice has been fully paid.
     *
     * @param invoice the invoice
     * @return {@code true} if the invoice has been paid
     */
    public boolean isPaid(FinancialAct invoice) {
        return rules.isAllocated(invoice);
    }

    /**
     * Determines if an invoice has no payments.
     *
     * @param invoice the invoice
     * @return {@code true} if the invoice has not been paid
     */
    public boolean isUnpaid(FinancialAct invoice) {
        return MathRules.isZero(invoice.getAllocatedAmount());
    }

    /**
     * Determines if an invoice has been reversed.
     *
     * @param invoice the invoice
     * @return {@code true} if the invoice is reversed
     */
    public boolean isReversed(FinancialAct invoice) {
        return rules.isReversed(invoice);
    }

    /**
     * Determines if an invoice item can be claimed.
     * <p>
     * An item can be claimed if the associated invoice can be claimed, hasn't been claimed in another claim</li>
     * </ul>
     *
     * @param item the invoice item
     * @return {@code true} if the invoice item can be claimed
     */
    public boolean canClaimItem(Act item) {
        boolean result = false;
        if (!contains(item)) {
            FinancialAct invoice = getInvoice(item);
            if (invoice != null && canClaimInvoice(invoice)) {
                result = !isClaimed(item);
            }
        }
        return result;
    }

    /**
     * Returns each of the invoices referenced by the charges.
     *
     * @return the invoices
     */
    public List<FinancialAct> getInvoices() {
        List<FinancialAct> invoices = new ArrayList<>();
        for (Reference ref : getInvoiceRefs()) {
            FinancialAct invoice = (FinancialAct) service.get(ref);
            if (invoice != null) {
                invoices.add(invoice);
            }
        }
        return invoices;
    }

    /**
     * Returns an invoice item associated with a reference, if it belongs to the specified patient.
     *
     * @param item    the invoice item reference
     * @param patient the patient reference
     * @return the corresponding invoice item, or {@code null} if it belongs to a different patient
     */
    public Act getItem(Reference item, Reference patient) {
        Act result = null;
        Act act = (Act) cache.get(item);
        if (act != null) {
            IMObjectBean itemBean = service.getBean(act);
            if (ObjectUtils.equals(patient, itemBean.getTargetRef("patient"))) {
                result = act;
            }
        }
        return result;
    }

    /**
     * Determines if an invoice item has been claimed already by another insurance claim.
     *
     * @param item the charge item
     * @return {@code true} if the charge item has a relationship to an insurance claim that isn't CANCELLED or DECLINED
     */
    public boolean isClaimed(Act item) {
        return getClaim(item, null) != null;
    }

    /**
     * Determines if an invoice item has already been claimed by another insurance claim.
     *
     * @param item    the charge item
     * @param exclude the claim to exclude. May be {@code null}
     * @return the claim that the charge item has a relationship to, if it isn't CANCELLED or DECLINED
     */
    public Act getClaim(Act item, Act exclude) {
        return claimHelper.getClaim(item, exclude);
    }

    /**
     * Returns the invoice associated with an invoice item.
     *
     * @param item the invoice item
     * @return the corresponding invoice, or {@code null} if none is found
     */
    public FinancialAct getInvoice(Act item) {
        return service.getBean(item).getSource("invoice", FinancialAct.class);
    }

}
