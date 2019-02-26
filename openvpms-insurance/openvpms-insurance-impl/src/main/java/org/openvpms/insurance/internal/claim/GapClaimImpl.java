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

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.credit.CreditActAllocator;
import org.openvpms.archetype.rules.finance.credit.CreditAllocation;
import org.openvpms.archetype.rules.insurance.InsuranceRules;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.insurance.claim.GapClaim;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of {@link GapClaim}.
 *
 * @author Tim Anderson
 */
public class GapClaimImpl extends ClaimImpl implements GapClaim {

    /**
     * The customer account rules.
     */
    private final CustomerAccountRules accountRules;

    /**
     * Constructs a {@link GapClaimImpl}.
     *
     * @param claim              the claim
     * @param service            the archetype service
     * @param insuranceRules     the insurance rules
     * @param partyRules         the party rules
     * @param accountRules       the customer account rules
     * @param patientRules       the patient rules
     * @param handlers           the document handlers
     * @param transactionManager the transaction manager
     */
    public GapClaimImpl(FinancialAct claim, IArchetypeRuleService service, InsuranceRules insuranceRules,
                        PartyRules partyRules, CustomerAccountRules accountRules, PatientRules patientRules,
                        DocumentHandlers handlers, PlatformTransactionManager transactionManager) {
        this(service.getBean(claim), service, insuranceRules, partyRules, accountRules, patientRules, handlers,
             transactionManager);
    }

    /**
     * Constructs a {@link GapClaimImpl}.
     *
     * @param claim              the claim
     * @param service            the archetype service
     * @param insuranceRules     the insurance rules
     * @param partyRules         the party rules
     * @param accountRules       the customer account rules
     * @param patientRules       the patient rules
     * @param handlers           the document handlers
     * @param transactionManager the transaction manager
     */
    public GapClaimImpl(IMObjectBean claim, IArchetypeRuleService service, InsuranceRules insuranceRules,
                        PartyRules partyRules, CustomerAccountRules accountRules, PatientRules patientRules,
                        DocumentHandlers handlers, PlatformTransactionManager transactionManager) {
        super(claim, service, insuranceRules, partyRules, patientRules, handlers, transactionManager);
        this.accountRules = accountRules;
    }

    /**
     * Returns the benefit amount.
     *
     * @return the benefit amount
     */
    @Override
    public BigDecimal getBenefitAmount() {
        return getClaim().getBigDecimal("benefitAmount", BigDecimal.ZERO);
    }

    /**
     * Returns the amount that the customer has paid towards the claim.
     *
     * @return the amount the customer has paid
     */
    @Override
    public BigDecimal getPaid() {
        return getClaim().getBigDecimal("paid", BigDecimal.ZERO);
    }

    /**
     * Records the invoice(s) associated with a claim as being fully paid.
     * <p/>
     * These can be paid before any benefit amount is received.
     */
    public void fullyPaid() {
        GapStatus status = getGapStatus();
        if (status == GapStatus.PAID || status == GapStatus.NOTIFIED) {
            throw new IllegalStateException("Cannot update paid status for gap claim with status=" + status);
        }
        IMObjectBean claim = getClaim();
        claim.setValue("status2", GapStatus.PAID);
        claim.setValue("paid", getTotal());
        claim.save();
    }

    /**
     * Records the gap amount as being paid.
     * <p>
     * This updates the gap status to {@link GapStatus#PAID}.
     * <p>
     * For non-zero benefit amounts, it creates a credit adjustment, which will be allocated against the claim invoices,
     * where possible.
     *
     * @param practice the practice
     * @param location the practice location
     * @param author   the author
     * @param notes    the notes
     * @return the credit adjustment, or {@code null} if none was created
     */
    public FinancialAct gapPaid(Party practice, Party location, User author, String notes) {
        FinancialAct adjustment = null;
        GapStatus status = getGapStatus();
        if (status == null || (status != GapStatus.RECEIVED)) {
            throw new IllegalStateException("Cannot update paid status for gap claim with status=" + status);
        }
        org.openvpms.component.business.domain.im.party.Party customer
                = (org.openvpms.component.business.domain.im.party.Party) getCustomer();
        BigDecimal benefitAmount = getBenefitAmount();
        BigDecimal gapAmount = getGapAmount();
        IMObjectBean claim = getClaim();
        claim.setValue("status2", GapStatus.PAID);
        claim.setValue("paid", gapAmount);
        if (!MathRules.isZero(benefitAmount)) {
            adjustment = accountRules.createCreditAdjustment(
                    customer, benefitAmount,
                    (org.openvpms.component.business.domain.im.party.Party) location,
                    (org.openvpms.component.business.domain.im.security.User) author,
                    (org.openvpms.component.business.domain.im.party.Party) practice, notes);
            IArchetypeRuleService service = getService();
            CreditActAllocator allocator = new CreditActAllocator(service, getInsuranceRules());
            List<FinancialAct> invoices = getInvoices();
            List<FinancialAct> toSave = new ArrayList<>();
            toSave.add((FinancialAct) claim.getObject());
            CreditAllocation allocation = allocator.allocate(adjustment, invoices, false);
            if (allocation.isModified()) {
                toSave.addAll(allocation.getModified());
            } else {
                // nothing to allocate against
                toSave.add(adjustment);
            }
            service.save(toSave);
        } else {
            claim.save();
        }
        return adjustment;
    }

    /**
     * Returns the gap amount. This is the difference between the claim total and the benefit amount.
     *
     * @return the gap amount
     */
    @Override
    public BigDecimal getGapAmount() {
        BigDecimal total = getTotal();
        BigDecimal benefit = getBenefitAmount();
        return (benefit.compareTo(total) <= 0) ? total.subtract(benefit) : BigDecimal.ZERO;
    }

    /**
     * Returns the notes associated with the benefit amount.
     *
     * @return the notes associated with the benefit amount. May be {@code null}
     */
    @Override
    public String getBenefitNotes() {
        return getClaim().getString("benefitNotes");
    }

    /**
     * Returns the gap claim status.
     *
     * @return the status, or {@code null} if this is not a gap claim
     */
    @Override
    public GapStatus getGapStatus() {
        GapStatus result = null;
        IMObjectBean claim = getClaim();

        String status;
        if (claim.getBoolean("gapClaim")) {
            status = claim.getString("status2");
            result = (status != null) ? GapStatus.valueOf(status) : GapStatus.PENDING;
        }
        return result;
    }

    /**
     * Updates the gap claim status.
     *
     * @param status the status
     */
    public void setGapStatus(GapStatus status) {
        IMObjectBean claim = getClaim();
        claim.setValue("status2", status.toString());
        claim.save();
    }

    /**
     * Updates the gap claim with the benefit.
     * <p>
     * This sets the benefit status to {@link GapStatus#RECEIVED}.
     *
     * @param amount the benefit amount
     * @param notes  notes associated with the benefit amount
     */
    @Override
    public void setBenefit(BigDecimal amount, String notes) {
        GapStatus status = getGapStatus();
        if (status != GapStatus.PENDING) {
            throw new IllegalStateException("Cannot set the benefit amount for gap claims with status=" + status);
        }
        if (amount.compareTo(MathRules.round(amount, 2)) != 0) {
            throw new IllegalArgumentException("Argument 'amount' must be rounded to 2 decimal places");
        }
        IMObjectBean claim = getClaim();
        claim.setValue("benefitAmount", amount);
        claim.setValue("benefitNotes", notes);
        claim.setValue("status2", GapStatus.RECEIVED.toString());
        claim.save();
    }

    /**
     * Updates the {@link GapStatus} to {@link GapStatus#NOTIFIED}.
     * <p>
     * This is only valid when the gap status is {@link GapStatus#PAID}.
     */
    @Override
    public void paymentNotified() {
        GapStatus status = getGapStatus();
        if (status != GapStatus.PAID) {
            throw new IllegalStateException("Cannot update gap status to NOTIFIED when gap status=" + status);
        }
        IMObjectBean claim = getClaim();
        claim.setValue("status2", GapStatus.NOTIFIED.toString());
        claim.save();
    }

    /**
     * Reloads the claim.
     *
     * @return the latest instance of the claim
     */
    @Override
    public GapClaimImpl reload() {
        return (GapClaimImpl) super.reload();
    }

    /**
     * Finalises the claim.
     */
    @Override
    protected void finaliseClaim() {
        super.finaliseClaim();
        IMObjectBean claim = getClaim();
        claim.setValue("status2", GapStatus.PENDING.toString());
    }

    /**
     * Creates a new instance of the claim.
     *
     * @param claim              the claim
     * @param service            the archetype service
     * @param insuranceRules     the insurance rules
     * @param partyRules         the customer rules
     * @param patientRules       the patient rules
     * @param handlers           the document handlers
     * @param transactionManager the transaction manager
     * @return a new instance
     */
    @Override
    protected ClaimImpl newInstance(Act claim, IArchetypeRuleService service, InsuranceRules insuranceRules,
                                    PartyRules partyRules, PatientRules patientRules, DocumentHandlers handlers,
                                    PlatformTransactionManager transactionManager) {
        return new GapClaimImpl(service.getBean(claim), service, insuranceRules, partyRules, accountRules,
                                patientRules, handlers, transactionManager);
    }
}
