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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Selector for suppliers that provides query support for partial/incorrect names.
 *
 * @author Tim Anderson
 */
public class SupplierSelector extends IMObjectSelector<Party> {

    /**
     * Constructs a {@link SupplierSelector}.
     *
     * @param context the layout context
     */
    public SupplierSelector(LayoutContext context) {
        super(Messages.get("supplier.type"), context, "party.supplier*");
    }

    /**
     * Creates a query to select objects.
     *
     * @param value a value to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @Override
    protected Query<Party> createQuery(String value) {
        // uses the query handler for party.supplier* by default
        Query<Party> query = QueryFactory.create(getShortNames(), false, getContext().getContext(), Party.class);
        query.setValue(value);
        return query;
    }
}
