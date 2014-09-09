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
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collections;
import java.util.List;

import static org.openvpms.archetype.rules.patient.InvestigationArchetypes.PATIENT_INVESTIGATION;


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
     * The locations available to the user.
     */
    private final List<Party> locations;

    /**
     * The location selector.
     */
    private final SelectField locationSelector;


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

        locations = getLocations(context.getContext());
        locationSelector = createLocationSelector(locations, context);
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
        return new IncompleteChargesResultSet(getArchetypeConstraint(), null, location, locations, getFrom(), getTo(),
                                              getStatuses(), excludeStatuses(), getMaxResults(), sort);
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
        label.setText(DescriptorHelper.getDisplayName(PATIENT_INVESTIGATION, "location"));
        container.add(label);
        container.add(locationSelector);
        getFocusGroup().add(locationSelector);
    }

    /**
     * Creates a field to select the location.
     *
     * @param locations the locations available to the user
     * @param context   the layout context
     * @return a new selector
     */
    private SelectField createLocationSelector(List<Party> locations, LayoutContext context) {
        IMObjectListModel model = new IMObjectListModel(locations, true, false);
        SelectField result = SelectFieldFactory.create(model);
        result.setSelectedItem(context.getContext().getLocation());
        result.setCellRenderer(IMObjectListCellRenderer.NAME);
        result.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        return result;
    }

    /**
     * Returns the locations for the current user.
     *
     * @return the locations
     */
    private List<Party> getLocations(Context context) {
        List<Party> locations = Collections.emptyList();
        User user = context.getUser();
        if (user != null) {
            UserRules rules = new UserRules();
            locations = rules.getLocations(user);
            if (locations.isEmpty()) {
                Party practice = context.getPractice();
                if (practice != null) {
                    locations = ServiceHelper.getBean(PracticeRules.class).getLocations(practice);
                }
            }
            IMObjectSorter.sort(locations, "name");
        }
        return locations;
    }

}
