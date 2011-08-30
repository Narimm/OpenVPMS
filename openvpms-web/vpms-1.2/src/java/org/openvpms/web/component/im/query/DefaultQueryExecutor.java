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

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.web.system.ServiceHelper;

import java.util.Arrays;


/**
 * Default implementation of the {@link QueryListener} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultQueryExecutor<T extends IMObject>
        implements QueryExecutor<T> {

    /**
     * Executes a query.
     *
     * @param query the query to execute
     * @param nodes the nodes to query. May be <tt>null</tt>, indicating that
     *              all nodes should be returned fully populated
     * @return the query results
     */
    @SuppressWarnings("unchecked")
    public IPage<T> query(ArchetypeQuery query, String[] nodes) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        if (nodes == null || nodes.length == 0) {
            return (IPage<T>) service.get(query);
        } else {
            return (IPage<T>) service.get(query, Arrays.asList(nodes));
        }
    }
}

