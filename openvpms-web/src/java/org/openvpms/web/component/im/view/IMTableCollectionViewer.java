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
 *
 *  $Id$
 */

package org.openvpms.web.component.im.view;

import echopointng.GroupBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.table.SortableTableModel;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GroupBoxFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Viewer for a collection of {@link IMObject}s. The collection is displayed
 * in a table. When an item is selected, a viewer containing it is displayed
 * in a box beneath the table.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class IMTableCollectionViewer<T>
        implements IMObjectCollectionViewer {

    /**
     * The object that owns the collection to view.
     */
    private final IMObject object;

    /**
     * Collection to view.
     */
    private PagedIMTable<T> table;

    /**
     * The collection property.
     */
    private final CollectionProperty property;

    /**
     * The component.
     */
    private Component component;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * Box to display child objects in.
     */
    private GroupBox box;

    /**
     * No. of rows to display.
     */
    protected static final int ROWS = 15;


    /**
     * Constructs a new <tt>AbstractIMObjectCollectionViewer</tt>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param layout   the layout context. May be <tt>null</tt>
     */
    public IMTableCollectionViewer(CollectionProperty property,
                                   IMObject parent,
                                   LayoutContext layout) {
        if (layout == null) {
            context = new DefaultLayoutContext();
        } else {
            context = new DefaultLayoutContext(layout);
        }
        context.setComponentFactory(new TableComponentFactory(context));

        // filter out the uid (aka "id") field
        NodeFilter idFilter = new NamedNodeFilter("uid");
        NodeFilter filter = FilterHelper.chain(
                idFilter, this.context.getDefaultNodeFilter());
        context.setNodeFilter(filter);

        object = parent;
        this.property = property;
    }

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    public CollectionProperty getProperty() {
        return property;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Returns the view component.
     *
     * @return the view component
     */
    public Component getComponent() {
        if (component == null) {
            component = doLayout();
        }
        return component;
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected abstract IMTableModel<T> createTableModel(LayoutContext context);

    /**
     * Selects an object in the table.
     *
     * @param object the object to select
     */
    protected abstract void setSelected(IMObject object);

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be <tt>null</tt>
     */
    protected abstract IMObject getSelected();

    /**
     * Creates a new result set.
     *
     * @return a new result set
     */
    protected abstract ResultSet<T> createResultSet();

    /**
     * Lays out the component.
     */
    protected Component doLayout() {
        Column column = ColumnFactory.create("WideCellSpacing", getTable());
        populateTable();
        return column;
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return context;
    }

    /**
     * Browses the selected object.
     */
    protected void onBrowse() {
        IMObject object = getSelected();
        if (object != null) {
            browse(object);
        }
    }

    /**
     * Browse an object.
     *
     * @param object the object to browse.
     */
    protected void browse(IMObject object) {
        if (box == null) {
            box = GroupBoxFactory.create();
            box.setInsets(new Insets(0));
        } else {
            box.removeAll();
            // workaround for a bug in EPNG
            component.remove(box);
        }
        component.add(box);
        IMObjectViewer viewer = new IMObjectViewer(object, getObject(),
                                                   context);
        box.setTitle(viewer.getTitle());
        box.add(viewer.getComponent());
    }

    /**
     * Returns the objects to display.
     *
     * @return the objects to display
     */
    protected List<IMObject> getObjects() {
        Collection values = property.getValues();
        List<IMObject> objects = new ArrayList<IMObject>();
        for (Object value : values) {
            objects.add((IMObject) value);
        }
        return objects;
    }

    /**
     * Returns the table, creating it if it doesn't exist.
     *
     * @return the table
     */
    protected PagedIMTable<T> getTable() {
        if (table == null) {
            table = createTable();
        }
        return table;
    }

    /**
     * Creates a new table to display the collection.
     *
     * @return a new table
     */
    protected PagedIMTable<T> createTable() {
        IMTableModel<T> tableModel = createTableModel(getLayoutContext());
        PagedIMTable<T> table = new PagedIMTable<T>(tableModel);
        table.getTable().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBrowse();
            }
        });
        return table;
    }

    /**
     * Populates the table.
     */
    protected void populateTable() {
        ResultSet<T> set = createResultSet();
        table.setResultSet(set);
        IMTableModel<T> model = table.getTable().getModel();
        if (model instanceof SortableTableModel) {
            // if no column is currently sorted, sort on the default (if any)
            SortableTableModel sortable = ((SortableTableModel) model);
            if (sortable.getSortColumn() == -1
                    && sortable.getDefaultSortColumn() != -1) {
                sortable.sort(sortable.getDefaultSortColumn(), true);
            }
        }
    }

}
