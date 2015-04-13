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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMTableModel;


/**
 * Default table browser for {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class DefaultIMObjectTableBrowser<T extends IMObject> extends IMObjectTableBrowser<T> {

    /**
     * Constructs a {@code DefaultIMObjectTableBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public DefaultIMObjectTableBrowser(Query<T> query, LayoutContext context) {
        super(query, context);
    }

    /**
     * Constructs a {@code DefaultIMObjectTableBrowser} that queries
     * IMObjects using the specified query, displaying them in the table.
     *
     * @param query   the query
     * @param sort    the sort criteria. May be {@code null}
     * @param context the layout context
     */
    public DefaultIMObjectTableBrowser(Query<T> query, SortConstraint[] sort, LayoutContext context) {
        super(query, sort, context);
    }

    /**
     * Constructs a {@code DefaultIMObjectBrowser} that queries
     * IMObjects using the specified query, displaying them in the table.
     *
     * @param query   the query
     * @param model   the table model
     * @param context the layout context
     */
    public DefaultIMObjectTableBrowser(Query<T> query, IMTableModel<T> model, LayoutContext context) {
        super(query, null, model, context);
    }

    /**
     * Constructs a {@code DefaultIMObjectBrowser} that queries
     * IMObjects using the specified query, displaying them in the table.
     *
     * @param query   the query
     * @param sort    the sort criteria. May be {@code null}
     * @param model   the table model
     * @param context the layout context
     */
    public DefaultIMObjectTableBrowser(Query<T> query, SortConstraint[] sort, IMTableModel<T> model,
                                       LayoutContext context) {
        super(query, sort, model, context);
    }
}
