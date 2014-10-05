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

package org.openvpms.web.workspace.customer.order;

import org.apache.commons.collections4.CollectionUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class CustomerOrders {

    public List<Act> getOrders(Party customer) {
        ArchetypeQuery query = new ArchetypeQuery("act.customerOrder*");
        query.add(join("customer").add(Constraints.eq("entity", customer)));
        List<Act> result = new ArrayList<Act>();
        CollectionUtils.addAll(result, new IMObjectQueryIterator<Act>(query));
        return result;
    }
}
