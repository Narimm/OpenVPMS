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
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.focus.FocusSet;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
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
    private CheckBox _hideInactive;

    /**
     * Construct a new <code>EntityRelationshipCollectionEditor</code>.
     *
     * @param property the collection property
     * @param object   the object being edited
     * @param context  the layout context
     */
    public EntityRelationshipCollectionEditor(CollectionProperty property,
                                              IMObject object,
                                              LayoutContext context) {
        super(new EntityRelationshipCollectionPropertyEditor(property),
              object, context);
    }

    /**
     * Creates the row of controls.
     *
     * @return the row of controls
     */
    @Override
    protected Row createControls(FocusSet focus) {
        Row row = super.createControls(focus);
        NodeDescriptor descriptor = getCollection().getDescriptor();
        String name = descriptor.getDisplayName();
        String label = Messages.get("relationship.hide.inactive", name);
        _hideInactive = CheckBoxFactory.create(null, true);
        _hideInactive.setText(label);
        _hideInactive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onHideInactiveChanged();
            }
        });
        row.add(_hideInactive);
        return row;
    }

    /**
     * Invoked when the 'hide inactive' checkbox changes.
     */
    private void onHideInactiveChanged() {
        EntityRelationshipCollectionPropertyEditor editor
                = (EntityRelationshipCollectionPropertyEditor)
                getCollectionPropertyEditor();
        editor.setExcludeInactive(_hideInactive.isSelected());
        populateTable();
    }

}
