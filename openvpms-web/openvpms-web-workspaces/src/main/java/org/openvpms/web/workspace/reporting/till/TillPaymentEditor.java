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

package org.openvpms.web.workspace.reporting.till;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.ReadOnlyProperty;
import org.openvpms.web.component.property.Validator;

/**
 * An editor for customer payments and refunds that places them directly into an IN_PROGRESS till balance.
 * <p>
 * This:
 * <ul>
 * <li>exposes the customer for editing</li>
 * <li>makes the till read-only</li>
 * <li>makes the status POSTED and read-only</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class TillPaymentEditor extends CustomerPaymentEditor {

    /**
     * Updates the till balance with the payment/refund.
     */
    private final TillBalanceUpdater updater;

    /**
     * Constructs a {@link TillPaymentEditor}.
     *
     * @param act     the act to edit
     * @param balance the till balance
     * @param context the layout context
     */
    public TillPaymentEditor(FinancialAct act, FinancialAct balance, LayoutContext context) {
        super(act, null, context);
        setStatus(ActStatus.POSTED);
        updater = new TillBalanceUpdater(act, balance);
    }

    /**
     * Invoked when layout has completed.
     * <p>
     * This can be used to perform processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        getParticipationEditor("customer", true).addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                updateSummary();
            }
        });
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && updater.validate();
    }

    /**
     * Save any edits.
     * <p>
     * This links the adjustment to the <em>act.tillBalance</em> and forces a recalculation, if one is present.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        updater.prepare();
        super.doSave();
        updater.commit();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PaymentLayoutStrategy(getItems());
    }

    private class PaymentLayoutStrategy extends LayoutStrategy {

        /**
         * Constructs a {@link PaymentLayoutStrategy}.
         *
         * @param editor the act items editor
         */
        public PaymentLayoutStrategy(IMObjectCollectionEditor editor) {
            super(editor);
            getArchetypeNodes().simple("customer").order("customer", "till");
        }

        /**
         * Creates a component for a property.
         * <p>
         * If there is a pre-existing component, registered via {@link #addComponent}, this will be returned.
         *
         * @param property the property
         * @param parent   the parent object
         * @param context  the layout context
         * @return a component to display {@code property}
         */
        @Override
        protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
            String name = property.getName();
            if ("status".equals(name) || "till".equals(name)) {
                return super.createComponent(new ReadOnlyProperty(property), parent, context);
            }
            return super.createComponent(property, parent, context);
        }
    }
}
