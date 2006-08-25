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
import nextapp.echo2.app.Column;
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
 * Editor for a collection of {@link IMObject}s. The collection is displayed
 * in a table. When an item is selected, an editor containing it is displayed
 * in a box beneath the table.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class IMObjectTableCollectionEditor
        extends AbstractIMObjectCollectionEditor {

    /**
     * The container.
     */
    private Column _container;

    /**
     * Collection to edit.
     */
    private PagedIMObjectTable<IMObject> _table;

    /**
     * The archetype short name used to create a new object.
     */
    private String _shortname;

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
    protected IMObjectTableCollectionEditor(CollectionPropertyEditor editor,
                                            IMObject object,
                                            LayoutContext context) {
        super(editor, object, new DefaultLayoutContext(context));

        context = getContext();
        // filter out the uid (aka "id") field
        NodeFilter idFilter = new NamedNodeFilter("uid");
        NodeFilter filter = FilterHelper.chain(
                idFilter, context.getDefaultNodeFilter());
        context.setNodeFilter(filter);

        _componentListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                onComponentChange(event);
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
    public IMObjectTableCollectionEditor(CollectionProperty property,
                                         IMObject object,
                                         LayoutContext context) {
        this(new DefaultCollectionPropertyEditor(property), object, context);
    }


    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    protected Component doLayout(LayoutContext context) {
        _container = ColumnFactory.create(COLUMN_STYLE);
        FocusSet focus = new FocusSet("CollectionEditor");
        context.getFocusTree().add(focus);
        Row row = createControls(focus);
        _container.add(row);

        _table = new PagedIMObjectTable<IMObject>(createTableModel(context));
        _table.getTable().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onEdit();
            }
        });

        populateTable();

        focus.add(_table);
        _container.add(_table);
        return _container;
    }

    /**
     * Creates the row of controls.
     *
     * @return the row of controls
     */
    protected Row createControls(FocusSet focus) {
        String[] range = getCollectionPropertyEditor().getArchetypeRange();
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
     * Adds the object being edited to the collection, if it doesn't exist.
     *
     * @param editor the editor
     * @return <code>true</code> if the object was added, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean addEdited(IMObjectEditor editor) {
        boolean added = super.addEdited(editor);
        if (added) {
            populateTable();
            IMObject object = editor.getObject();
            _table.getTable().setSelected(object);
        }
        return added;
    }


    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected IMObjectTableModel<IMObject> createTableModel(
            LayoutContext context) {
        context = new DefaultLayoutContext(context);
        context.setComponentFactory(new TableComponentFactory(context));
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        return IMObjectTableModelFactory.create(editor.getArchetypeRange(),
                                                context);
    }

    /**
     * Invoked when the "New" button is pressed. Creates a new instance of the
     * selected archetype, and displays it in an editor.
     */
    protected void onNew() {
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        if (addCurrentEdits(new Validator()) && _shortname != null) {
            int maxSize = editor.getMaxCardinality();
            if (maxSize == -1 || editor.getObjects().size() < maxSize) {
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
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null) {
            object = editor.getObject();
            removeEditor();
        } else {
            object = _table.getTable().getSelected();
        }
        if (object != null) {
            delete(object);
            getListeners().notifyListeners(this);
        }
    }

    protected void onCancel() {
        if (getCurrentEditor() != null) {
            removeEditor();
        }
    }

    /**
     * Delete an object.
     *
     * @param object the object to delete
     */
    protected void delete(IMObject object) {
        getCollectionPropertyEditor().remove(object);
        populateTable();
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null && editor.getObject() == object) {
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
        LayoutContext context = getContext();
        FocusTree focus = context.getFocusTree();
        int focusIndex = focus.size();
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null) {
            focusIndex = focus.indexOf(editor.getFocusGroup());
            focus.remove(editor.getFocusGroup());

            editor.removePropertyChangeListener(
                    IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                    _componentListener);
            _editBox.remove(editor.getComponent());
        } else {
            _editBox = GroupBoxFactory.create();
            _editBox.setInsets(new Insets(0));
            _container.add(_editBox);
        }
        editor = getEditor(object);
        _editBox.add(editor.getComponent());
        _editBox.setTitle(editor.getTitle());
        focus.add(focusIndex, editor.getFocusGroup());
        editor.addPropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                _componentListener);
        editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                getListeners().notifyListeners(
                        IMObjectTableCollectionEditor.this);
            }
        });
        setCurrentEditor(editor);
    }

    /**
     * Populates the table.
     */
    protected void populateTable() {
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<IMObject> objects = editor.getObjects();
        ResultSet<IMObject> set = new PreloadedResultSet<IMObject>(objects,
                                                                   ROWS);
        _table.setResultSet(set);
    }

    /**
     * Remove the editor.
     */
    private void removeEditor() {
        IMObjectEditor editor = getCurrentEditor();
        LayoutContext context = getContext();
        FocusTree focus = context.getFocusTree();
        focus.remove(editor.getFocusGroup());
        _editBox.remove(editor.getComponent());
        _container.remove(_editBox);
        setCurrentEditor(null);
        _editBox = null;
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
