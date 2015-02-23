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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.LookupFilter;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * An editor for {@link Act}s which have an archetype of <em>act.customerEstimation</em>.
 *
 * @author Tim Anderson
 */
public class EstimateEditor extends ActEditor {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(EstimateEditor.class);

    /**
     * Constructs an {@link EstimateEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public EstimateEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, EstimateArchetypes.ESTIMATE)) {
            throw new IllegalArgumentException("Invalid act type:" + act.getArchetypeId().getShortName());
        }
        addStartEndTimeListeners();
        initParticipant("customer", context.getContext().getCustomer());
    }

    /**
     * Returns the items collection editor.
     *
     * @return the items collection editor. May be {@code null}
     */
    @Override
    public ActRelationshipCollectionEditor getItems() {
        return super.getItems();
    }

    /**
     * Returns the customer notes collection editor.
     *
     * @return the customer notes collection editor. May be {@code null}
     */
    public ActRelationshipCollectionEditor getCustomerNotes() {
        return (ActRelationshipCollectionEditor) getEditors().getEditor("customerNotes");
    }

    /**
     * Returns the documents editor.
     *
     * @return the documents editor. May be {@code null}
     */
    public ActRelationshipCollectionEditor getDocuments() {
        return (ActRelationshipCollectionEditor) getEditors().getEditor("documents");
    }

    /**
     * Creates a collection editor for the items collection.
     *
     * @param act   the act
     * @param items the items collection
     * @return a new collection editor
     */
    @Override
    protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
        return new EstimateActRelationshipCollectionEditor(items, act, getLayoutContext());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategyFactory layoutStrategy = getLayoutContext().getLayoutStrategyFactory();
        IMObjectLayoutStrategy strategy = layoutStrategy.create(getObject(), getParent());

        // exclude the INVOICED status from the status dropdown
        Property status = getProperty("status");
        LookupQuery query = new NodeLookupQuery(getObject(), status);
        query = new LookupFilter(query, false, EstimateActStatus.INVOICED);
        LookupField field = LookupFieldFactory.create(status, query);
        strategy.addComponent(new ComponentState(field, status));

        strategy.addComponent(new ComponentState(getItems()));
        return strategy;
    }

    /**
     * Update totals when an act item changes.
     */
    protected void onItemsChanged() {
        Property highTotal = getProperty("highTotal");
        Property lowTotal = getProperty("lowTotal");

        List<Act> acts = getItems().getCurrentActs();
        BigDecimal low = calculateTotal(acts, "lowTotal");
        BigDecimal high = calculateTotal(acts, "highTotal");
        lowTotal.setValue(low);
        highTotal.setValue(high);
    }

    /**
     * Invoked when the start time changes. Sets the value to today if start time < today.
     */
    @Override
    protected void onStartTimeChanged() {
        Date start = getStartTime();
        if (start != null) {
            Date now = new Date();
            if (DateRules.compareDates(start, now) < 0) {
                // ensure start date isn't before the current date
                setStartTime(now, true);
            } else {
                Date end = getEndTime();
                if (end != null && end.compareTo(start) < 0) {
                    setEndTime(start, true);
                }
            }
        }
    }

    /**
     * Validates the object.
     * <p/>
     * This extends validation by ensuring that the total matches that of the sum of the item totals.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validateAmounts(validator);
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
        // NOTE: the current act should be mapped into the collection if it has been edited
        List<Act> acts = getItems().getActs();

        return validateTotal(validator, acts, "lowTotal") && validateTotal(validator, acts, "highTotal");
    }

    /**
     * Validates the total of a node for a collection of acts.
     *
     * @param validator the validator
     * @param acts      the acts to validate
     * @param node      the node to sum
     * @return {@code true} if the total is valid
     */
    private boolean validateTotal(Validator validator, List<Act> acts, String node) {
        boolean result;
        BigDecimal sum = calculateTotal(acts, node);
        Property property = getProperty(node);
        BigDecimal total = property.getBigDecimal(BigDecimal.ZERO);

        result = total.compareTo(sum) == 0;
        if (!result) {
            // need to pre-format the amounts as the Messages uses the browser's locale which may have different
            // currency format
            String message = Messages.format("act.validation.totalMismatch", property.getDisplayName(),
                                             NumberFormatter.formatCurrency(total),
                                             getItems().getProperty().getDisplayName(),
                                             NumberFormatter.formatCurrency(sum));
            validator.add(this, new ValidatorError(message));
            if (log.isWarnEnabled()) {
                log.warn(message);
                User user = getLayoutContext().getContext().getUser();
                String userName = (user != null) ? user.getUsername() : null;
                log.warn("username = " + userName + ", act = " + getObject());
                for (int i = 0; i < acts.size(); ++i) {
                    log.warn("act item (" + (i + 1) + " of " + acts.size() + ") = " + acts.get(i));
                }
                IMObjectEditor current = getItems().getCurrentEditor();
                if (current != null) {
                    log.warn("current act item = " + current.getObject());
                }
            }
        }
        return result;
    }

    /**
     * Calculates the total of node in a collection of acts.
     *
     * @param acts the acts
     * @param node the node to sum
     * @return the total
     */
    private BigDecimal calculateTotal(List<Act> acts, String node) {
        return ActHelper.sum((Act) getObject(), acts, node);
    }

}
