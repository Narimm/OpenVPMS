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

package org.openvpms.web.app.supplier;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.select.AbstractIMObjectSelectorListener;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * Query for supplier acts. Adds filters for supplier and stock location.
 *
 * @author Tim Anderson
 */
public abstract class SupplierActQuery<T extends Act> extends DateRangeActQuery<T> {

    /**
     * The supplier selector.
     */
    private final IMObjectSelector<Party> supplier;

    /**
     * The stock location selector.
     */
    private final IMObjectSelector<Party> stockLocation;


    /**
     * Constructs a {@code SupplierActQuery}.
     *
     * @param shortNames the act short names to query
     * @param statuses   the act statuses. May be {@code null}
     * @param type       the type that this query returns
     * @param context    the layout context
     */
    public SupplierActQuery(String[] shortNames, ActStatuses statuses, Class type, LayoutContext context) {
        super(shortNames, statuses, type);

        supplier = new IMObjectSelector<Party>(Messages.get("supplier.type"), context, "party.supplier*");
        supplier.setListener(new AbstractIMObjectSelectorListener<Party>() {
            public void selected(Party object) {
                if (object == null) {
                    // query all suppliers
                    setParticipantConstraint(null, null, null);
                } else {
                    // limit query to the selected supplier
                    setParticipantConstraint(object, "supplier", "participation.supplier");
                }
                onQuery();
            }
        });

        stockLocation = new IMObjectSelector<Party>(Messages.get("product.stockLocation"), context,
                                                    "party.organisationStockLocation");
        stockLocation.setListener(new AbstractIMObjectSelectorListener<Party>() {
            public void selected(Party object) {
                onQuery();
            }
        });

        setSupplier(context.getContext().getSupplier());
        setStockLocation(context.getContext().getStockLocation());
    }

    /**
     * Sets the supplier.
     *
     * @param supplier the supplier. May be {@code null}
     */
    public void setSupplier(Party supplier) {
        this.supplier.setObject(supplier);
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier, or {@code null} if none is selected
     */
    public Party getSupplier() {
        return supplier.getObject();
    }

    /**
     * Sets the stock location.
     *
     * @param stockLocation the stock location. May be {@code null}
     */
    public void setStockLocation(Party stockLocation) {
        this.stockLocation.setObject(stockLocation);
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location, or {@code null} if none is selected
     */
    public Party getStockLocation() {
        return stockLocation.getObject();
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return a new result set
     */
    @Override
    protected ResultSet<T> createResultSet(SortConstraint[] sort) {
        List<ParticipantConstraint> list
            = new ArrayList<ParticipantConstraint>();
        ParticipantConstraint supplier = getParticipantConstraint();
        if (supplier != null) {
            list.add(supplier);
        }
        if (stockLocation.getObject() != null) {
            ParticipantConstraint location = new ParticipantConstraint(
                "stockLocation", "participation.stockLocation",
                stockLocation.getObject());
            list.add(location);
        }
        ParticipantConstraint[] participants = list.toArray(new ParticipantConstraint[list.size()]);
        return createResultSet(participants, sort);
    }

    /**
     * Creates a new result set.
     *
     * @param participants the participant constraints
     * @param sort         the sort criteria
     * @return a new result set
     */
    protected abstract ResultSet<T> createResultSet(
        ParticipantConstraint[] participants, SortConstraint[] sort);

    /**
     * Adds the supplier selector to a container.
     *
     * @param container the container
     */
    protected void addSupplierSelector(Component container) {
        addSelector(supplier, container);
    }

    /**
     * Adds the stock location selector to a container.
     *
     * @param container the container
     */
    protected void addStockLocationSelector(Component container) {
        addSelector(stockLocation, container);
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

}
