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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.stock;

import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Selector for <em>party.organisationStockLocation</em> instances.
 * <p/>
 * This restricts locations to those visible to the current user.
 *
 * @author Tim Anderson
 */
public class StockLocationSelector extends IMObjectSelector<Party> {

    /**
     * Constructs a {@link StockLocationSelector}.
     *
     * @param context the context
     */
    public StockLocationSelector(LayoutContext context) {
        this(Messages.get("product.stockLocation"), context);
    }

    /**
     * Constructs a {@link StockLocationSelector}.
     *
     * @param type    display name for the types of objects this may select
     * @param context the context
     */
    public StockLocationSelector(String type, LayoutContext context) {
        super(type, context, StockArchetypes.STOCK_LOCATION);
        Party location = context.getContext().getStockLocation();
        if (location != null) {
            setObject(location);
        }
    }

    /**
     * Creates a query to select objects. This implementation restricts
     * stock locations to those accessible to the current user.
     *
     * @param name a name to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @Override
    protected Query<Party> createQuery(String name) {
        return new UserStockLocationQuery(getContext().getContext());
    }
}
