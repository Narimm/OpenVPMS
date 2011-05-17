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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectTableCollectionViewer;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Viewer for collections of {@link IMObjectRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipCollectionTargetViewer
        extends IMObjectTableCollectionViewer {

    /**
     * Determines if inactive relationships should be displayed.
     */
    private CheckBox hideInactive;


    /**
     * Constructs a <tt>RelationshipCollectionTargetViewer</tt>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param context  the layout context. May be <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public RelationshipCollectionTargetViewer(CollectionProperty property,
                                              IMObject parent,
                                              LayoutContext context) {
        super(property, parent, context);
    }


    /**
     * Browse an object.
     *
     * @param object the object to browse.
     */
    @Override
    protected void browse(IMObject object) {
        IMObjectRelationship relationship = (IMObjectRelationship) object;
        IMObject target = IMObjectHelper.getObject(relationship.getTarget());
        if (target != null) {
            browse(target);
        }
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        String name = getProperty().getDisplayName();
        String label = Messages.get("relationship.hide.inactive", name);
        hideInactive = CheckBoxFactory.create(null, true);
        hideInactive.setText(label);
        hideInactive.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onHideInactiveChanged();
            }
        });
        Component component = super.doLayout();
        component.add(hideInactive, 0);
        return component;
    }

    /**
     * Determines if inactive objects should be hidden.
     *
     * @return <code>true</code> if inactive objects should be hidden
     */
    protected boolean hideInactive() {
        return hideInactive.isSelected();
    }

    /**
     * Invoked when the 'hide inactive' checkbox changes.
     */
    private void onHideInactiveChanged() {
        populateTable();
    }

}