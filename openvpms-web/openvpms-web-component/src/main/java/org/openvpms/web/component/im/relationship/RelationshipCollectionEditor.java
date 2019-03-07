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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMTableCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.table.TableNavigator;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * Editor for collections of {@link IMObjectRelationship}s.
 * <p/>
 * This ensures that if a relationship has a 'default' node name, only one relationship may be selected as default.
 *
 * @author Tim Anderson
 */
public class RelationshipCollectionEditor extends IMTableCollectionEditor<RelationshipState> {

    /**
     * Determines if inactive relationships should be displayed.
     */
    private CheckBox hideInactive;

    /**
     * Default relationship node name.
     */
    private static final String DEFAULT = "default";

    /**
     * Constructs a {@link RelationshipCollectionEditor}.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected RelationshipCollectionEditor(RelationshipCollectionPropertyEditor editor, IMObject object,
                                           LayoutContext context) {
        super(editor, object, context);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected IMTableModel<RelationshipState> createTableModel(LayoutContext context) {
        RelationshipCollectionPropertyEditor editor = getCollectionPropertyEditor();
        String[] archetypes = editor.getArchetypeRange();
        if (IMObjectTableModelFactory.hasModel(archetypes, RelationshipDescriptorTableModel.class)) {
            // delegate to a RelationshipDescriptorTableModel if one is present for the archetypes
            if (!(context.getComponentFactory() instanceof TableComponentFactory)) {
                context = new DefaultLayoutContext(context);
                context.setComponentFactory(new TableComponentFactory(context));
            }
            IMObjectTableModel<IMObjectRelationship> model = IMObjectTableModelFactory.create(archetypes, context);
            return new DelegatingRelationshipStateTableModel(model, context);
        }
        return new RelationshipStateTableModel(context, editor.parentIsSource());
    }

    /**
     * Selects an object in the table.
     *
     * @param object the object to select
     */
    protected void setSelected(IMObject object) {
        RelationshipCollectionPropertyEditor editor = getCollectionPropertyEditor();
        RelationshipState state = editor.getRelationshipState((IMObjectRelationship) object);

        PagedIMTable<RelationshipState> table = getTable();
        table.setSelected(state);

        enableNavigation(table.getSelected() != null, true);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be {@code null}
     */
    protected IMObject getSelected() {
        RelationshipState selected = getTable().getSelected();
        return (selected != null) ? selected.getRelationship() : null;
    }

    /**
     * Selects the object prior to the selected object, if one is available.
     *
     * @return the prior object. May be {@code null}
     */
    protected IMObject selectPrevious() {
        IMObject result = null;
        PagedIMTable<RelationshipState> table = getTable();
        TableNavigator navigator = table.getNavigator();
        if (navigator.selectPreviousRow()) {
            result = table.getSelected().getRelationship();
            setSelected(result);
        }
        return result;
    }

    /**
     * Selects the object after the selected object, if one is available.
     *
     * @return the next object. May be {@code null}
     */
    protected IMObject selectNext() {
        IMObject result = null;
        PagedIMTable<RelationshipState> table = getTable();
        TableNavigator navigator = table.getNavigator();
        if (navigator.selectNextRow()) {
            result = table.getSelected().getRelationship();
            setSelected(result);
        }
        return result;
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    protected ResultSet<RelationshipState> createResultSet() {
        RelationshipCollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<RelationshipState> relationships = new ArrayList<>(editor.getRelationships());
        return new RelationshipStateResultSet(relationships, ROWS);
    }

    /**
     * Creates the row of controls.
     *
     * @return the row of controls
     */
    @Override
    protected ButtonRow createControls(FocusGroup focus) {
        ButtonRow row = super.createControls(focus);
        String name = getProperty().getDisplayName();
        String label = Messages.format("relationship.hide.inactive", name);
        hideInactive = CheckBoxFactory.create(null, true);
        hideInactive.setText(label);
        hideInactive.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onHideInactiveChanged();
            }
        });
        row.add(hideInactive);
        focus.add(hideInactive);
        return row;
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    @Override
    protected RelationshipCollectionPropertyEditor getCollectionPropertyEditor() {
        return (RelationshipCollectionPropertyEditor) super.getCollectionPropertyEditor();
    }

    /**
     * Invoked when the current editor is modified.
     */
    @Override
    protected void onCurrentEditorModified() {
        IMObjectEditor currentEditor = getCurrentEditor();
        if (currentEditor != null) {
            updateDefaults(currentEditor);
        }
        super.onCurrentEditorModified();
    }

    /**
     * Ensures that if the current object is specified as a default, no other object is.
     *
     * @param currentEditor the current editor
     */
    protected void updateDefaults(IMObjectEditor currentEditor) {
        IMObject current = currentEditor.getObject();
        Property property = currentEditor.getProperty(DEFAULT);
        if (property != null && property.getBoolean()) {
            for (IMObject object : getCurrentObjects()) {
                if (!current.equals(object)) {
                    IMObjectEditor editor = getEditor(object);
                    if (editor != null) {
                        editor.getProperty(DEFAULT).setValue(false);
                    }
                }
            }
        }
    }

    /**
     * Invoked when the 'hide inactive' checkbox changes.
     */
    private void onHideInactiveChanged() {
        RelationshipCollectionPropertyEditor editor = getCollectionPropertyEditor();
        boolean selected = hideInactive.isSelected();
        editor.setExcludeInactive(selected);
        IMTableModel<RelationshipState> model = getTable().getModel().getModel();
        if (model instanceof RelationshipStateTableModel) {
            ((RelationshipStateTableModel) model).setShowActive(!selected);
        }

        refresh();
    }
}
