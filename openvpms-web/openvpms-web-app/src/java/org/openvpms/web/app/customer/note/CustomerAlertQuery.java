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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.customer.note;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.LocalSortResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
import org.openvpms.web.component.util.LabelFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.archetype.rules.customer.CustomerArchetypes.ALERT;


/**
 * Query for <em>act.customerAlert</em> acts.
 *
 * @author Tim Anderson
 */
public class CustomerAlertQuery extends DateRangeActQuery<Act> {

    /**
     * The alert types.
     */
    private final LookupField alertTypes;

    /**
     * If {@code true}, limit to alerts active now.
     */
    private final boolean activeNow;

    /**
     * The selected alert type. If {@code null}, indicates to display all alerts.
     */
    private String alertType;

    /**
     * The statuses to query.
     */
    private static final ActStatuses STATUSES = new ActStatuses(ALERT);

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT = {
        new VirtualNodeSortConstraint("alertType.priority", true, AlertPriorityTransformer.INSTANCE),
        new NodeSortConstraint("id")
    };


    /**
     * Constructs a {@code CustomerAlertQuery}.
     *
     * @param customer the customer to query alerts for
     */
    public CustomerAlertQuery(Party customer) {
        this(customer, false);
    }

    /**
     * Constructs a {@code CustomerAlertQuery}.
     *
     * @param customer  the customer to query alerts for
     * @param activeNow if {@code true}, limit to alerts active now
     */
    public CustomerAlertQuery(Party customer, boolean activeNow) {
        super(customer, "customer", "participation.customer", new String[]{ALERT}, STATUSES, Act.class);
        setDefaultSortConstraint(DEFAULT_SORT);
        LookupQuery source = new ArchetypeLookupQuery("lookup.customerAlertType");
        alertTypes = LookupFieldFactory.create(source, true);
        alertTypes.setSelected((String) null); // default to all
        alertTypes.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                onAlertTypeChanged();
            }
        });
        this.activeNow = activeNow;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set. May be {@code null}
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Act> query(SortConstraint[] sort) {
        ResultSet<Act> result = super.query(sort);
        if (alertType != null) {
            result = filterOnAlertType(result, sort);
        }
        return result;
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        container.add(LabelFactory.create("customer.alert.type"));
        container.add(alertTypes);
        getFocusGroup().add(alertTypes);
        super.doLayout(container);
        FocusHelper.setFocus(alertTypes);
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
        ResultSet<Act> set;
        // intercept rather than pass on the sort constraint on, as it may contain a VirtualNodeSortConstraint
        if (activeNow) {
            IConstraint dateRangeConstraint = QueryHelper.createDateRangeConstraint(new Date());
            set = new ActResultSet<Act>(getArchetypeConstraint(),
                                        new ParticipantConstraint[]{getParticipantConstraint()},
                                        dateRangeConstraint, getStatuses(),
                                        excludeStatuses(), getConstraints(), getMaxResults(), null);
        } else {
            set = super.createResultSet(null);
        }
        LocalSortResultSet<Act> result = new LocalSortResultSet<Act>(set);
        result.sort(sort);
        return result;
    }

    /**
     * Filters notes to include only those that have the selected alert type.
     *
     * @param set  the set to filter
     * @param sort the sort constraint. May be {@code null}
     * @return the filtered set
     */
    private ResultSet<Act> filterOnAlertType(ResultSet<Act> set, SortConstraint[] sort) {
        List<Act> matches = new ArrayList<Act>();
        while (set.hasNext()) {
            IPage<Act> page = set.next();
            for (Act act : page.getResults()) {
                IMObjectBean bean = new IMObjectBean(act);
                if (alertType.equals(bean.getValue("alertType"))) {
                    matches.add(act);
                }
            }
        }
        ResultSet<Act> result = new IMObjectListResultSet<Act>(matches, getMaxResults());
        if (sort != null) {
            result.sort(sort);
        }
        return result;
    }

    /**
     * Invoked when the alert type changes.
     */
    private void onAlertTypeChanged() {
        alertType = alertTypes.getSelectedCode();
        onQuery();
    }

}