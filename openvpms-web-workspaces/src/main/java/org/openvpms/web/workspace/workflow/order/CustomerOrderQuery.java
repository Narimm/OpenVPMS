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

package org.openvpms.web.workspace.workflow.order;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.clinician.ClinicianSelectField;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.location.LocationSelectField;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.LocationActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.select.AbstractIMObjectSelectorListener;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.web.component.im.query.QueryHelper.addParticipantConstraint;

/**
 * Query for <em>act.customerOrder*</em> parent acts.
 *
 * @author Tim Anderson
 */
public class CustomerOrderQuery extends DateRangeActQuery<FinancialAct> {

    /**
     * The statuses to query.
     */
    private static final ActStatuses statuses = new ActStatuses(OrderArchetypes.PHARMACY_ORDER);

    /**
     * The location selector.
     */
    private final LocationSelectField locationSelector;

    /**
     * The customer selector.
     */
    private final IMObjectSelector<Party> customerSelector;

    /**
     * The clinician selector.
     */
    private final ClinicianSelectField clinicianSelector;


    /**
     * Constructs a {@link CustomerOrderQuery}.
     *
     * @param shortNames the act short names to query
     * @param context    the layout context
     */
    public CustomerOrderQuery(String[] shortNames, LayoutContext context) {
        super(shortNames, statuses, Act.class);

        locationSelector = createLocationSelector(context.getContext());
        customerSelector = createCustomerSelector(context);
        clinicianSelector = createClinicianSelector();
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<FinancialAct> createResultSet(SortConstraint[] sort) {
        Party location = (Party) locationSelector.getSelectedItem();

        List<ParticipantConstraint> list = new ArrayList<ParticipantConstraint>();
        addParticipantConstraint(list, "customer", CustomerArchetypes.CUSTOMER_PARTICIPATION,
                                 customerSelector.getObject());
        addParticipantConstraint(list, "clinician", UserArchetypes.CLINICIAN_PARTICIPATION,
                                 (Entity) clinicianSelector.getSelectedItem());
        ParticipantConstraint[] participants = list.toArray(new ParticipantConstraint[list.size()]);

        return new LocationActResultSet<FinancialAct>(getArchetypeConstraint(), participants, location,
                                                      locationSelector.getLocations(), getFrom(), getTo(),
                                                      getStatuses(), excludeStatuses(), getMaxResults(), sort);
    }

    /**
     * Creates a container component to lay out the query component in.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(6);
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
        addCustomer(container);
        addClinician(container);
    }

    /**
     * Adds a date range to the container.
     *
     * @param container the container
     */
    @Override
    protected void addDateRange(Component container) {
        super.addDateRange(container);
        GridLayoutData layoutData = new GridLayoutData();
        layoutData.setColumnSpan(2);
        getDateRange().getComponent().setLayoutData(layoutData);
    }

    /**
     * Adds the location selector to a container.
     *
     * @param container the container
     */
    private void addLocation(Component container) {
        Label label = LabelFactory.create();
        label.setText(DescriptorHelper.getDisplayName(OrderArchetypes.PHARMACY_ORDER, "location"));
        container.add(label);
        container.add(locationSelector);
        getFocusGroup().add(locationSelector);
    }

    /**
     * Adds the customer selector to a container.
     *
     * @param container the container
     */
    private void addCustomer(Component container) {
        Label label = LabelFactory.create();
        label.setText(customerSelector.getType());
        container.add(label);
        Component component = customerSelector.getComponent();
        container.add(component);
        customerSelector.getSelect().setFocusTraversalParticipant(false);
        getFocusGroup().add(customerSelector.getTextField());
    }

    /**
     * Adds the clinician selector to a container.
     *
     * @param container the container
     */
    private void addClinician(Component container) {
        Label label = LabelFactory.create();
        label.setText(Messages.get("label.clinician"));
        container.add(label);
        container.add(clinicianSelector);
        getFocusGroup().add(clinicianSelector);
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

    /**
     * Creates a field to select the customer.
     *
     * @param context the layout context
     * @return a new selector
     */
    private IMObjectSelector<Party> createCustomerSelector(LayoutContext context) {
        IMObjectSelector<Party> selector = new IMObjectSelector<Party>(
                DescriptorHelper.getDisplayName(CustomerArchetypes.PERSON), context, CustomerArchetypes.PERSON);
        AbstractIMObjectSelectorListener<Party> listener = new AbstractIMObjectSelectorListener<Party>() {
            @Override
            public void selected(Party object) {
                onQuery();
            }
        };
        selector.setListener(listener);
        return selector;
    }

    /**
     * Creates a field to select the clinician.
     *
     * @return a new selector
     */
    private ClinicianSelectField createClinicianSelector() {
        ClinicianSelectField clinicianSelector = new ClinicianSelectField();
        clinicianSelector.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        return clinicianSelector;
    }

}
