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
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.PagedIMObjectTable;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GroupBoxFactory;
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
    private Column container;

    /**
     * Collection to edit.
     */
    private PagedIMObjectTable<IMObject> table;

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
     * The column style.
     */
    private static final String COLUMN_STYLE = "CellSpacing";

    /**
     * The no. of rows to display.
     */
    private static final int ROWS = 15;

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(IMObjectTableCollectionEditor.class);


    /**
     * Construct a new <code>IMObjectTableCollectionEditor</code>.
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

        // don't want to increase the depth for this context
        context.setLayoutDepth(context.getLayoutDepth() - 1);

        // filter out the uid (aka "id") field
        NodeFilter idFilter = new NamedNodeFilter("uid");
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
                getListeners().notifyListeners(
                        IMObjectTableCollectionEditor.this);
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
     * Creates a new object, subjecf to a short name being selected, and
     * current collection cardinality. This must be registered with the
     * collection.
     *
     * @return a new object, or <code>null</code> if the object can't be created
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
        return object;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or <code>null</code> if the editor hasn't been
     *         rendered
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Lays out the component.
     *
     * @param context the layout context
     * @return the component
     */
    protected Component doLayout(LayoutContext context) {
        container = ColumnFactory.create(COLUMN_STYLE);
        focusGroup = new FocusGroup(ClassUtils.getShortClassName(getClass()));
        Row row = createControls(focusGroup);
        container.add(row);

        table = new PagedIMObjectTable<IMObject>(createTableModel(context));
        table.getTable().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onEdit();
            }
        });

        populateTable();

        focusGroup.add(table);
        container.add(table);
        return container;
    }

    /**
     * Creates the row of controls.
     *
     * @return the row of controls
     */
    protected Row createControls(FocusGroup focus) {
        String[] range = getCollectionPropertyEditor().getArchetypeRange();
        range = DescriptorHelper.getShortNames(range,
                                               false); // expand any wildcards

        ButtonRow buttons = new ButtonRow(focus);

        boolean disableShortcut;

        // Only use button shortcuts for the first level of collections
        // as multiple collections may be displayed on the one form
        disableShortcut = getContext().getLayoutDepth() > 1;

        ActionListener addListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onNew();
            }
        };
        buttons.addButton("add", addListener, disableShortcut);

        ActionListener deleteListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        };
        buttons.addButton("delete", deleteListener, disableShortcut);

        if (range.length == 1) {
            shortName = range[0];
        } else if (range.length > 1) {
            final ShortNameListModel model
                    = new ShortNameListModel(range, false, false);
            final SelectField archetypeNames = SelectFieldFactory.create(model);
            int index = archetypeNames.getSelectedIndex();
            shortName = model.getShortName(index);

            archetypeNames.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
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
     *
     * @param editor the editor
     * @return <code>true</code> if the object was added, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean addEdited(IMObjectEditor editor) {
        boolean added = super.addEdited(editor);
        populateTable();  // refresh the table
        IMObject object = editor.getObject();
        table.getTable().setSelected(object);
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
        if (addCurrentEdits(new Validator()) && shortName != null) {
            IMObject object = create();
            if (object != null) {
                edit(object);
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
            object = table.getTable().getSelected();
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
        IMObject object = table.getTable().getSelected();
        if (object != null) {
            if (addCurrentEdits(new Validator())) {
                // need to add any edits after getting the selected object
                // as this may change the order within the table
                table.getTable().setSelected(object);
                edit(object);
            }
        }
    }

    /**
     * Edit an object.
     *
     * @param object the object to edit
     */
    protected void edit(final IMObject object) {
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
    }

    /**
     * Populates the table.
     */
    protected void populateTable() {
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<IMObject> objects = editor.getObjects();
        ResultSet<IMObject> set = new IMObjectListResultSet<IMObject>(objects,
                                                                      ROWS);
        table.setResultSet(set);
    }

    /**
     * Returns the table.
     *
     * @return the table
     */
    protected PagedIMObjectTable<IMObject> getTable() {
        return table;
    }

    /**
     * Remove the editor.
     */
    private void removeEditor() {
        IMObjectEditor editor = getCurrentEditor();
        focusGroup.remove(editorFocusGroup);
        editorFocusGroup = null;
        editBox.remove(editor.getComponent());
        container.remove(editBox);
        editor.removePropertyChangeListener(
                IMObjectEditor.COMPONENT_CHANGED_PROPERTY, componentListener);
        editor.removeModifiableListener(editorListener);
        setCurrentEditor(null);
        editBox = null;
    }

    /**
     * Invoked when the editor changes components.
     *
     * @param event the property change event
     */
    private void onComponentChange(PropertyChangeEvent event) {
        Component oldValue = (Component) event.getOldValue();
        Component newValue = (Component) event.getNewValue();
        editBox.remove(oldValue);
        editBox.add(newValue);
        changeFocusGroup(getCurrentEditor());
    }

    /**
     * Changes the focus group to that belonging to the specified editor.
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
            }
        }
        focusGroup.add(index, editor.getFocusGroup());
    }

}
