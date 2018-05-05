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

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.insurance.ClaimItemStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;
import java.util.List;

/**
 * Editor for <em>act.patientInsuranceClaimItem</em> acts.
 *
 * @author Tim Anderson
 */
public class ClaimItemEditor extends AbstractClaimEditor {

    /**
     * The charges associated with the claim item.
     */
    private final ChargeCollectionEditor charges;

    /**
     * Constructs a {@link ClaimItemEditor}.
     *
     * @param act         the act to edit
     * @param parent      the parent act. May be {@code null}
     * @param customer    the customer
     * @param patient     the patient
     * @param charges     the claim-wide charges
     * @param attachments the attachments
     * @param context     the layout context
     */
    public ClaimItemEditor(Act act, Act parent, Party customer, Party patient, Charges charges,
                           AttachmentCollectionEditor attachments, LayoutContext context) {
        super(act, parent, "total", context);
        this.charges = new ChargeCollectionEditor(getCollectionProperty("items"), act, customer, patient, charges,
                                                  attachments, context);
        this.charges.addModifiableListener(modifiable -> onItemsChanged());
        getEditors().add(this.charges);

        addStartEndTimeListeners();
        getProperty("status").addModifiableListener(modifiable -> onStatusChanged());
    }

    /**
     * Returns the charges being claimed.
     *
     * @return the charges
     */
    public List<Act> getCharges() {
        return charges.getActs();
    }

    /**
     * Adds a charge, if it can be claimed..
     *
     * @param charge the charge to add
     */
    public void addCharge(Act charge) {
        if (charges.canClaim(charge)) {
            charges.add(charge);
        }
    }

    /**
     * Invoked when the start time changes. Sets the value to end time if
     * start time > end time.
     */
    @Override
    protected void onStartTimeChanged() {
        Date start = getStartTime();
        if (start != null) {
            Date now = new Date();
            if (start.compareTo(now) > 0) {
                setStartTime(now);
            }
        }
        super.onStartTimeChanged();
    }

    /**
     * Invoked when the end time changes. Sets the value to start time if
     * end time < start time.
     */
    @Override
    protected void onEndTimeChanged() {
        Date end = getEndTime();
        if (end != null) {
            Date now = new Date();
            if (end.compareTo(now) > 0) {
                setEndTime(now);
            }
        }
        super.onEndTimeChanged();
    }

    /**
     * Returns the item acts to sum.
     *
     * @return the acts
     */
    @Override
    protected List<Act> getItemActs() {
        return charges.getActs();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        strategy.addComponent(new ComponentState(charges));
        return strategy;
    }

    /**
     * Validates the object.
     * <p>
     * This extends validation by ensuring that the start time is less than the end time, if non-null.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = super.doValidation(validator);
        if (valid) {
            Property reason = getProperty("reason");
            Property description = getProperty("description");
            valid = reason.getValue() != null || !StringUtils.isEmpty(description.getString());
            if (!valid) {
                String message = Messages.format("patient.insurance.item.incomplete", reason.getDisplayName(),
                                                 description.getDisplayName());
                validator.add(this, new ValidatorError(message));
            }
        }
        return valid;
    }

    /**
     * Invoked when the status changes.
     */
    private void onStatusChanged() {
        IMObjectLayoutStrategy layout = getView().getLayout();
        if (layout instanceof ClaimItemLayoutStrategy) {
            boolean euthanased = ClaimItemStatus.EUTHANASED.equals(getStatus());
            ((ClaimItemLayoutStrategy) layout).setShowEuthanasiaReason(euthanased);
        }
    }

}
