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
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.order;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.select.IMObjectSelectorListener;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * Query for <em>act.supplierOrder</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class OrderQuery extends DateRangeActQuery<FinancialAct> {


    /**
     * The supplier selector.
     */
    private final IMObjectSelector<Party> supplier;

    /**
     * The stock location selector.
     */
    private final IMObjectSelector<Party> stockLocation;

    /**
     * The delivery status selector.
     */
    private SelectField deliveryStatus;


    /**
     * Constructs a new <tt>OrderQuery</tt>.
     *
     * @param shortNames the act short names to query
     */
    public OrderQuery(String[] shortNames) {
        super(shortNames,
              FastLookupHelper.getLookups("act.supplierOrder", "status"),
              FinancialAct.class);

        supplier = new IMObjectSelector<Party>(
                Messages.get("supplier.order.type"),
                new String[]{"party.supplier*"});
        supplier.setListener(new IMObjectSelectorListener<Party>() {
            public void selected(Party object) {
                if (object == null) {
                    // query all suppliers
                    setParticipantConstraint(null, null, null);
                } else {
                    // limit query to the selected supplier
                    setParticipantConstraint(object, "supplier",
                                             "participation.supplier");
                }
                onQuery();
            }

            public void create() {
                // no-op
            }
        });

        stockLocation = new IMObjectSelector<Party>(
                Messages.get("supplier.order.location"),
                new String[]{"party.organisationStockLocation"});
        stockLocation.setListener(new IMObjectSelectorListener<Party>() {
            public void selected(Party object) {
                onQuery();
            }

            public void create() {
                // no-op
            }
        });
    }

    /**
     * Performs the query.
     * If constraining acts to a particular entity, the entity must be non-null
     * or a <tt>null</tt> will be returned.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set. May be <tt>null</tt>
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
    private ResultSet<FinancialAct> filterOnDeliveryStatus(
            ResultSet<FinancialAct> set,
            String deliveryStatus) {
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
        return new IMObjectListResultSet<FinancialAct>(matches,
                                                       getMaxResults());
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    @Override
    protected ResultSet<FinancialAct> createResultSet(SortConstraint[] sort) {
        List<ParticipantConstraint> list
                = new ArrayList<ParticipantConstraint>();
        ParticipantConstraint supplier = getParticipantConstraint();
        if (supplier != null) {
            list.add(supplier);
        }
        if (stockLocation.getObject() != null) {
            ParticipantConstraint location = new ParticipantConstraint(
                    "stockLocation", "participation.locationStockLocation",
                    stockLocation.getObject());
            list.add(location);
        }
        ParticipantConstraint[] participants
                = list.toArray(new ParticipantConstraint[0]);
        return new ActResultSet<FinancialAct>(getArchetypeConstraint(),
                                              participants, getFrom(), getTo(),
                                              getStatuses(), excludeStatuses(),
                                              getConstraints(), getMaxResults(),
                                              sort);
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

        addSelector(supplier, row1);
        addSelector(stockLocation, row1);
        addStatusSelector(row1);
        addDeliveryStatus(row1);
        addDateRange(row2);

        container.add(ColumnFactory.create("CellSpacing", row1, row2));
    }

    /**
     * Returns the selected delivery status.
     *
     * @return the selected delivery status, or <tt>null</tt> if no status is
     *         selected
     */
    private String getDeliveryStatus() {
        String result = null;
        int index = deliveryStatus.getSelectedIndex();
        if (index != -1) {
            LookupListModel model = (LookupListModel) deliveryStatus.getModel();
            Lookup lookup = model.getLookup(index);
            if (LookupListModel.ALL != lookup) {
                result = lookup.getCode();
            }
        }
        return result;
    }

    /**
     * Adds a selector component to a container.
     *
     * @param selector  the selector
     * @param container the container
     */
    private void addSelector(IMObjectSelector<Party> selector,
                             Component container) {
        Label label = LabelFactory.create();
        label.setText(selector.getType());
        container.add(label);
        container.add(selector.getComponent());
        getFocusGroup().add(selector.getComponent());
    }

    /**
     * Adds the delivery status select field component to a container.
     *
     * @param container the container
     */
    private void addDeliveryStatus(Component container) {
        List<Lookup> lookups = FastLookupHelper.getLookups("act.supplierOrder",
                                                           "deliveryStatus");
        LookupListModel model = new LookupListModel(lookups, true);
        Label label = LabelFactory.create();
        String displayName = DescriptorHelper.getDisplayName(
                "act.supplierOrder", "deliveryStatus");
        label.setText(displayName);
        deliveryStatus = SelectFieldFactory.create(model);
        deliveryStatus.setCellRenderer(new LookupListCellRenderer());
        getFocusGroup().add(deliveryStatus);
        container.add(label);
        container.add(deliveryStatus);
    }
}
