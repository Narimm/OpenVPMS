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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import echopointng.GroupBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.layout.LayoutHelper;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.DefaultIMObjectComponent;
import org.openvpms.web.component.im.view.IMObjectComponent;
import org.openvpms.web.component.im.view.Selection;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GroupBoxFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.keyboard.KeyStrokeHelper;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.SortableTableModel;
import org.openvpms.web.echo.table.TableNavigator;

import java.beans.PropertyChangeListener;


/**
 * Editor for a collection of {@link IMObject}s. The collection is displayed
 * in a table. When an item is selected, an editor containing it is displayed
 * in a box beneath the table.
 *
 * @author Tim Anderson
 */
public abstract class IMTableCollectionEditor<T> extends AbstractEditableIMObjectCollectionEditor {

    /**
     * The no. of rows to display.
     */
    protected static final int ROWS = 15;

    /**
     * Listener for component change events.
     */
    private final PropertyChangeListener componentListener;

    /**
     * The listener for editor events.
     */
    private final ModifiableListener editorListener;

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
     * Determines if the current editor has been modified since being displayed.
     */
    private boolean editorModified;

    /**
     * The buttons.
     */
    private ButtonRow buttons;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMTableCollectionEditor.class);

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
     * Constructs an {@link IMTableCollectionEditor}.
     *
     * @param editor  the editor
     * @param object  the parent object
     * @param context the layout context
     */
    public IMTableCollectionEditor(CollectionPropertyEditor editor, IMObject object, LayoutContext context) {
        super(editor, object, context);

        context = getContext();

        // filter out the "id" field
        NodeFilter idFilter = new NamedNodeFilter("id");
        NodeFilter filter = FilterHelper.chain(
                idFilter, context.getDefaultNodeFilter());
        context.setNodeFilter(filter);

        componentListener = event -> onComponentChange();

        editorListener = modifiable -> onCurrentEditorModified();
    }

    /**
     * Creates a new object, subject to a short name being selected, and
     * current collection cardinality. This must be registered with the
     * collection.
     * <p>
     * If an {@link IMObjectCreationListener} is registered, it will be
     * notified on successful creation of an object.
     *
     * @return a new object, or {@code null} if the object can't be created
     */
    public IMObject create() {
        return create(shortName);
    }

    /**
     * Creates a new object, subject to a short name being selected, and current collection cardinality. This must be
     * registered with the collection.
     * <p>
     * If an {@link IMObjectCreationListener} is registered, it will be notified on successful creation of an object.
     *
     * @param shortName the archetype short name. May be {@code null}
     * @return a new object, or {@code null} if the object can't be created
     */
    public IMObject create(String shortName) {
        IMObject object = null;
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        if (shortName != null && ArrayUtils.contains(getCollectionPropertyEditor().getArchetypeRange(), shortName)) {
            int maxSize = editor.getMaxCardinality();
            if (maxSize == -1 || editor.getObjects().size() < maxSize) {
                object = IMObjectCreator.create(shortName);
                if (object != null) {
                    IMObjectCreationListener creationListener = getCreationListener();
                    if (creationListener != null) {
                        creationListener.created(object);
                    }
                }
            }
        }
        return object;
    }

    /**
     * Adds a new item to the collection, subject to the constraints of {@link #create()}.
     *
     * @return the editor for the item, or {@code null} a new item cannot be created.
     */
    public IMObjectEditor add() {
        return add(shortName);
    }

    /**
     * Adds a new item to the collection, subject to the constraints of {@link #create(String)}.
     *
     * @param shortName the archetype to add
     * @return the editor for the item, or {@code null} a new item cannot be created.
     */
    public IMObjectEditor add(String shortName) {
        IMObjectEditor editor = null;
        if (addCurrentEdits(new DefaultValidator())) {
            IMObject object = create(shortName);
            if (object != null) {
                editor = edit(object);
                addCurrentEdits(new DefaultValidator()); // add the object to the table if it is valid
            }
        }
        return editor;
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
        CollectionPropertyEditor collection = getCollectionPropertyEditor();
        boolean present = collection.getObjects().contains(object);
        boolean removed = collection.remove(object);
        if (removed) {
            if (selectNext() == null) {
                selectPrevious();
            }
        }
        refresh();
        if (!(present && removed)) {
            // the object was not committed, so no notification has been generated yet
            // RelationshipCollectionTargetEditor doesn't commit the object to the collection until saved, but
            // the editor can still be be removed without generating a callback
            getListeners().notifyListeners(getProperty());
        }
        // workaround for OVPMS-629
        KeyStrokeHelper.reregisterKeyStrokeListeners(container);
    }

    /**
     * Refreshes the collection display.
     */
    public void refresh() {
        refresh(true);
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been
     * rendered
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
     * Refreshes the collection display.
     *
     * @param preserveSelection if {@code true}, preserve the current selection
     */
    protected void refresh(boolean preserveSelection) {
        if (table != null) {
            if (preserveSelection) {
                IMObject object = getSelected();
                populateTable();
                setSelected(object);
            } else {
                populateTable();
            }
            enableNavigation(true);
        }
    }

    /**
     * Selects an object in the table.
     *
     * @param object the object to select
     */
    protected abstract void setSelected(IMObject object);

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be {@code null}
     */
    protected abstract IMObject getSelected();

    /**
     * Returns the target of a selection.
     * <p>
     * This is to support situations where a selection path from a viewer uses a related object to that used by the
     * editor.
     * <p>
     * This implementation returns {@code object}.
     *
     * @param object the selected object
     * @return the selection target
     */
    protected IMObject getSelectionTarget(IMObject object) {
        return object;
    }

    /**
     * Selects the object prior to the selected object, if one is available.
     *
     * @return the prior object. May be {@code null}
     */
    protected abstract IMObject selectPrevious();

    /**
     * Selects the object after the selected object, if one is available.
     *
     * @return the next object. May be {@code null}
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
        container = new IMObjectCollectionComponent();
        container.setStyleName(Styles.CELL_SPACING);
        focusGroup = new FocusGroup(ClassUtils.getShortClassName(getClass()));

        table = new PagedIMTable<>(createTableModel(context));
        table.getTable().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelected();
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

        focusGroup.add(table.getFocusGroup());
        container.add(table.getComponent());
    }

    /**
     * Creates the row of controls.
     *
     * @param focus the focus group
     * @return the row of controls
     */
    protected ButtonRow createControls(FocusGroup focus) {
        buttons = new ButtonRow(focus);

        addButtons(buttons);
        addArchetypeSelector(buttons, focus);
        return buttons;
    }

    /**
     * Adds add/delete buttons to the button row, and previous/next buttons, if the cardinality is > 1.
     *
     * @param buttons the buttons to add to
     */
    protected void addButtons(ButtonRow buttons) {
        boolean disableShortcut;

        // Only use button shortcuts for the first level of collections
        // as multiple collections may be displayed on the one form
        disableShortcut = getContext().getLayoutDepth() > 1;

        ActionListener addListener = new ActionListener() {
            public void onAction(ActionEvent event) {
                onAdd();
            }
        };
        buttons.addButton(ADD_ID, disableShortcut, addListener);

        ActionListener deleteListener = new ActionListener() {
            public void onAction(ActionEvent event) {
                onDelete();
            }
        };
        buttons.addButton(DELETE_ID, disableShortcut, deleteListener);

        int cardinality = getCollection().getMaxCardinality();
        if (cardinality > 1 || cardinality == -1) {
            ActionListener previous = new ActionListener() {
                public void onAction(ActionEvent event) {
                    onPrevious();
                }
            };
            buttons.addButton(PREVIOUS_ID, disableShortcut, previous);

            ActionListener next = new ActionListener() {
                public void onAction(ActionEvent event) {
                    onNext();
                }
            };
            buttons.addButton(NEXT_ID, disableShortcut, next);
        }
    }

    /***
     * Adds an archetype selector to the button row, if there is more than one archetype.
     *
     * @param buttons the buttons
     * @param focus   the focus group
     */
    protected void addArchetypeSelector(ButtonRow buttons, FocusGroup focus) {
        String[] range = getCollectionPropertyEditor().getArchetypeRange();
        range = DescriptorHelper.getShortNames(range, false); // expand any wildcards
        if (range.length == 1) {
            shortName = range[0];
        } else if (range.length > 1) {
            final ShortNameListModel model = new ShortNameListModel(range, false, false);
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
    }

    /**
     * Adds the object being edited to the collection, if it doesn't exist.
     * <p>
     * The object will be selected if visible in the table.
     *
     * @param editor the editor
     * @return {@code true} if the object was added, otherwise {@code false}
     */
    @Override
    protected boolean addEdited(IMObjectEditor editor) {
        boolean added = super.addEdited(editor);
        if (added || editorModified || editor != getCurrentEditor()) {
            if (editor == getCurrentEditor()) {
                editorModified = false;
            }
            refresh(false);  // refresh the table, without preserving the current selection
        }
        IMObject object = editor.getObject();
        setSelected(object);
        return added;
    }

    /**
     * Returns the selected archetype short name.
     */
    protected String getShortName() {
        return shortName;
    }

    /**
     * Invoked when the "Add" button is pressed. Creates a new instance of the selected archetype, and displays it in
     * an editor.
     *
     * @return the new editor, or {@code null} if one could not be created
     */
    protected IMObjectEditor onAdd() {
        return add();
    }

    /**
     * Invoked when the 'delete' button is pressed.
     * If the selected object has been saved, delegates to the registered
     * {@link #getRemoveConfirmationHandler() RemoveConfirmationHandler},
     * or uses {@link DefaultRemoveConfirmationHandler} if none is registered.
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
            RemoveConfirmationHandler handler = getRemoveConfirmationHandler();
            if (handler == null) {
                handler = DefaultRemoveConfirmationHandler.INSTANCE;
            }
            handler.remove(object, this);
        }
    }

    /**
     * Invoked when an object is selected in the table.
     * This implementation edits the selected object.
     */
    protected void onSelected() {
        IMObject object = getSelected();
        if (object != null) {
            if (addCurrentEdits(new DefaultValidator())) {
                // need to add any edits after getting the selected object as this may change the order within the table
                setSelected(object);
                edit(object);
            } else {
                // the current editor is invalid, so reselect its object in the table
                IMObjectEditor current = getCurrentEditor();
                if (current != null) {
                    object = current.getObject();
                } else {
                    object = null;
                }
                setSelected(object);
                enableNavigation(false);
                if (current != null && current.getFocusGroup() != null) {
                    current.getFocusGroup().setFocus();
                }
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
            editor.removePropertyChangeListener(IMObjectEditor.COMPONENT_CHANGED_PROPERTY, componentListener);
            editBox.removeAll();
        } else {
            editBox = GroupBoxFactory.create();
            editBox.setInsets(new Insets(0));
        }
        container.add(editBox); // add even if present due to bug in GroupBox removal code
        editor = getEditor(object);
        Component component = editor.getComponent();
        if (LayoutHelper.needsInset(component)) {
            component = ColumnFactory.create(Styles.INSET, component);
        }
        editBox.add(component);
        editBox.setTitle(editor.getTitle());
        editor.addPropertyChangeListener(IMObjectEditor.COMPONENT_CHANGED_PROPERTY, componentListener);
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
        if (addCurrentEdits(new DefaultValidator())) {
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
        if (addCurrentEdits(new DefaultValidator())) {
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
        IMTableModel<T> model = table.getTable().getModel();
        int sortColumn = -1;
        boolean ascending = (table.getResultSet() != null) && table.getResultSet().isSortedAscending();
        if (model instanceof SortableTableModel) {
            SortableTableModel sortable = ((SortableTableModel) model);
            sortColumn = sortable.getSortColumn();
        }

        ResultSet<T> set = createResultSet();
        table.setResultSet(set);
        if (model instanceof SortableTableModel) {
            // if no column is currently sorted, sort using the prior sort, or the default (if any)
            SortableTableModel sortable = ((SortableTableModel) model);
            if (sortable.getSortColumn() == -1) {
                if (sortColumn != -1) {
                    sortable.sort(sortColumn, ascending);
                } else if (model.getDefaultSortColumn() != -1) {
                    sortable.sort(model.getDefaultSortColumn(), model.getDefaultSortAscending());
                }
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
     * Sets the current editor.
     * <p>
     * This registers a listener so that {@link #onCurrentEditorModified()} is invoked when the editor changes.
     * If there is an existing editor, its listener is removed.
     *
     * @param editor the editor. May be {@code null}
     */
    @Override
    protected void setCurrentEditor(IMObjectEditor editor) {
        IMObjectEditor current = getCurrentEditor();
        if (current != null) {
            current.removeModifiableListener(editorListener);
        }
        if (editor != null) {
            editor.addModifiableListener(editorListener);
        }
        super.setCurrentEditor(editor);
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
        editor.removePropertyChangeListener(IMObjectEditor.COMPONENT_CHANGED_PROPERTY, componentListener);
        editorModified = false;
        editBox = null;

        super.removeCurrentEditor();
    }

    /**
     * Invoked when the editor changes components.
     */
    protected void onComponentChange() {
        editBox.removeAll();
        IMObjectEditor editor = getCurrentEditor();
        editBox.add(editor.getComponent());
        changeFocusGroup(editor);
    }

    /**
     * Invoked when the current editor is modified.
     */
    protected void onCurrentEditorModified() {
        // flag this as invalid. This is required for collections when incomplete objects can be mapped in and out
        // of the collection
        resetValid(false);

        editorModified = true;
        IMObjectEditor current = getCurrentEditor();
        boolean valid = current != null && current.isValid();
        enableNavigation(valid);
        getListeners().notifyListeners(this);
    }

    /**
     * Enable/disables the buttons.
     *
     * @param enable if {@code true} enable buttons (subject to criteria), otherwise disable them
     */
    protected void enableNavigation(boolean enable) {
        enableNavigation(enable, enable);
    }

    /**
     * Enable/disables the buttons.
     * <p>
     * This allows the Add button to be enabled independently of the other buttons.
     *
     * @param enable    if {@code true}, enable buttons (subject to criteria), otherwise disable them
     * @param enableAdd if {@code true}, enable the add button (subject to criteria), otherwise disable it
     */
    protected void enableNavigation(boolean enable, boolean enableAdd) {
        if (buttons != null) {
            enableNavigation(buttons.getButtons(), enable, enableAdd);
        }
    }

    /**
     * Enable/disables the buttons.
     * <p>
     * This allows the Add button to be enabled independently of the other buttons.
     * <p>
     * Note that the delete button is enabled if {@link #getCurrentEditor()} or {@link #getSelected()} return non-null.
     *
     * @param buttons   the buttons
     * @param enable    if {@code true}, enable buttons (subject to criteria), otherwise disable them
     * @param enableAdd if {@code true}, enable the add button (subject to criteria), otherwise disable it
     */
    protected void enableNavigation(ButtonSet buttons, boolean enable, boolean enableAdd) {
        boolean add = enableAdd;
        boolean delete = getCurrentEditor() != null || getSelected() != null;
        boolean previous = enable;
        boolean next = enable;
        if (enable) {
            TableNavigator navigator = getTable().getNavigator();
            previous = navigator.hasPreviousRow();
            next = navigator.hasNextRow();
        }
        if (add) {
            CollectionProperty property = getCollection();
            int maxSize = property.getMaxCardinality();
            add = (maxSize == -1 || property.size() < maxSize);
        }
        buttons.setEnabled(ADD_ID, add);
        buttons.setEnabled(DELETE_ID, delete);
        buttons.setEnabled(PREVIOUS_ID, previous);
        buttons.setEnabled(NEXT_ID, next);
    }

    /**
     * Changes the focus group to that belonging to the specified editor.
     * <p>
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
        if (editorFocusGroup != null) {
            focusGroup.add(index, editorFocusGroup);
            editorFocusGroup.setFocus();
        } else {
            log.warn("Editor " + editor.getClass().getName() + " for " + editor.getDisplayName()
                     + " has no focus");
        }
    }

    /**
     * The root editor component. This hooks the collection editor into the component hierarchy.
     */
    private class IMObjectCollectionComponent extends Column implements IMObjectComponent {

        @Override
        public IMObject getObject() {
            return null;
        }

        @Override
        public String getNode() {
            return IMTableCollectionEditor.this.getProperty().getName();
        }

        @Override
        public IMObjectComponent getSelected() {
            IMObject object = IMTableCollectionEditor.this.getSelected();
            return object != null ? new DefaultIMObjectComponent(object, editBox) : null;
        }

        @Override
        public boolean select(Selection selection) {
            boolean result = false;
            IMObject object = getSelectionTarget(selection.getObject());
            setSelected(object);
            if (object != null && ObjectUtils.equals(object, IMTableCollectionEditor.this.getSelected())) {
                onSelected();
                result = true;
            }
            return result;
        }

        @Override
        public Component getComponent() {
            return editBox;
        }
    }

}
