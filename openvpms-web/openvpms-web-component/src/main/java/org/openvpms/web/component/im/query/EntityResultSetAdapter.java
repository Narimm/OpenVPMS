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
package org.openvpms.web.component.im.query;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts an {@link EntityObjectSetResultSet} to one that returns {@code Entity} instances.
 *
 * @author Tim Anderson
 */
public class EntityResultSetAdapter<T extends Entity> extends ResultSetAdapter<ObjectSet, T> {

    /**
     * The context.
     */
    private final Context context;

    /**
     * Constructs an {@code EntityResultSetAdapter}.
     *
     * @param set     the result set to adapt
     * @param context the context
     */
    public EntityResultSetAdapter(EntityObjectSetResultSet set, Context context) {
        super(set);
        this.context = context;
    }

    /**
     * Converts a page.
     *
     * @param page the page to convert
     * @return the converted page
     */
    @SuppressWarnings("unchecked")
    protected IPage<T> convert(IPage<ObjectSet> page) {
        List<T> objects = new ArrayList<T>();
        for (ObjectSet set : page.getResults()) {
            IMObjectReference ref = set.getReference("entity.reference");
            T entity = (T) IMObjectHelper.getObject(ref, context);
            objects.add(entity);
        }
        return new Page<T>(objects, page.getFirstResult(), page.getPageSize(), page.getTotalResults());
    }
}
