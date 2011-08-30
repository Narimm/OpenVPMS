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
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Editor for collections of {@link EntityRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipCollectionEditor
        extends IMObjectTableCollectionEditor {

    /**
     * Determines if inactive relationships should be displayed.
     */
    private CheckBox hideInactive;


    /**
     * Constructs a new <code>EntityRelationshipCollectionEditor</code>.
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
     * Construct a new <code>EntityRelationshipCollectionEditor</code>.
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
     * Invoked when the 'hide inactive' checkbox changes.
     */
    private void onHideInactiveChanged() {
        EntityRelationshipCollectionPropertyEditor editor
                = (EntityRelationshipCollectionPropertyEditor)
                getCollectionPropertyEditor();
        editor.setExcludeInactive(hideInactive.isSelected());
        populateTable();
    }

}
