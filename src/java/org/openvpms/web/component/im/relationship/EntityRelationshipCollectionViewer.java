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
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.view.AbstractIMObjectCollectionViewer;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.resource.util.Messages;

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
        Component component = super.doLayout();
        component.add(_hideInactive, 0);
        return component;
    }

    /**
     * Returns the objects to display.
     *
     * @return the objects to display
     */
    @Override
    protected List<IMObject> getObjects() {
        return filter(super.getObjects());
    }

    /**
     * Filters objects.
     * This implementation filters inactive objects, if {@link #hideInactive()}
     * is <code>true</code>.
     *
     * @param objects the objects to filter
     * @return the filtered objects
     */
    protected List<IMObject> filter(List<IMObject> objects) {
        if (hideInactive()) {
            objects = RelationshipHelper.filterInactive(objects, new Date());
        }
        return objects;
    }

    /**
     * Determines if inactive objects should be hidden.
     *
     * @return <code>true</code> if inactive objects should be hidden
     */
    protected boolean hideInactive() {
        return _hideInactive.isSelected();
    }

    /**
     * Invoked when the 'hide inactive' checkbox changes.
     */
    private void onHideInactiveChanged() {
        populateTable();
    }

}
