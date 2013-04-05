/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.component.util.NumberFormatter;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.List;



/**
 * An editor for parent {@link FinancialAct}s.
 * <p/>
 * This is responsible for calculating the amount and tax when a child item
 * changes.
 *
 * @author Tim Anderson
 */
public class FinancialActEditor extends ActEditor {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(FinancialActEditor.class);

    /**
     * Constructs a {@code FinancialActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context. May be {@code null}
     */
    protected FinancialActEditor(FinancialAct act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        recalculateTax();
    }

    /**
     * Update the tax amounts for the act.
     */
    public void calculateTax() {
        Property taxAmount = getProperty("tax");
        if (taxAmount != null) {
            List<Act> acts = getItems().getActs();
            BigDecimal tax = ActHelper.sum((Act) getObject(), acts, "tax");
            taxAmount.setValue(tax);
        }
    }

    /**
     * Validates the object.
     * <p/>
     * This extends validation by ensuring that the total matches that of the sum of the item totals.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendents are valid otherwise {@code false}
     */
    @Override
    public boolean validate(Validator validator) {
        boolean result = super.validate(validator);
        if (result) {
            result = validateAmounts(validator);
        }
        return result;
    }

    /**
     * Validates that the amounts match that expected.
     * <p/>
     * This should only be necessary for acts that have been migrated from other systems.
     *
     * @param validator the validator
     * @return {@code true} if the amounts match
     */
    protected boolean validateAmounts(Validator validator) {
        boolean result;
        ActCalculator calc = new ActCalculator(ServiceHelper.getArchetypeService());
        FinancialAct act = (FinancialAct) getObject();
        BigDecimal total = calc.getTotal(act);

        List<Act> acts = getItems().getActs();
        // NOTE: the current act should be mapped into the collection if it has been edited

        BigDecimal sum = calc.sum(acts.iterator(), "total");
        result = total.compareTo(sum) == 0;
        if (!result) {
            // need to pre-format the amounts as the Messages uses the browser's locale which may have different
            // currency format
            String message = Messages.get("act.validation.totalMismatch", getProperty("amount").getDisplayName(),
                                          NumberFormatter.formatCurrency(total),
                                          getItems().getProperty().getDisplayName(),
                                          NumberFormatter.formatCurrency(sum));
            validator.add(this, new ValidatorError(message));
            if (log.isWarnEnabled()) {
                log.warn(message);
                User user = GlobalContext.getInstance().getUser();
                String userName = (user != null) ? user.getUsername() : null;
                log.warn("username = " + userName + ", act = " + format(act));
                for (int i = 0; i < acts.size(); ++i) {
                    log.warn("act item (" + (i + 1) + " of " + acts.size() + ") = " + format(acts.get(i)));
                }
                IMObjectEditor current = getItems().getCurrentEditor();
                if (current != null) {
                    log.warn("current act item = " + format(current.getObject()));
                }
            }
        }
        return result;
    }

    /**
     * Updates the amount and tax when an act item changes.
     */
    @Override
    protected void onItemsChanged() {
        Property amount = getProperty("amount");
        BigDecimal value = ActHelper.sum((Act) getObject(), getItems().getCurrentActs(), "total");
        amount.setValue(value);
        calculateTax();
    }

    /**
     * Recalculates all tax amounts. See OVPMS-334.
     */
    private void recalculateTax() {
        Property taxAmount = getProperty("tax");
        if (taxAmount != null) {
            ActRelationshipCollectionEditor items = getItems();
            List<Act> acts = items.getActs();
            for (Act act : acts) {
                // get the item editor. For CustomerInvoiceItemEditors, this
                // will recalculate the tax amount
                items.getEditor(act);
            }
            BigDecimal previousTax = (BigDecimal) taxAmount.getValue();
            BigDecimal tax = ActHelper.sum((Act) getObject(), acts, "tax");
            if (tax.compareTo(previousTax) != 0) {
                taxAmount.setValue(tax);
            }
        }
    }

    /**
     * Helper to format an object for debugging purposes, as the toString() method is not helpful.
     * TODO.
     *
     * @param object the object
     * @return the formatted object
     */
    private String format(IMObject object) {
        return new ReflectionToStringBuilder(object, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

}
