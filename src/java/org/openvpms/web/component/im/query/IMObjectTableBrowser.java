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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;


/**
 * Table browser for {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public abstract class IMObjectTableBrowser<T extends IMObject>
        extends TableBrowser<T> {

    /**
     * Constructs an {@code IMObjectTableBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public IMObjectTableBrowser(Query<T> query, LayoutContext context) {
        this(query, (SortConstraint[]) null, context);
    }

    /**
     * Constructs an {@code IMObjectTableBrowser} that queries IMObjects
     * using the specified query, displaying them in the table.
     *
     * @param query the query
     * @param sort  the sort criteria. May be {@code null}
     * @param context the layout context
     */
    public IMObjectTableBrowser(Query<T> query, SortConstraint[] sort, LayoutContext context) {
        super(query, sort, null, context);
    }

    /**
     * Constructs an {@code IMObjectTableBrowser} that queries IMObjects
     * using the specified query, displaying them in the table.
     *
     * @param query the query
     * @param model the table model
     * @param context the layout context
     */
    public IMObjectTableBrowser(Query<T> query, IMTableModel<T> model, LayoutContext context) {
        super(query, null, model, context);
    }

    /**
     * Constructs an {@code IMObjectTableBrowser} that queries IMObjects
     * using the specified query, displaying them in the table.
     *
     * @param query   the query
     * @param sort    the sort criteria. May be {@code null}
     * @param model   the table model
     * @param context the layout context
     */
    public IMObjectTableBrowser(Query<T> query, SortConstraint[] sort, IMTableModel<T> model, LayoutContext context) {
        super(query, sort, model, context);
    }

    /**
     * Creates a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<T> createTableModel(LayoutContext context) {
        context = new DefaultLayoutContext(context);
        IMObjectComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        Query<T> query = getQuery();
        String[] shortNames = query.getShortNames();
        if (query instanceof AbstractArchetypeQuery) {
            String shortName = ((AbstractArchetypeQuery<T>) query).getShortName();
            if (shortName != null) {
                shortNames = new String[]{shortName};
            }
        }
        return IMObjectTableModelFactory.create(shortNames, context);
    }

}
