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

package org.openvpms.web.app.supplier;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.select.IMObjectSelector;


/**
 * Selector for <em>party.organisationStockLocation</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockLocationSelector extends IMObjectSelector<Party> {

    /**
     * The context.
     */
    private final Context context;


    /**
     * Constructs a new <tt>StockLocationSelector</tt>.
     *
     * @param type    display name for the types of objects this may select
     * @param context the context
     */
    public StockLocationSelector(String type, Context context) {
        super(type, "party.organisationStockLocation");
        this.context = context;
        Party location = context.getStockLocation();
        if (location != null) {
            setObject(location);
        }
    }

    /**
     * Creates a query to select objects. This implementation restricts
     * stock locations to those associated with the current organisation
     * location obtained via {@link Context#getLocation()}.
     *
     * @param name a name to filter on. May be <tt>null</tt>
     * @param name
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    @Override
    protected Query<Party> createQuery(String name) {
        Query<Party> query = super.createQuery(name);
        Party location = context.getLocation();
        if (location != null) {
            CollectionNodeConstraint node
                    = new CollectionNodeConstraint("locations");
            node.add(new NodeConstraint("source",
                                        location.getObjectReference()));
            query.setConstraints(node);
        }
        return query;
    }
}
