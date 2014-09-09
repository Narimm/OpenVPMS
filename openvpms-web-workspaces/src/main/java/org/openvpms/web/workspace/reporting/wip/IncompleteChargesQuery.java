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

package org.openvpms.web.workspace.reporting.wip;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.location.LocationSelectField;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;


/**
 * Query for incomplete charges. i.e invoices, credit and counter acts that
 * are IN_PROGRESS, COMPLETE, or ON_HOLD.
 *
 * @author Tim Anderson
 */
public class IncompleteChargesQuery extends DateRangeActQuery<Act> {

    /**
     * The act short names.
     */
    public static final String[] SHORT_NAMES = new String[]{
            CustomerAccountArchetypes.INVOICE,
            CustomerAccountArchetypes.CREDIT,
            CustomerAccountArchetypes.COUNTER};

    /**
     * The location selector.
     */
    private final LocationSelectField locationSelector;


    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT = {
            new NodeSortConstraint("customer")
    };

    /**
     * The act statuses, excluding POSTED.
     */
    private static final ActStatuses STATUSES = new ActStatuses(CustomerAccountArchetypes.INVOICE, ActStatus.POSTED);


    /**
     * Constructs an {@link IncompleteChargesQuery}.
     */
    public IncompleteChargesQuery(LayoutContext context) {
        super(null, null, null, SHORT_NAMES, STATUSES, Act.class);
        setDefaultSortConstraint(DEFAULT_SORT);
        setAuto(true);

        locationSelector = createLocationSelector(context.getContext());
    }

    /**
     * Sets the selected location.
     *
     * @param location the location. May be {@code null}
     */
    public void setLocation(Party location) {
        locationSelector.setSelectedItem(location);
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    @Override
    protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
        Party location = (Party) locationSelector.getSelectedItem();
        return new IncompleteChargesResultSet(getArchetypeConstraint(), null, location, locationSelector.getLocations(),
                                              getFrom(), getTo(), getStatuses(), excludeStatuses(), getMaxResults(),
                                              sort);
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        addLocation(container);
    }

    /**
     * Adds the location selector to a container.
     *
     * @param container the container
     */
    private void addLocation(Component container) {
        Label label = LabelFactory.create();
        label.setText(DescriptorHelper.getDisplayName(CustomerAccountArchetypes.INVOICE, "location"));
        container.add(label);
        container.add(locationSelector);
        getFocusGroup().add(locationSelector);
    }

    /**
     * Creates a field to select the location.
     *
     * @param context the context
     * @return a new selector
     */
    private LocationSelectField createLocationSelector(Context context) {
        LocationSelectField result = new LocationSelectField(context.getUser(), context.getPractice(), true);
        result.setSelectedItem(context.getLocation());
        result.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        return result;
    }

}
