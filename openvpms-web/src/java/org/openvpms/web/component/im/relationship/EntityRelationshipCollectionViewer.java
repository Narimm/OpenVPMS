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

import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.view.AbstractIMObjectCollectionViewer;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import java.util.Date;
import java.util.List;


/**
 * Viewer for collections of {@link EntityRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipCollectionViewer
        extends AbstractIMObjectCollectionViewer {

    /**
     * Determines if inactive relationships should be displayed.
     */
    private CheckBox _hideInactive;


    /**
     * Construct a new <code>EntityRelationshipCollectionViewer</code>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     */
    public EntityRelationshipCollectionViewer(CollectionProperty property,
                                              IMObject parent) {
        super(property, parent);
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        NodeDescriptor descriptor = getProperty().getDescriptor();
        String name = descriptor.getDisplayName();
        String label = Messages.get("relationship.hide.inactive", name);
        _hideInactive = CheckBoxFactory.create(null, true);
        _hideInactive.setText(label);
        _hideInactive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onHideInactiveChanged();
            }
        });
        Row row = RowFactory.create("CellSpacing", getTable(), _hideInactive);
        Column column = ColumnFactory.create("WideCellSpacing", row);
        populateTable();
        return column;
    }

    /**
     * Returns the objects to display.
     *
     * @return the objects to display
     */
    @Override
    protected List<IMObject> getObjects() {
        List<IMObject> objects = super.getObjects();
        if (_hideInactive.isSelected()) {
            objects = RelationshipHelper.filterInactive(objects, new Date());
        }
        return objects;
    }

    /**
     * Invoked when the 'hide inactive' checkbox changes.
     */
    private void onHideInactiveChanged() {
        populateTable();
    }

}
