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

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.List;


/**
 * Adapts a {@link ResultSet} of {@link ObjectSet ObjectSet}s to a different
 * type.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ObjectSetResultSetAdapter<T>
        extends ResultSetAdapter<ObjectSet, T> {

    /**
     * The name of the object.
     */
    private final String name;

    /**
     * The type to cast to.
     */
    private final Class<T> type;


    /**
     * Creates a new <tt>ObjectSetResultSetAdapter</tt>.
     *
     * @param set  the set to adapt
     * @param name the name of the object to return
     * @param type the type to cast to
     */
    public ObjectSetResultSetAdapter(ResultSet<ObjectSet> set,
                                     String name, Class<T> type) {
        super(set);
        this.name = name;
        this.type = type;
    }

    /**
     * Converts a page.
     *
     * @param page the page to convert
     * @return the converted page
     * @throws ClassCastException an object cannot be converted to the required
     *                            type
     */
    protected IPage<T> convert(IPage<ObjectSet> page) {
        List<T> objects = new ArrayList<T>();
        for (ObjectSet set : page.getResults()) {
            objects.add(type.cast(set.get(name)));
        }
        return new Page<T>(objects, page.getFirstResult(), page.getPageSize(),
                           page.getTotalResults());
    }
}
