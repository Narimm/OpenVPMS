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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.macro;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractIMObjectResultSet;
import org.openvpms.web.component.im.query.DefaultQueryExecutor;


/**
 * A <tt>ResultSet</tt> for <em>lookup.macro</em> lookups.
 * <p/>
 * This searches the <em>id</em>, <em>name</em> and <em>code</em> nodes for the query value
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MacroResultSet extends AbstractIMObjectResultSet<Lookup> {

    /**
     * Constructs a <tt>MacroResultSet</tt>.
     *
     * @param archetypes the archetypes to query
     * @param value      the value to query on. May be <tt>null</tt>
     * @param sort       the sort criteria. May be <tt>null</tt>
     * @param rows       the maximum no. of rows per page
     */
    public MacroResultSet(ShortNameConstraint archetypes, String value, SortConstraint[] sort, int rows) {
        super(archetypes, value, null, sort, rows, false, new DefaultQueryExecutor<Lookup>());
        setSearch(value, ID, NAME, "code");
    }

}
