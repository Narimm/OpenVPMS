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

package org.openvpms.web.component.im.edit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.ModifiableListeners;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.focus.FocusSet;
import org.openvpms.web.component.focus.FocusTree;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;
import org.openvpms.web.component.im.query.PreloadedResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.PagedIMObjectTable;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Editor for a collection of {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 * @see IMObjectEditor
 */
public class CollectionEditor implements PropertyEditor, Saveable {

    /**
     * The collection.
     */
    private final CollectionProperty _collection;

    /**
     * The object to edit.
     */
    private final IMObject _object;

    /**
     * The layout context.
     */
    private final LayoutContext _context;

    /**
     * Collection to edit.
     */
    private PagedIMObjectTable _table;

    /**
     * The component representing this.
     */
    private Component _component;

    /**
     * The archetype short name used to create a new object.
     */
    private String _shortname;

    /**
     * Tracks the modified status of the collection.
     */
    private boolean _modified;

    /**
     * Indicates if any object has been saved.
     */
    private boolean _saved;

    /**
     * The current editor.
     */
    private IMObjectEditor _editor;

    /**
     * The edit group box.
     */
    private GroupBox _editBox;

    /**
     * Listener for component change events.
     */
    private final PropertyChangeListener _componentListener;

    /**
     * The event listeners.
     */
    private final ModifiableListeners _listeners = new ModifiableListeners();

    /**
     * The button row style.
     */
    private static final String ROW_STYLE = "CellSpacing";

    /**
     * The column style.
     */
    private static final String COLUMN_STYLE = "CellSpacing";

    /**
     * The no. of rows to display.
     */
    private static final int ROWS = 15;


    /**
     * Construct a new <code>CollectionEditor</code>.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public CollectionEditor(CollectionProperty property,
                            IMObject object, LayoutContext context) {
        _collection = property;
        _object = object;
        _context = new DefaultLayoutContext(context);

        // filter out the uid (aka "id") field
        NodeFilter idFilter = new NamedNodeFilter("uid");
        NodeFilter filter = FilterHelper.chain(
                idFilter, _context.getDefaultNodeFilter());
        _context.setNodeFilter(filter);

        _componentListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                onComponentChange(event);
            }
        };
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return _collection;
    }

    /**
     * Returns the rendered collection.
     *
     * @return the rendered collection
     */
    public Component getComponent() {
        if (_component == null) {
            doLayout();
        }
        return _component;
    }

    /**
     * Returns the object being edited.
     *
     * @return the object being edited
     */
    public IMObject getObject() {
        return _object;
    }

    /**
     * Returns the collection property.
     *
     * @return the collection property
     */
    public CollectionProperty getCollection() {
        return _collection;
    }

    /**
     * Returns the collection descriptor.
     *
     * @return the collection descriptor
     */
    public NodeDescriptor getDescriptor() {
        return _collection.getDescriptor();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <code>true</code> if the object has been modified
     */
    public boolean isModified() {
        if (!_modified && _editor != null) {
            _modified = _editor.isModified();
        }
        return _modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        _modified = false;
        if (_editor != null) {
            _editor.clearModified();
        }
    }

    /**
     * Add a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        _listeners.addListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        _listeners.removeListener(listener);
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    public boolean save() {
        boolean saved;
        if (!isModified()) {
            saved = true;
        } else {
            saved = saveCurrentEdits();
            if (saved) {
                clearModified();
            }
            _saved |= saved;
        }
        return saved;
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return <code>true</code> if edits have been saved.
     */
    public boolean isSaved() {
        return _saved;
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        boolean valid;
        if (_editor != null) {
            valid = _editor.isValid();
        } else {
            valid = true;
        }
        return valid;
    }

    /**
     * Lays out the component.
     */
    protected void doLayout() {
        _component = ColumnFactory.create(COLUMN_STYLE);
        NodeDescriptor descriptor = getDescriptor();
        String[] range = getArchetypeRange();
        range = DescriptorHelper.getShortNames(range); // expand any wildcards

/*
        if (_collection.getMinCardinality() == 1
            && _collection.getMaxCardinality() == 1 && range.length == 1) {
            // Handles the special case of a collection of a single archetype.
            // This can be edited in-line.
            _shortname = range[0];
            Object[] values = _collection.getValues().toArray();
            IMObject object;
            if (values.length > 0) {
                object = (IMObject) values[0];
            } else {
                object = IMObjectCreator.create(_shortname);
                if (object != null) {
                    _collection.add(object);
                }
            }
            if (object != null) {
                IMObjectComponentFactory factory
                        = _context.getComponentFactory();
                Component editor = factory.create(object, _object, descriptor);
                _component.add(editor);
            }
        } else {
*/
        Button create = ButtonFactory.create("add", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onNew();
            }
        });

        Button cancel = ButtonFactory.create("cancel", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onCancel();
            }
        });

        Button delete = ButtonFactory.create("delete", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });

        FocusSet focus = new FocusSet("CollectionEditor");
        focus.add(create);
        focus.add(cancel);
        focus.add(delete);
        _context.getFocusTree().add(focus);

        Row row = RowFactory.create(ROW_STYLE, create, cancel, delete);

        if (range.length == 1) {
            _shortname = range[0];
        } else if (range.length > 1) {
            final ArchetypeShortNameListModel model
                    = new ArchetypeShortNameListModel(range);
            final SelectField archetypeNames = SelectFieldFactory.create(model);
            int index = archetypeNames.getSelectedIndex();
            _shortname = model.getShortName(index);

            archetypeNames.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int index = archetypeNames.getSelectedIndex();
                    if (index != -1) {
                        _shortname = model.getShortName(index);
                    }
                }
            });
            row.add(archetypeNames);
            focus.add(archetypeNames);
        }

        _component.add(row);

        _table = new PagedIMObjectTable(createTableModel(_context));
        _table.getTable().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onEdit();
            }
        });

        populateTable();

        focus.add(_table);
        _component.add(_table);
    }

    /**
     * Returns the list of objects to display in the table.
     *
     * @return the list objects to display.
     */
    protected List<IMObject> getObjects() {
        List<IMObject> objects = Collections.emptyList();
        Collection values = _collection.getValues();
        int size = values.size();
        if (size != 0) {
            objects = new ArrayList<IMObject>();
            for (Object value : values) {
                objects.add((IMObject) value);
            }
        }
        return objects;
    }

    /**
     * Returns the range of archetypes that this may create.
     *
     * @return the range of archetypes that this may create
     */
    protected String[] getArchetypeRange() {
        NodeDescriptor descriptor = getDescriptor();
        if (!StringUtils.isEmpty(descriptor.getFilter())) {
            return new String[]{descriptor.getFilter()};
        }
        return descriptor.getArchetypeRange();
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected IMObjectTableModel createTableModel(LayoutContext context) {
        return IMObjectTableModelFactory.create(getDescriptor(), context);
    }

    /**
     * Invoked when the "New" button is pressed. Creates a new instance of the
     * selected archetype, and displays it in an editor.
     */
    protected void onNew() {
        if (saveCurrentEdits() && _shortname != null) {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            try {
                IMObject object = service.create(_shortname);
                if (object != null) {
                    edit(object);
                } else {
                    String title = Messages.get("imobject.create.failed.title");
                    String message = Messages.get("imobject.create.failed",
                                                  _shortname);
                    ErrorHelper.show(title, message);
                }
            } catch (OpenVPMSException exception) {
                String message = Messages.get("imobject.create.failed",
                                              _shortname);
                ErrorHelper.show(message, exception);
            }
        }
    }

    /**
     * Deletes the selected object.
     */
    protected void onDelete() {
        IMObject object = _table.getTable().getSelected();
        if (object != null) {
            delete(object);
            _listeners.notifyListeners(this);
        }
    }

    /**
     * Cancels the current edit.
     */
    protected void onCancel() {
        if (_editor != null) {
            removeEditor();
        }
    }

    /**
     * Delete an object.
     *
     * @param object the object to delete
     */
    protected void delete(IMObject object) {
        removeFromCollection(object);
        populateTable();
        _modified = true;
        if (_editor != null && _editor.getObject() == object) {
            removeEditor();
        }
    }

    /**
     * Remove an object from the collection.
     *
     * @param object the object to remove
     */
    protected void removeFromCollection(IMObject object) {
        _collection.remove(object);
    }

    /**
     * Edits the selected object.
     */
    protected void onEdit() {
        IMObject object = _table.getTable().getSelected();
        if (object != null) {
            if (_editor != null && _editor.isModified()) {
                // need to save any edits after getting the selected object
                // as this may change the order within the table
                if (!saveCurrentEdits()) {
                    return;
                }
                // reselect it
                _table.getTable().setSelected(object);
            }
            edit(object);
        }
    }

    /**
     * Edit an object. This pops up a window containing the editor.
     *
     * @param object the object to edit
     */
    protected void edit(final IMObject object) {
        if (_editor != null) {
            FocusTree focus = _context.getFocusTree();
            focus.remove(_editor.getFocusGroup());

            _editor.removePropertyChangeListener(
                    IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                    _componentListener);
            _editBox.remove(_editor.getComponent());
        } else {
            _editBox = new GroupBox();
            _component.add(_editBox);
        }
        _editor = createEditor(object, _context);
        _editBox.add(_editor.getComponent());
        _editBox.setTitle(_editor.getTitle());
        _editor.addPropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                _componentListener);
        _editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                _listeners.notifyListeners(CollectionEditor.this);
            }
        });
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <code>object</code>
     */
    protected IMObjectEditor createEditor(IMObject object,
                                          LayoutContext context) {
        return IMObjectEditorFactory.create(object, _object, context);
    }

    /**
     * Save any edits.
     *
     * @param editor the editor managing the object to save
     * @return <code>true</code> if the save was successful
     */
    protected boolean save(IMObjectEditor editor) {
        boolean saved = false;
        IMObject object = editor.getObject();
        boolean isNew = object.isNew();
        if (_editor.save()) {
            if (isNew) {
                _collection.add(object);
            }
            saved = true;
        }
        return saved;
    }

    /**
     * Remove the editor.
     */
    private void removeEditor() {
        FocusTree tabTree = _context.getFocusTree();
        tabTree.remove(_editor.getFocusGroup());
        _editBox.remove(_editor.getComponent());
        _component.remove(_editBox);
        _editor = null;
        _editBox = null;

    }

    /**
     * Save any current edits. If there are edits to save, the table will be
     * repopulated and the edited object reselected.
     *
     * @return <code>true</code> if the save was successful; otherwise
     *         <code>false</code>
     */
    private boolean saveCurrentEdits() {
        boolean result = false;
        if (_editor != null && _editor.isModified()) {
            if (save(_editor)) {
                result = true;
                populateTable();
                IMObject object = _editor.getObject();
                _table.getTable().setSelected(object);
                _listeners.notifyListeners(this);
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Populates the table.
     */
    private void populateTable() {
        List<IMObject> objects = getObjects();
        ResultSet set = new PreloadedResultSet<IMObject>(objects, ROWS);
        _table.setResultSet(set);
    }

    /**
     * Invoked when the editor changes components.
     *
     * @param event the property change event
     */
    private void onComponentChange(PropertyChangeEvent event) {
        Component oldValue = (Component) event.getOldValue();
        Component newValue = (Component) event.getNewValue();
        _editBox.remove(oldValue);
        _editBox.add(newValue);
    }

}
