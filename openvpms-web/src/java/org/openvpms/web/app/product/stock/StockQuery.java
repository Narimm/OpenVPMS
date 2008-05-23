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

package org.openvpms.web.app.product.stock;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.select.IMObjectSelectorListener;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Query for <em>act.stock*</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockQuery extends DateRangeActQuery<Act> {

    /**
     * The stock location selector.
     */
    private final IMObjectSelector<Party> stockLocation;

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES
            = new ActStatuses(StockArchetypes.STOCK_TRANSFER);


    /**
     * Constructs a new <tt>StockQuery</tt>.
     *
     * @param shortNames the act short names to query
     */
    public StockQuery(String[] shortNames) {
        super(shortNames, STATUSES, Act.class);

        setParticipantConstraint(null, "stockLocation",
                                 StockArchetypes.STOCK_LOCATION_PARTICIPATION);

        stockLocation = new IMObjectSelector<Party>(
                Messages.get("product.stockLocation"),
                "party.organisationStockLocation");
        stockLocation.setListener(new IMObjectSelectorListener<Party>() {
            public void selected(Party object) {
                setEntity(object);
                onQuery();
            }

            public void create() {
                // no-op
            }
        });

        GlobalContext context = GlobalContext.getInstance();
        Party location = context.getStockLocation();
        stockLocation.setObject(location);
        setEntity(location);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Act> query(SortConstraint[] sort) {
        return createResultSet(sort);
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        Row row1 = RowFactory.create("CellSpacing");
        Row row2 = RowFactory.create("CellSpacing");

        addShortNameSelector(row1);
        row1.add(stockLocation.getComponent());
        addStatusSelector(row1);
        addDateRange(row2);

        container.add(ColumnFactory.create("CellSpacing", row1, row2));
    }
}
