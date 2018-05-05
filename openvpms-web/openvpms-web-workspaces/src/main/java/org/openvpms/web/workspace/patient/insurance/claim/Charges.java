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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.CachingReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.component.system.common.cache.SoftRefIMObjectCache;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.web.system.ServiceHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks charges across a claim.
 *
 * @author Tim Anderson
 */
class Charges {

    /**
     * Caches invoices.
     */
    private final IMObjectCache cache;

    /**
     * Caches objects returned by the archetype service.
     */
    private final IArchetypeService cachingService;

    /**
     * The balance calculator.
     */
    private final CustomerAccountRules rules;

    /**
     * The charges, keyed on reference.
     */
    private Map<Reference, Act> charges = new HashMap<>();

    /**
     * Constructs a {@link Charges}.
     */
    public Charges() {
        IArchetypeRuleService service = ServiceHelper.getArchetypeService();
        cache = new SoftRefIMObjectCache(service);
        cachingService = new CachingReadOnlyArchetypeService(cache, service);
        rules = ServiceHelper.getBean(CustomerAccountRules.class);
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
            IMObjectBean bean = new IMObjectBean(item);
            Reference invoiceRef = bean.getSourceRef("invoice");
            if (invoiceRef != null) {
                invoices.add(invoiceRef);
            }
        }
        return invoices;
    }

    /**
     * Determines if an invoice can be claimed.
     * <br/>
     * An invoice can be claimed if:
     * <ul>
     * <li>is POSTED; and</li>
     * <li>hasn't been reversed; and</li>
     * <li>is paid
     * </ul>
     *
     * @param invoice the invoice
     * @return {@code true} if the invoice can be claimed
     */
    public boolean canClaimInvoice(FinancialAct invoice) {
        boolean result = false;
        if (ActStatus.POSTED.equals(invoice.getStatus())) {
            if (!isReversed(invoice) && isPaid(invoice)) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Determines if an invoice has been paid.
     *
     * @param invoice the invoice
     * @return {@code true} if the invoice has been paid
     */
    public boolean isPaid(FinancialAct invoice) {
        return rules.isAllocated(invoice);
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
            IMObjectBean itemBean = new IMObjectBean(act);
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
        boolean result = false;
        IMObjectBean chargeBean = new IMObjectBean(item, cachingService);
        for (Act claimItem : chargeBean.getSources("claims", Act.class)) {
            IMObjectBean bean = new IMObjectBean(claimItem, cachingService);
            Act claim = bean.getSource("claim", Act.class);
            if (claim != null) {
                String status = claim.getStatus();
                if (!Claim.Status.CANCELLED.isA(status) && !Claim.Status.DECLINED.isA(status)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private FinancialAct getInvoice(Act item) {
        return new IMObjectBean(item, cachingService).getSource("invoice", FinancialAct.class);
    }
}
