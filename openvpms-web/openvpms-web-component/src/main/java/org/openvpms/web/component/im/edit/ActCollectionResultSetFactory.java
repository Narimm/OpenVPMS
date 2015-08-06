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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.act.DefaultActTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;

/**
 * An implementation of the {@link CollectionResultSetFactory} interface for acts that uses a
 * {@link DefaultActTableModel} to display results
 *
 * @author Tim Anderson
 */
public class ActCollectionResultSetFactory extends AbstractCollectionResultSetFactory {

    /**
     * The singleton instance.
     */
    public static final CollectionResultSetFactory INSTANCE = new ActCollectionResultSetFactory();

    /**
     * Default constructor.
     */
    protected ActCollectionResultSetFactory() {
    }

    /**
     * Creates a table model to display the result set.
     * <p/>
     * This implementation sets the default sort column to the first column, and sorts it descending if
     * it is a "startTime" node.
     *
     * @param property the collection property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a new table model
     */
    @Override
    @SuppressWarnings("unchecked")
    public IMTableModel<IMObject> createTableModel(CollectionPropertyEditor property, IMObject parent,
                                                   LayoutContext context) {
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        DefaultActTableModel model = new DefaultActTableModel(property.getArchetypeRange(), context);
        TableColumn column = model.getColumnModel().getColumn(0);
        if (column instanceof DescriptorTableColumn && ((DescriptorTableColumn) column).getName().equals("startTime")) {
            model.setDefaultSortAscending(false);
        } else {
            model.setDefaultSortAscending(true);
        }
        model.setDefaultSortColumn(column.getModelIndex());
        IMTableModel result = model;
        return (IMTableModel<IMObject>) result;
    }
}
