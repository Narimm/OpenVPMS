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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.PreloadedResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.PagedIMObjectTable;
import org.openvpms.web.component.util.ColumnFactory;


/**
 * Read-only viewer for a collection of {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AbstractIMObjectCollectionViewer
        implements IMObjectCollectionViewer {

    /**
     * The object that owns the collection to view.
     */
    private final IMObject _object;

    /**
     * Collection to browse.
     */
    private PagedIMObjectTable _table;

    /**
     * The collection property.
     */
    private final CollectionProperty _property;

    /**
     * The component.
     */
    private Component _component;

    /**
     * The layout context.
     */
    private final LayoutContext _context;

    /**
     * Box to display child objects in.
     */
    private GroupBox _box;

    /**
     * No. of rows to display.
     */
    private int ROWS = 15;


    /**
     * Construct a new <code>AbstractIMObjectCollectionViewer</code>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     */
    public AbstractIMObjectCollectionViewer(CollectionProperty property,
                                            IMObject parent) {
        _context = new DefaultLayoutContext();

        // filter out the uid (aka "id") field
        NodeFilter idFilter = new NamedNodeFilter("uid");
        NodeFilter filter = FilterHelper.chain(
                idFilter, _context.getDefaultNodeFilter());
        _context.setNodeFilter(filter);

        _object = parent;
        _property = property;
    }

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    public CollectionProperty getProperty() {
        return _property;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object
     */
    public IMObject getObject() {
        return _object;
    }

    /**
     * Returns the view component.
     *
     * @return the view component
     */
    public Component getComponent() {
        if (_component == null) {
            _component = doLayout();
        }
        return _component;
    }

    /**
     * Lays out the component.
     */
    protected Component doLayout() {
        Column column = ColumnFactory.create("WideCellSpacing", getTable());
        populateTable();
        return column;
    }

    /**
     * Browses the selected object.
     */
    protected void onBrowse() {
        IMObject object = _table.getTable().getSelected();
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
        if (_box == null) {
            _box = new GroupBox();
        } else {
            _box.removeAll();
            // workaround for a bug in EPNG
            _component.remove(_box);
        }
        _component.add(_box);
        IMObjectViewer viewer = new IMObjectViewer(object, _context);
        _box.setTitle(viewer.getTitle());
        _box.add(viewer.getComponent());
    }

    /**
     * Returns the objects to display.
     *
     * @return the objects to display
     */
    protected List<IMObject> getObjects() {
        Collection values = _property.getValues();
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
    protected PagedIMObjectTable getTable() {
        if (_table == null) {
            _table = createTable();
        }
        return _table;
    }

    /**
     * Creates a new table to display the collection.
     *
     * @return a new table
     */
    protected PagedIMObjectTable createTable() {
        NodeDescriptor descriptor = _property.getDescriptor();
        IMObjectTableModel model
                = IMObjectTableModelFactory.create(descriptor, _context);
        PagedIMObjectTable table = new PagedIMObjectTable(model);
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
        List<IMObject> objects = getObjects();
        ResultSet set = new PreloadedResultSet<IMObject>(objects, ROWS);
        _table.setResultSet(set);
    }

}
