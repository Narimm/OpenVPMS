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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
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
import org.openvpms.web.component.property.Property;

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
        BigDecimal low = ActHelper.sum((Act) getObject(), acts, "lowTotal");
        BigDecimal high = ActHelper.sum((Act) getObject(), acts, "highTotal");
        lowTotal.setValue(low);
        highTotal.setValue(high);
    }

    /**
     * Invoked when the start time changes. Sets the value to today if
     * start time < today.
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

}
