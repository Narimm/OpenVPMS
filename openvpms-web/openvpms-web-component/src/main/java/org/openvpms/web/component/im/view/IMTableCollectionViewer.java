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

import echopointng.GroupBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.layout.LayoutHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GroupBoxFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.SortableTableModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Viewer for a collection of {@link IMObject}s. The collection is displayed
 * in a table. When an item is selected, a viewer containing it is displayed
 * in a box beneath the table.
 *
 * @author Tim Anderson
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
     * Constructs an {@code AbstractIMObjectCollectionViewer}.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param layout   the layout context
     */
    public IMTableCollectionViewer(CollectionProperty property,
                                   IMObject parent,
                                   LayoutContext layout) {
        context = new DefaultLayoutContext(layout);
        context.setComponentFactory(new TableComponentFactory(context));

        // filter out the id field
        NodeFilter idFilter = new NamedNodeFilter("id");
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
     * Creates a new result set.
     *
     * @return a new result set
     */
    protected abstract ResultSet<T> createResultSet();

    /**
     * Lays out the component.
     *
     * @return a new component
     */
    protected Component doLayout() {
        Column column = new IMObjectCollectionComponent();
        column.setStyleName("WideCellSpacing");
        column.add(getTable());
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
        }
        component.add(box); // add even if present due to bug in GroupBox removal code
        IMObjectViewer viewer = new IMObjectViewer(object, getObject(), context);
        box.setTitle(viewer.getTitle());
        Component child = viewer.getComponent();
        if (LayoutHelper.needsInset(child)) {
            child = ColumnFactory.create(Styles.INSET, child);
        }
        box.add(child);
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
            public void onAction(ActionEvent e) {
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
            if (sortable.getSortColumn() == -1 && sortable.getDefaultSortColumn() != -1) {
                sortable.sort(sortable.getDefaultSortColumn(), true);
            }
        }
    }

    /**
     * The root viewer component. This hooks the collection viewer into the component hierarchy.
     */
    private class IMObjectCollectionComponent extends Column implements IMObjectComponent {

        @Override
        public IMObjectComponent getSelected() {
            IMObject object = IMTableCollectionViewer.this.getSelected();
            return object != null ? new DefaultIMObjectComponent(object, box) : null;
        }

        @Override
        public boolean select(Selection selection) {
            IMObject object = selection.getObject();
            setSelected(object);
            return object != null && ObjectUtils.equals(object, IMTableCollectionViewer.this.getSelected());
        }

        @Override
        public IMObject getObject() {
            return null;
        }

        @Override
        public String getNode() {
            return IMTableCollectionViewer.this.getProperty().getName();
        }

        @Override
        public Component getComponent() {
            return box;
        }
    }

}
