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

package org.openvpms.web.workspace.customer.credit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.finance.credit.CreditActAllocator;
import org.openvpms.archetype.rules.finance.credit.CreditAllocation;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.claim.GapClaim;
import org.openvpms.insurance.service.GapInsuranceService;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An edit dialog for credit customer account acts.
 * <p>
 * This supports allocating credits against specific debits.
 *
 * @author Tim Anderson
 */
public class CreditActEditDialog extends ActEditDialog {

    /**
     * The credit act allocator.
     */
    private final CreditActAllocator allocator;

    /**
     * The archetype service.
     */
    private final IArchetypeRuleService service;

    /**
     * Debit acts to allocate against by default.
     */
    private final List<FinancialAct> debits;

    /**
     * Acts that have been modified by allocation, and need to be saved. May be {@code null}
     */
    private List<FinancialAct> allocation;

    /**
     * Gap claims that may have been modified by allocation. May be {@code null}
     */
    private List<GapClaimAllocation> claimAllocations;

    /**
     * Credit adjustments created when paying gap claims.
     */
    private List<FinancialAct> adjustments = new ArrayList<>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(CreditActEditDialog.class);

    /**
     * Constructs a {@link CreditActEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public CreditActEditDialog(IMObjectEditor editor, Context context) {
        this(editor, Collections.emptyList(), context);
    }

    /**
     * Constructs a {@link CreditActEditDialog}.
     *
     * @param editor  the editor
     * @param debits  debits to allocate against
     * @param context the context
     */
    public CreditActEditDialog(IMObjectEditor editor, List<FinancialAct> debits, Context context) {
        super(editor, context);
        this.debits = debits;
        service = ServiceHelper.getArchetypeService();
        allocator = ServiceHelper.getBean(CreditActAllocator.class);
    }

    /**
     * Saves the current object, if saving is enabled.
     * <p>
     * If it is, and the object is valid, then {@link #doSave(IMObjectEditor)} is called.
     * If {@link #doSave(IMObjectEditor)} fails (i.e returns {@code false}), then {@link #saveFailed()} is called.
     *
     * @return {@code true} if the object was saved
     */
    @Override
    public boolean save() {
        allocation = null;
        claimAllocations = null;
        adjustments.clear();
        boolean save = false;
        if (canSave()) {
            FinancialAct credit = (FinancialAct) getEditor().getObject();
            CreditAllocation allocation = allocator.allocate(credit, debits, false);
            if (!allocation.overrideDefaultAllocation()) {
                this.allocation = allocation.getModified();
                save = super.save();
            } else {
                AllocationDialog dialog = createAllocationDialog(allocation);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        onAllocated(credit, dialog.getDebits(), dialog.getGapClaimAllocations());
                    }
                });
                dialog.show();
            }
        }
        return save;
    }

    /**
     * Returns any credit adjustments generated through the payment of gap claims.
     *
     * @return the credit adjustments
     */
    public List<FinancialAct> getAdjustments() {
        return adjustments;
    }

    /**
     * Creates a new allocation dialog.
     *
     * @param allocation the credit allocation state
     * @return a new allocation dialog
     */
    protected AllocationDialog createAllocationDialog(CreditAllocation allocation) {
        return new AllocationDialog(allocation, getContext(), getHelpContext().subtopic("allocation"));
    }

    /**
     * Saves the current object.
     *
     * @param editor the editor
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave(IMObjectEditor editor) {
        if (allocation != null && !allocation.isEmpty()) {
            service.save(allocation);
            allocation = null;
        }
        if (claimAllocations != null) {
            for (GapClaimAllocation claimAllocation : claimAllocations) {
                GapClaimAllocation.Status status = claimAllocation.getStatus();
                if (status == GapClaimAllocation.Status.ALLOCATION_EQUAL_TO_GAP) {
                    Context context = getContext();
                    FinancialAct adjustment = claimAllocation.gapPaid(context.getPractice(), context.getLocation(),
                                                                      context.getUser());
                    if (adjustment != null) {
                        adjustments.add(adjustment);
                    }
                } else if (status == GapClaimAllocation.Status.FULL_PAYMENT
                           || status == GapClaimAllocation.Status.NO_BENEFIT_FULL_PAYMENT) {
                    claimAllocation.fullyPaid();
                }
            }
        }
        super.doSave(editor);
    }

    /**
     * Returns the insurance services.
     *
     * @return the insurance services
     */
    protected InsuranceServices getInsuranceServices() {
        return ServiceHelper.getBean(InsuranceServices.class);
    }

    /**
     * Invoked prior to payment allocation changes being saved, where the user has selected the allocation order.
     * <p/>
     * This implementation is a no-op.
     *
     * @param allocation the allocation changes
     */
    protected void preSaveAllocation(List<FinancialAct> allocation) {

    }

    /**
     * Invoked after payment allocation changes have been saved, where the user has selected the allocation order.
     * <p/>
     * This implementation notifies the insurer of claim payments, if required.
     *
     * @param claimAllocations gap claim allocations
     */
    protected void postSaveAllocation(List<GapClaimAllocation> claimAllocations) {
        if (!claimAllocations.isEmpty()) {
            notifyPayment(claimAllocations);
        }
    }

    /**
     * Invoked when a user manually selects allocation order.
     *
     * @param credit           the credit act to allocate
     * @param debits           the debit acts to allocate to
     * @param claimAllocations the gap claim allocations
     */
    private void onAllocated(FinancialAct credit, List<FinancialAct> debits,
                             List<GapClaimAllocation> claimAllocations) {
        allocation = allocator.allocate(credit, debits);
        this.claimAllocations = claimAllocations;
        preSaveAllocation(allocation);
        if (super.save()) {
            close(OK_ID);
            // close the dialog before notifying gap payments, to ensure user can't change the payment in the event
            // of a failure.
            postSaveAllocation(claimAllocations);
        }
    }

    /**
     * Notifies insurers of gap payments, if the gap has been paid, or the claim has been fully paid.
     *
     * @param allocations the claim allocations
     */
    private void notifyPayment(List<GapClaimAllocation> allocations) {
        InsuranceServices insuranceServices = getInsuranceServices();
        for (GapClaimAllocation allocation : allocations) {
            GapClaim claim = allocation.getClaim();
            if (claim.getGapStatus() == GapClaim.GapStatus.PAID
                && (claim.getStatus() == Claim.Status.ACCEPTED || claim.getStatus() == Claim.Status.SUBMITTED)) {
                Party insurer = allocation.getInsurer();
                try {
                    InsuranceService service = insuranceServices.getService(insurer);
                    if (service instanceof GapInsuranceService) {
                        ((GapInsuranceService) service).notifyPayment(claim);
                    } else {
                        ErrorDialog.show(Messages.get("patient.insurance.pay.title"),
                                         Messages.format("customer.credit.gap.notificationfailed", insurer.getName()));
                    }
                } catch (Throwable exception) {
                    log.error("Failed to notify " + insurer.getName() + " of gap payment", exception);
                    ErrorDialog.show(Messages.get("patient.insurance.pay.title"),
                                     Messages.format("customer.credit.gap.notificationfailed", insurer.getName()));
                }
            }
        }
    }

}
