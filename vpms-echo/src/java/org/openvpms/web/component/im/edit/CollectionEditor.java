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

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.table.TableNavigator;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Editor for a collection of {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 * @see IMObjectEditor
 */
public class CollectionEditor implements Saveable {

    /**
     * The object to edit.
     */
    private final IMObject _object;

    /**
     * Collection to edit.
     */
    private IMObjectTable _table;

    /**
     * The component representing this.
     */
    private Component _component;

    /**
     * Table navigator.
     */
    private TableNavigator _navigator;

    /**
     * The node descriptor.
     */
    private final NodeDescriptor _descriptor;

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
     * The button row style.
     */
    private static final String ROW_STYLE = "CellSpacingRow";

    /**
     * The column style.
     */
    private static final String COLUMN_STYLE = "CellSpacingColumn";


    /**
     * Construct a new <code>CollectionEditor</code>.
     *
     * @param descriptor the node descriptor
     */
    public CollectionEditor(IMObject object, NodeDescriptor descriptor) {
        _object = object;
        _descriptor = descriptor;

        _componentListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                onComponentChange(event);
            }
        };
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
     * Returns the collection descriptor.
     *
     * @return the collection descriptor
     */
    public NodeDescriptor getDescriptor() {
        return _descriptor;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return <code>true</code> if the object has been modified
     */
    public boolean isModified() {
        if (!_modified) {
            if (_editor != null) {
                _modified = _editor.isModified();
            }
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
     * Lays out the component.
     */
    protected void doLayout() {
        _component = ColumnFactory.create(COLUMN_STYLE);

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

        _table = createTable(false);
        _table.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onEdit();
            }
        });
        _component.add(_table);

        Row row = RowFactory.create(ROW_STYLE, create, cancel, delete);

        String[] range = _descriptor.getArchetypeRange();
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
        }

        _component.add(row);

        populate();
    }

    /**
     * Populates the table.
     */
    protected void populate() {
        List<IMObject> objects = getObjects();
        int size = objects.size();
        if (size != 0) {
            _table.setObjects(objects);

            int rowsPerPage = _table.getRowsPerPage();
            if (_navigator == null && size > rowsPerPage) {
                // display the navigator before the table
                _navigator = new TableNavigator(_table);
                _component.add(_navigator, _component.indexOf(_table));
            } else if (_navigator != null && size <= rowsPerPage) {
                _component.remove(_navigator);
            }
        } else {
            _table.setObjects(new ArrayList<IMObject>());
            if (_navigator != null) {
                _component.remove(_navigator);
            }
        }
    }

    /**
     * Returns the list of objects to display in the table.
     *
     * @return the list objects to display.
     */
    protected List<IMObject> getObjects() {
        List<IMObject> objects = Collections.emptyList();
        Collection values = (Collection) _descriptor.getValue(_object);
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
     * Create a new table.
     *
     * @param deletable determines if entries should be deleteable.
     * @return a new table
     */
    protected IMObjectTable createTable(boolean deletable) {
        return new IMObjectTable(deletable);
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
                    ErrorDialog.show("Failed to create object of type "
                                     + _shortname);
                }
            } catch (ArchetypeServiceException exception) {
                ErrorDialog.show(exception);
            }
        }
    }

    /**
     * Deletes the selected object.
     */
    protected void onDelete() {
        IMObject object = _table.getSelected();
        if (object != null) {
            delete(object);
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
        _table.remove(object);
        _modified = true;
        if (_editor != null && _editor.getObject() == object) {
            removeEditor();
        }
    }

    /**
     * Remove the editor.
     */
    private void removeEditor() {
        _editBox.remove(_editor.getComponent());
        _component.remove(_editBox);
        _editor = null;
        _editBox = null;

    }

    /**
     * Remove an object from the collection.
     *
     * @param object the object to remove
     */
    protected void removeFromCollection(IMObject object) {
        _descriptor.removeChildFromCollection(_object, object);
    }

    /**
     * Edits the selected object.
     */
    protected void onEdit() {
        IMObject object = _table.getSelected();
        if (object != null) {
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
            _editor.removePropertyChangeListener(
                    IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                    _componentListener);
            _editBox.remove(_editor.getComponent());
        } else {
            _editBox = new GroupBox();
            _component.add(_editBox);
        }
        _editor = createEditor(object);
        _editBox.add(_editor.getComponent());
        _editBox.setTitle(_editor.getTitle());
        _editor.addPropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                _componentListener);
    }

    /**
     * Creates a new editor.
     *
     * @param object the object to edit
     * @return an editor to edit <code>object</code>
     */
    protected IMObjectEditor createEditor(IMObject object) {
        boolean showAll = !object.isNew();
        return IMObjectEditorFactory.create(object, _object, _descriptor,
                                            showAll);
    }

    /**
     * Save any current edits.
     *
     * @return <code>true</code> if the save was successful; otherwise
     *         <code>false</code>
     */
    private boolean saveCurrentEdits() {
        boolean result = false;
        if (_editor != null && _editor.isModified()) {
            if (save(_editor)) {
                result = true;
                IMObject object = _editor.getObject();
                if (!_table.getObjects().contains(object)) {
                    _table.add(object);
                }
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Save any edits.
     *
     * @param editor the editor managing the object to save
     * @return <code>true</code> if the save was successful
     */
    protected boolean save(IMObjectEditor editor) {
        return _editor.save();
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
