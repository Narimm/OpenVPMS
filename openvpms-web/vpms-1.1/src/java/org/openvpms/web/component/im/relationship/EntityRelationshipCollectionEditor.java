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
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.edit.IMTableCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * Editor for collections of {@link EntityRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipCollectionEditor
        extends IMTableCollectionEditor<RelationshipState> {

    /**
     * Determines if inactive relationships should be displayed.
     */
    private CheckBox hideInactive;


    /**
     * Constructs a new <tt>EntityRelationshipCollectionEditor</tt>.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public EntityRelationshipCollectionEditor(CollectionProperty property,
                                              Entity object,
                                              LayoutContext context) {
        this(new EntityRelationshipCollectionPropertyEditor(property, object),
             object, context);
    }

    /**
     * Construct a new <tt>EntityRelationshipCollectionEditor</tt>.
     *
     * @param editor  the collection property editor
     * @param object  the object being edited
     * @param context the layout context
     */
    protected EntityRelationshipCollectionEditor(
            EntityRelationshipCollectionPropertyEditor editor,
            IMObject object,
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
        EntityRelationshipCollectionPropertyEditor editor
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
        EntityRelationshipCollectionPropertyEditor editor
                = getCollectionPropertyEditor();
        RelationshipState state
                = editor.getRelationshipState((EntityRelationship) object);
        getTable().getTable().setSelected(state);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be <tt>null</tt>
     */
    protected IMObject getSelected() {
        RelationshipState selected = getTable().getTable().getSelected();
        return (selected != null) ? selected.getRelationship() : null;
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    protected ResultSet<RelationshipState> createResultSet() {
        EntityRelationshipCollectionPropertyEditor editor
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
            public void actionPerformed(ActionEvent event) {
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
    protected EntityRelationshipCollectionPropertyEditor
            getCollectionPropertyEditor() {
        return (EntityRelationshipCollectionPropertyEditor)
                super.getCollectionPropertyEditor();
    }

    /**
     * Invoked when the 'hide inactive' checkbox changes.
     */
    private void onHideInactiveChanged() {
        EntityRelationshipCollectionPropertyEditor editor
                = getCollectionPropertyEditor();
        editor.setExcludeInactive(hideInactive.isSelected());
        populateTable();
    }

}
