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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit;

import echopointng.GroupBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.table.SortableTableModel;
import org.openvpms.web.component.table.TableNavigator;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GroupBoxFactory;
import org.openvpms.web.component.util.KeyStrokeHelper;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * Editor for a collection of {@link IMObject}s. The collection is displayed
 * in a table. When an item is selected, an editor containing it is displayed
 * in a box beneath the table.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class IMTableCollectionEditor<T>
    extends AbstractIMObjectCollectionEditor {

    /**
     * The container.
     */
    private Column container;

    /**
     * Collection to edit.
     */
    private PagedIMTable<T> table;

    /**
     * The archetype short name used to create a new object.
     */
    private String shortName;

    /**
     * The edit group box.
     */
    private GroupBox editBox;

    /**
     * The focus group.
     */
    private FocusGroup focusGroup;

    /**
     * The current editor's focus group.
     */
    private FocusGroup editorFocusGroup;

    /**
     * Listener for component change events.
     */
    private final PropertyChangeListener componentListener;

    /**
     * The listener for editor events.
     */
    private final ModifiableListener editorListener;

    /**
     * Determines if the current editor has been modified since being displayed.
     */
    private boolean editorModified;

    /**
     * The column style.
     */
    private static final String COLUMN_STYLE = "CellSpacing";

    /**
     * The no. of rows to display.
     */
    protected static final int ROWS = 15;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(
        IMTableCollectionEditor.class);

    /**
     * The 'add' button identifier.
     */
    private static final String ADD_ID = "add";

    /**
     * The 'delete' button identifier.
     */
    private static final String DELETE_ID = "delete";

    /**
     * The 'previous' button identifier.
     */
    private static final String PREVIOUS_ID = "previous";

    /**
     * The 'next' button identifier.
     */
    private static final String NEXT_ID = "next";

    /**
     * The buttons.
     */
    private ButtonRow buttons;


    /**
     * Creates a new <tt>IMTableCollectionEditor</tt>.
     *
     * @param editor  the editor
     * @param object  the parent object
     * @param context the layout context
     */
    public IMTableCollectionEditor(CollectionPropertyEditor editor,
                                   IMObject object, LayoutContext context) {
        super(editor, object, context);

        context = getContext();

        // filter out the "id" field
        NodeFilter idFilter = new NamedNodeFilter("id");
        NodeFilter filter = FilterHelper.chain(
            idFilter, context.getDefaultNodeFilter());
        context.setNodeFilter(filter);

        componentListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                onComponentChange(event);
            }
        };

        editorListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onCurrentEditorModified();
            }
        };
    }

    /**
     * Creates a new object, subject to a short name being selected, and
     * current collection cardinality. This must be registered with the
     * collection.
     * <p/>
     * If an {@link IMObjectCreationListener} is registered, it will be
     * notified on successful creation of an object.
     *
     * @return a new object, or <tt>null</tt> if the object can't be created
     */
    public IMObject create() {
        IMObject object = null;
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        if (shortName != null) {
            int maxSize = editor.getMaxCardinality();
            if (maxSize == -1 || editor.getObjects().size() < maxSize) {
                object = IMObjectCreator.create(shortName);
            }
        }
        IMObjectCreationListener creationListener = getCreationListener();
        if (creationListener != null) {
            creationListener.created(object);
        }
        return object;
    }

    /**
     * Removes an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        // remove the current editor if it matches the object being deleted.
        // This won't generate any events.
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null && editor.getObject() == object) {
            removeCurrentEditor();
        }
        // remove the object from the collection. May generate events
        boolean removed = getCollectionPropertyEditor().remove(object);
        refresh();
        if (!removed) {
            // the object was not committed, so no notification has been
            // generated yet
            getListeners().notifyListeners(getProperty());
        }
        // workaround for OVPMS-629
        KeyStrokeHelper.reregisterKeyStrokeListeners(container);
    }

    /**
     * Refreshes the collection display.
     */
    public void refresh() {
        if (table != null) {
            populateTable();
            enableNavigation(true);
        }
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or <tt>null</tt> if the editor hasn't been
     *         rendered
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
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
     * Selects the object prior to the selected object, if one is available.
     *
     * @return the prior object. May be <tt>null</tt>
     */
    protected abstract IMObject selectPrevious();

    /**
     * Selects the object after the selected object, if one is available.
     *
     * @return the next object. May be <tt>null</tt>
     */
    protected abstract IMObject selectNext();

    /**
     * Creates a new result set.
     *
     * @return a new result set
     */
    protected abstract ResultSet<T> createResultSet();

    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    protected Component doLayout(LayoutContext context) {
        container = ColumnFactory.create(COLUMN_STYLE);
        focusGroup = new FocusGroup(ClassUtils.getShortClassName(getClass()));

        table = new PagedIMTable<T>(createTableModel(context));
        table.getTable().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onEdit();
            }
        });

        doLayout(container, context);
        return container;
    }

    /**
     * Lays out the component in the specified container.
     *
     * @param container the container
     * @param context   the layout context
     */
    protected void doLayout(Component container, LayoutContext context) {
        if (!isCardinalityReadOnly()) {
            Row row = createControls(focusGroup);
            container.add(row);
        }

        refresh();

        focusGroup.add(table);
        container.add(table);
    }

    /**
     * Creates the row of controls.
     *
     * @param focus the focus group
     * @return the row of controls
     */
    protected Row createControls(FocusGroup focus) {
        String[] range = getCollectionPropertyEditor().getArchetypeRange();
        range = DescriptorHelper.getShortNames(range, false); // expand any wildcards

        buttons = new ButtonRow(focus);

        boolean disableShortcut;

        // Only use button shortcuts for the first level of collections
        // as multiple collections may be displayed on the one form
        disableShortcut = getContext().getLayoutDepth() > 1;

        ActionListener addListener = new ActionListener() {
            public void onAction(ActionEvent event) {
                onNew();
            }
        };
        buttons.addButton(ADD_ID, addListener, disableShortcut);

        ActionListener deleteListener = new ActionListener() {
            public void onAction(ActionEvent event) {
                onDelete();
            }
        };
        buttons.addButton(DELETE_ID, deleteListener, disableShortcut);

        ActionListener previous = new ActionListener() {
            public void onAction(ActionEvent event) {
                onPrevious();
            }
        };
        buttons.addButton(PREVIOUS_ID, previous, disableShortcut);

        ActionListener next = new ActionListener() {
            public void onAction(ActionEvent event) {
                onNext();
            }
        };
        buttons.addButton(NEXT_ID, next, disableShortcut);

        if (range.length == 1) {
            shortName = range[0];
        } else if (range.length > 1) {
            final ShortNameListModel model
                = new ShortNameListModel(range, false, false);
            final SelectField archetypeNames = SelectFieldFactory.create(model);
            int index = archetypeNames.getSelectedIndex();
            shortName = model.getShortName(index);

            archetypeNames.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    int index = archetypeNames.getSelectedIndex();
                    if (index != -1) {
                        shortName = model.getShortName(index);
                    }
                }
            });
            archetypeNames.setCellRenderer(new ShortNameListCellRenderer());
            buttons.add(archetypeNames);
            focus.add(archetypeNames);
        }
        return buttons;
    }

    /**
     * Adds the object being edited to the collection, if it doesn't exist.
     * <p/>
     * The object will be selected if visible in the table.
     *
     * @param editor the editor
     * @return <tt>true</tt> if the object was added, otherwise <tt>false</tt>
     */
    @Override
    protected boolean addEdited(IMObjectEditor editor) {
        boolean added = super.addEdited(editor);
        if (added || editorModified || editor != getCurrentEditor()) {
            if (editor == getCurrentEditor()) {
                editorModified = false;
            }
            refresh();  // refresh the table
        }
        IMObject object = editor.getObject();
        setSelected(object);
        return added;
    }

    /**
     * Invoked when the "New" button is pressed. Creates a new instance of the
     * selected archetype, and displays it in an editor.
     *
     * @return the new editor, or <tt>null</tt> if an object couldn't be created
     */
    protected IMObjectEditor onNew() {
        IMObjectEditor editor = null;
        if (addCurrentEdits(new Validator()) && shortName != null) {
            IMObject object = create();
            if (object != null) {
                editor = edit(object);
                addCurrentEdits(new Validator()); // add the object to the table if it is valid
            }
        }
        return editor;
    }

    /**
     * Invoked when the 'delete' button is pressed.
     * If the selected object has been saved, a confirmation dialog will be
     * displayed, prompting to delete it. If the object hasn't been saved,
     * it will be deleted without prompting.
     */
    protected void onDelete() {
        IMObject object;
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null) {
            object = editor.getObject();
        } else {
            object = getSelected();
        }
        if (object != null) {
            if (object.isNew()) {
                remove(object);
            } else {
                confirmDelete(object);
            }
        }
    }

    /**
     * Edits the selected object.
     */
    protected void onEdit() {
        IMObject object = getSelected();
        if (object != null) {
            if (addCurrentEdits(new Validator())) {
                // need to add any edits after getting the selected object
                // as this may change the order within the table
                setSelected(object);
                edit(object);
            } else {
                enableNavigation(false);
            }
        }
    }

    /**
     * Edit an object.
     *
     * @param object the object to edit
     * @return the editor
     */
    protected IMObjectEditor edit(final IMObject object) {
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null) {
            editor.removePropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
                componentListener);
            editBox.remove(editor.getComponent());
        } else {
            editBox = GroupBoxFactory.create();
            editBox.setInsets(new Insets(0));
            container.add(editBox);
        }
        editor = getEditor(object);
        editBox.add(editor.getComponent());
        editBox.setTitle(editor.getTitle());
        editor.addPropertyChangeListener(
            IMObjectEditor.COMPONENT_CHANGED_PROPERTY,
            componentListener);
        editor.addModifiableListener(editorListener);
        changeFocusGroup(editor);
        setCurrentEditor(editor);

        // workaround for OVPMS-629
        KeyStrokeHelper.reregisterKeyStrokeListeners(container);

        enableNavigation(editor.isValid());
        return editor;
    }

    /**
     * Selects the previous object.
     */
    protected void onPrevious() {
        if (addCurrentEdits(new Validator())) {
            IMObject object = selectPrevious();
            if (object != null) {
                edit(object);
            }
        } else {
            enableNavigation(false);
        }
    }

    /**
     * Selects the next object.
     */
    protected void onNext() {
        if (addCurrentEdits(new Validator())) {
            IMObject object = selectNext();
            if (object != null) {
                edit(object);
            }
        } else {
            enableNavigation(false);
        }
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

    /**
     * Returns the table.
     *
     * @return the table
     */
    protected PagedIMTable<T> getTable() {
        return table;
    }

    /**
     * Removes the current editor.
     */
    protected void removeCurrentEditor() {
        IMObjectEditor editor = getCurrentEditor();
        focusGroup.remove(editorFocusGroup);
        editorFocusGroup = null;
        editBox.remove(editor.getComponent());
        container.remove(editBox);
        editor.removePropertyChangeListener(
            IMObjectEditor.COMPONENT_CHANGED_PROPERTY, componentListener);
        editor.removeModifiableListener(editorListener);
        editorModified = false;
        editBox = null;

        super.removeCurrentEditor();
    }

    /**
     * Confirms to delete an object.
     *
     * @param object the object to delete
     */
    private void confirmDelete(final IMObject object) {
        String displayName = DescriptorHelper.getDisplayName(object);
        String title = Messages.get("imobject.collection.delete.title",
                                    displayName);
        String message = Messages.get("imobject.collection.delete.message",
                                      displayName);
        final ConfirmationDialog dialog
            = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                remove(object);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when the editor changes components.
     *
     * @param event the property change event
     */
    protected void onComponentChange(PropertyChangeEvent event) {
        Component oldValue = (Component) event.getOldValue();
        Component newValue = (Component) event.getNewValue();
        editBox.remove(oldValue);
        editBox.add(newValue);
        changeFocusGroup(getCurrentEditor());
    }

    /**
     * Invoked when the current editor is modified.
     */
    protected void onCurrentEditorModified() {
        // flag this as invalid. This is required for collections when incomplete objects can be mapped in and out
        // of the collection
        resetValid(false);

        editorModified = true;
        enableNavigation(getCurrentEditor().isValid());
        getListeners().notifyListeners(this);
    }

    /**
     * Enable/disables the buttons.
     * <p/>
     * Note that the delete button is enabled if {@link #getCurrentEditor()} or {@link #getSelected()} return non-null.
     *
     * @param enable if <tt>true</tt> enable buttons (subject to criteria), otherwise disable them
     */
    protected void enableNavigation(boolean enable) {
        if (buttons != null) {
            boolean add = enable;
            boolean delete = getCurrentEditor() != null || getSelected() != null;
            boolean previous = enable;
            boolean next = enable;
            if (enable) {
                CollectionProperty property = getCollection();
                int maxSize = property.getMaxCardinality();
                add = (maxSize == -1 || property.size() < maxSize);
                TableNavigator navigator = getTable().getNavigator();
                previous = navigator.hasPreviousRow();
                next = navigator.hasNextRow();
            }
            buttons.getButtons().setEnabled(ADD_ID, add);
            buttons.getButtons().setEnabled(DELETE_ID, delete);
            buttons.getButtons().setEnabled(PREVIOUS_ID, previous);
            buttons.getButtons().setEnabled(NEXT_ID, next);
        }
    }

    /**
     * Changes the focus group to that belonging to the specified editor.
     * <p/>
     * The focus is moved to the default focus component for the editor.
     *
     * @param editor the editor
     */
    private void changeFocusGroup(IMObjectEditor editor) {
        int index;
        if (editorFocusGroup == null) {
            index = focusGroup.size();
        } else {
            index = focusGroup.indexOf(editorFocusGroup);
            if (index == -1) {
                log.error("Missing focus group for existing editor");
                index = focusGroup.size();
            } else {
                focusGroup.remove(editorFocusGroup);
            }
        }
        editorFocusGroup = editor.getFocusGroup();
        focusGroup.add(index, editorFocusGroup);
        editorFocusGroup.setFocus();
    }
}
