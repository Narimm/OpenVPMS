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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.customer;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.ResultSetAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Adapts a {@link CustomerResultSet} to one that returns {@code Party} instances.
 *
 * @author Tim Anderson
 */
public class CustomerResultSetAdapter extends ResultSetAdapter<ObjectSet, Party> {

    /**
     * Constructs a {@link CustomerResultSetAdapter}.
     *
     * @param set the result set to adapt
     */
    public CustomerResultSetAdapter(CustomerResultSet set) {
        super(set);
    }

    /**
     * Converts a page.
     *
     * @param page the page to convert
     * @return the converted page
     */
    protected IPage<Party> convert(IPage<ObjectSet> page) {
        List<Party> objects = new ArrayList<Party>();
        for (ObjectSet set : page.getResults()) {
            Party customer = (Party) set.get("customer");
            objects.add(customer);
        }
        return new Page<Party>(objects, page.getFirstResult(), page.getPageSize(), page.getTotalResults());
    }
}