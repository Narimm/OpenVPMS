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

package org.openvpms.web.component.im.relationship;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.edit.IMTableCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.table.TableNavigator;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * Editor for collections of {@link IMObjectRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipCollectionEditor
    extends IMTableCollectionEditor<RelationshipState> {

    /**
     * Determines if inactive relationships should be displayed.
     */
    private CheckBox hideInactive;


    /**
     * Construct a new <tt>RelationshipCollectionEditor</tt>.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected RelationshipCollectionEditor(
        RelationshipCollectionPropertyEditor editor, IMObject object,
        LayoutContext context) {
        super(editor, object, context);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    protected IMTableModel<RelationshipState> createTableModel(
        LayoutContext context) {
        RelationshipCollectionPropertyEditor editor
            = getCollectionPropertyEditor();
        return new RelationshipStateTableModel(context,
                                               editor.parentIsSource());
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

        enableNavigation(table.getSelected() != null);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be <tt>null</tt>
     */
    protected IMObject getSelected() {
        RelationshipState selected = getTable().getSelected();
        return (selected != null) ? selected.getRelationship() : null;
    }

    /**
     * Selects the object prior to the selected object, if one is available.
     *
     * @return the prior object. May be <tt>null</tt>
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
     * @return the next object. May be <tt>null</tt>
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
        RelationshipCollectionPropertyEditor editor
            = getCollectionPropertyEditor();
        List<RelationshipState> relationships
            = new ArrayList<RelationshipState>(editor.getRelationships());
        return new RelationshipStateResultSet(relationships,
                                              editor.parentIsSource(), ROWS);
    }

    /**
     * Creates the row of controls.
     *
     * @return the row of controls
     */
    @Override
    protected Row createControls(FocusGroup focus) {
        Row row = super.createControls(focus);
        String name = getProperty().getDisplayName();
        String label = Messages.get("relationship.hide.inactive", name);
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
        return (RelationshipCollectionPropertyEditor)
            super.getCollectionPropertyEditor();
    }

    /**
     * Invoked when the 'hide inactive' checkbox changes.
     */
    private void onHideInactiveChanged() {
        RelationshipCollectionPropertyEditor editor
            = getCollectionPropertyEditor();
        editor.setExcludeInactive(hideInactive.isSelected());
        refresh();
    }

}
