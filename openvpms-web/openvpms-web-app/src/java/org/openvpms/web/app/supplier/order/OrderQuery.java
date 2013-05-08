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

package org.openvpms.web.app.supplier.order;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.supplier.SupplierActQuery;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Query for <em>act.supplierOrder</em> acts.
 *
 * @author Tim Anderson
 */
public class OrderQuery extends SupplierActQuery<FinancialAct> {

    /**
     * The delivery status selector.
     */
    private LookupField deliveryStatus;

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES = new ActStatuses("act.supplierOrder");


    /**
     * Constructs an {@code OrderQuery}.
     *
     * @param shortNames the act short names to query
     * @param context    the context
     */
    public OrderQuery(String[] shortNames, LayoutContext context) {
        super(shortNames, STATUSES, FinancialAct.class, context);
    }

    /**
     * Performs the query.
     * If constraining acts to a particular entity, the entity must be non-null
     * or a {@code null} will be returned.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set. May be {@code null}
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<FinancialAct> query(SortConstraint[] sort) {
        ResultSet<FinancialAct> result = super.query(sort);
        String deliveryStatus = getDeliveryStatus();
        if (deliveryStatus != null) {
            result = filterOnDeliveryStatus(result, deliveryStatus);
        }

        return result;
    }

    /**
     * Filters the result set on delivery status.
     *
     * @param set            the result set to filter
     * @param deliveryStatus the status to match on
     * @return the filtered result set
     */
    private ResultSet<FinancialAct> filterOnDeliveryStatus(ResultSet<FinancialAct> set, String deliveryStatus) {
        List<FinancialAct> matches = new ArrayList<FinancialAct>();
        while (set.hasNext()) {
            IPage<FinancialAct> page = set.next();
            for (FinancialAct act : page.getResults()) {
                IMObjectBean bean = new IMObjectBean(act);
                if (deliveryStatus.equals(bean.getString("deliveryStatus"))) {
                    matches.add(act);
                }
            }
        }
        return new IMObjectListResultSet<FinancialAct>(matches, getMaxResults(), set.getSortConstraints(), null);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        Row row1 = RowFactory.create("CellSpacing");
        Row row2 = RowFactory.create("CellSpacing");

        addSupplierSelector(row1);
        addStockLocationSelector(row1);
        addStatusSelector(row1);
        addDeliveryStatus(row1);
        addDateRange(row2);

        container.add(ColumnFactory.create("CellSpacing", row1, row2));
    }

    /**
     * Returns the selected delivery status.
     *
     * @return the selected delivery status, or {@code null} if no status is selected
     */
    private String getDeliveryStatus() {
        return deliveryStatus.getSelectedCode();
    }

    /**
     * Adds the delivery status select field component to a container.
     *
     * @param container the container
     */
    private void addDeliveryStatus(Component container) {
        Label label = LabelFactory.create();
        String displayName = DescriptorHelper.getDisplayName(
            "act.supplierOrder", "deliveryStatus");
        label.setText(displayName);
        NodeLookupQuery source = new NodeLookupQuery("act.supplierOrder", "deliveryStatus");
        deliveryStatus = LookupFieldFactory.create(source, true);
        getFocusGroup().add(deliveryStatus);
        container.add(label);
        container.add(deliveryStatus);
    }

    /**
     * Creates a new result set.
     *
     * @param participants the participant constraints
     * @param sort         the sort criteria
     * @return a new result set
     */
    protected ResultSet<FinancialAct> createResultSet(ParticipantConstraint[] participants, SortConstraint[] sort) {
        return new ActResultSet<FinancialAct>(getArchetypeConstraint(),
                                              participants, getFrom(), getTo(),
                                              getStatuses(), excludeStatuses(),
                                              getConstraints(), getMaxResults(),
                                              sort);
    }

}
