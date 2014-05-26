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

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;

/**
 * A factory for {@link ResultSet}s for display by {@link IMObjectTableCollectionEditor}.
 *
 * @author Tim Anderson
 */
public interface CollectionResultSetFactory {

    static final int DEFAULT_ROWS = IMTableCollectionEditor.ROWS;

    /**
     * Creates a new result set.
     *
     * @param property the collection property
     * @return a new result set
     */
    ResultSet<IMObject> createResultSet(CollectionPropertyEditor property);

    /**
     * Creates a table model to display the result set.
     *
     * @param property the collection property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a new table model
     */
    IMTableModel<IMObject> createTableModel(CollectionPropertyEditor property, IMObject parent, LayoutContext context);
}