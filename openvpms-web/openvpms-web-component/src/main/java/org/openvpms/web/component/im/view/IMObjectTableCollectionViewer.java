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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.view;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Viewer for a collection of {@link IMObject}s. The collection is displayed
 * in a table. When an item is selected, a viewer containing it is displayed
 * in a box beneath the table.
 * <p/>
 * This implementation renders {@link IMObject} instances, and creates the
 * table model using {@link IMObjectTableModelFactory}.
 *
 * @author Tim Anderson
 */
public abstract class IMObjectTableCollectionViewer
        extends IMTableCollectionViewer<IMObject> {

    /**
     * Constructs a new <tt>IMObjectTableCollectionViewer</tt>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param layout   the layout context. May be <tt>null</tt>
     */
    public IMObjectTableCollectionViewer(CollectionProperty property,
                                         IMObject parent,
                                         LayoutContext layout) {
        super(property, parent, layout);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be {@code null}
     */
    public IMObject getSelected() {
        return getTable().getTable().getSelected();
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected IMTableModel<IMObject> createTableModel(LayoutContext context) {
        return IMObjectTableModelFactory.create(getProperty().getArchetypeRange(), getObject(), context);
    }

    /**
     * Selects an object in the table.
     *
     * @param object the object to select
     */
    protected void setSelected(IMObject object) {
        getTable().getTable().setSelected(object);
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    protected ResultSet<IMObject> createResultSet() {
        return new IMObjectListResultSet<IMObject>(getObjects(), ROWS);
    }
}
