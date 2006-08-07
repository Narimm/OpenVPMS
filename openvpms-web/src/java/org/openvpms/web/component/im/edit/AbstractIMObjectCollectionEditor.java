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

import echopointng.GroupBox;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.button.ShortcutHelper;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.ModifiableListeners;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.Validator;
import org.openvpms.web.component.focus.FocusSet;
import org.openvpms.web.component.focus.FocusTree;
import org.openvpms.web.component.im.create.IMObjectCreator;
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
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;


/**
 * Editor for a collection of {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 * @see IMObjectEditor
 */
public abstract class AbstractIMObjectCollectionEditor
        implements IMObjectCollectionEditor {

    /**
     * The collection.
     */
    private final CollectionPropertyEditor _collection;

    /**
     * The parent object.
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
     * Event broadcaster.
     */
    private final ModifiableListener _broadcaster;

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
     * Construct a new <code>AbstractIMObjectCollectionEditor</code>.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected AbstractIMObjectCollectionEditor(CollectionPropertyEditor editor,
                                               IMObject object,
                                               LayoutContext context) {
        _collection = editor;
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

        _broadcaster = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                _listeners.notifyListeners(modifiable);
            }
        };
    }

    /**
     * Construct a new <code>AbstractIMObjectCollectionEditor</code>.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public AbstractIMObjectCollectionEditor(CollectionProperty property,
                                            IMObject object,
                                            LayoutContext context) {
        this(new DefaultCollectionPropertyEditor(property), object, context);
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return _collection.getProperty();
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
        return _collection.getProperty();
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <code>true</code> if the object has been modified
     */
    public boolean isModified() {
        boolean modified = _collection.isModified();
        if (!modified && _editor != null) {
            modified = _editor.isModified();
        }
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        _collection.clearModified();
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
        }
        return saved;
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return <code>true</code> if edits have been saved.
     */
    public boolean isSaved() {
        return _collection.isSaved();
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        Validator validator = new Validator();
        return validator.validate(this);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <code>true</code> if the object and its descendents are valid
     *         otherwise <code>false</code>
     */
    public boolean validate(Validator validator) {
        boolean valid = false;
        if (addCurrentEdits(validator)) {
            valid = _collection.validate(validator);
        }
        return valid;
    }


    /**
     * Lays out the component.
     */
    protected void doLayout() {
        _component = ColumnFactory.create(COLUMN_STYLE);
        FocusSet focus = new FocusSet("CollectionEditor");
        _context.getFocusTree().add(focus);
        Row row = createControls(focus);
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
     * Creates the row of controls.
     *
     * @return the row of controls
     */
    protected Row createControls(FocusSet focus) {
        String[] range = _collection.getArchetypeRange();
        range = DescriptorHelper.getShortNames(range,
                                               false); // expand any wildcards

        Button create = ButtonFactory.create(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onNew();
            }
        });

        Button delete = ButtonFactory.create(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });

        // remove any shortcuts from the text, as multiple collections may
        // be displayed on the one form
        create.setText(ShortcutHelper.getLocalisedText("button.add"));
        delete.setText(ShortcutHelper.getLocalisedText("button.delete"));

        focus.add(create);
        focus.add(delete);
        Row row = RowFactory.create(ROW_STYLE, create, delete);

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
        return row;
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    protected CollectionPropertyEditor getCollectionPropertyEditor() {
        return _collection;
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected IMObjectTableModel createTableModel(LayoutContext context) {
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        return IMObjectTableModelFactory.create(_collection.getArchetypeRange(),
                                                context);
    }

    /**
     * Invoked when the "New" button is pressed. Creates a new instance of the
     * selected archetype, and displays it in an editor.
     */
    protected void onNew() {
        if (addCurrentEdits(new Validator()) && _shortname != null) {
            int maxSize = _collection.getMaxCardinality();
            if (maxSize == -1 || _collection.getObjects().size() < maxSize) {
                IMObject object = IMObjectCreator.create(_shortname);
                if (object != null) {
                    edit(object);
                }
            }
        }
    }

    /**
     * Deletes the selected object.
     */
    protected void onDelete() {
        IMObject object;
        if (_editor != null) {
            object = _editor.getObject();
            removeEditor();
        } else {
            object = _table.getTable().getSelected();
        }
        if (object != null) {
            delete(object);
            _listeners.notifyListeners(this);
        }
    }

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
        _collection.remove(object);
        populateTable();
        if (_editor != null && _editor.getObject() == object) {
            removeEditor();
        }
    }

    /**
     * Edits the selected object.
     */
    protected void onEdit() {
        IMObject object = _table.getTable().getSelected();
        if (object != null) {
            if (addCurrentEdits(new Validator())) {
                // need to add any edits after getting the selected object
                // as this may change the order within the table
                _table.getTable().setSelected(object);
                edit(object);
            }
        }
    }

    /**
     * Edit an object. This pops up a window containing the editor.
     *
     * @param object the object to edit
     */
    protected void edit(final IMObject object) {
        FocusTree focus = _context.getFocusTree();
        int focusIndex = focus.size();
        if (_editor != null) {
            focusIndex = focus.indexOf(_editor.getFocusGroup());
            focus.remove(_editor.getFocusGroup());

            _editor.removePropertyChangeListener(
                    IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                    _componentListener);
            _editBox.remove(_editor.getComponent());
        } else {
            _editBox = GroupBoxFactory.create();
            _editBox.setInsets(new Insets(0));
            _component.add(_editBox);
        }
        _editor = getEditor(object);
        _editBox.add(_editor.getComponent());
        _editBox.setTitle(_editor.getTitle());
        focus.add(focusIndex, _editor.getFocusGroup());
        _editor.addPropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                _componentListener);
        _editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                _listeners.notifyListeners(
                        AbstractIMObjectCollectionEditor.this);
            }
        });
    }

    /**
     * Returns an editor for an object, creating one if it doesn't exist.
     *
     * @param object the object to edit
     * @return an editor for the object
     */
    protected IMObjectEditor getEditor(IMObject object) {
        IMObjectEditor editor = _collection.getEditor(object);
        if (editor == null) {
            editor = createEditor(object, _context);
            editor.addModifiableListener(_broadcaster);
            _collection.setEditor(object, editor);
        }
        return editor;
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
     * Adds the object being edited to the collection, if it doesn't exist.
     *
     * @param editor the editor
     */
    protected boolean addEdited(IMObjectEditor editor) {
        IMObject object = editor.getObject();
        return _collection.add(object);
    }

    /**
     * Populates the table.
     */
    protected void populateTable() {
        List<IMObject> objects = _collection.getObjects();
        ResultSet set = new PreloadedResultSet<IMObject>(objects, ROWS);
        _table.setResultSet(set);
    }

    /**
     * Remove the editor.
     */
    private void removeEditor() {
        FocusTree focus = _context.getFocusTree();
        focus.remove(_editor.getFocusGroup());
        _editBox.remove(_editor.getComponent());
        _component.remove(_editBox);
        _editor = null;
        _editBox = null;
    }

    /**
     * Saves any current edits.
     *
     * @return <code>true</code> if edits were saved successfully, otherwise
     *         <code>false</code>
     */
    private boolean saveCurrentEdits() {
        boolean saved = false;
        if (addCurrentEdits(new Validator())) {
            saved = _collection.save();
        }
        return saved;
    }

    /**
     * Adds any current edits. If there are edits to add and they are valid,
     * the table will be repopulated and the edited object reselected.
     *
     * @return <code>true</code> if the edits were added,
     *         otherwise <code>false</code>
     */
    private boolean addCurrentEdits(Validator validator) {
        boolean added = true;
        if (_editor != null && _editor.isModified()) {
            if (!validator.validate(_editor)) {
                added = false;
            } else {
                if (addEdited(_editor)) {
                    populateTable();
                    IMObject object = _editor.getObject();
                    _table.getTable().setSelected(object);
                    _listeners.notifyListeners(this);
                }
            }
        }
        return added;
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
