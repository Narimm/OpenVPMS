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
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * A query for <em>party.organisationStockLocation</em> objects that limits them to those
 * available to the current user.
 *
 * @author Tim Anderson
 */
public class UserStockLocationQuery extends EntityQuery<Party> {

    /**
     * Constructs a {@link UserStockLocationQuery}.
     *
     * @param context the context
     */
    public UserStockLocationQuery(Context context) {
        super(new String[]{StockArchetypes.STOCK_LOCATION}, context);
        setAuto(true);
        User user = context.getUser();
        if (user != null) {
            IMObjectBean bean = ServiceHelper.getArchetypeService().getBean(user);
            List<Reference> locations = bean.getTargetRefs("locations");
            if (!locations.isEmpty()) {
                Object[] ids = QueryHelper.getIds(locations);
                setConstraints(Constraints.join("locations").add(Constraints.in("source", ids)));
            }
        }
    }

}
