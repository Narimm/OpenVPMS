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

package org.openvpms.web.component.im.view.act;

import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.AbstractIMObjectCollectionViewer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Viewer for collections of {@link ActRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActRelationshipCollectionViewer
        extends AbstractIMObjectCollectionViewer {


    /**
     * Construct a new <code>ActRelationshipCollectionViewer</code>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     */
    public ActRelationshipCollectionViewer(CollectionProperty property,
                                           IMObject parent) {
        super(property, parent);
    }

    /**
     * Creates a new table model.
     *
     * @return a new table model
     */
    @Override
    protected IMObjectTableModel<IMObject> createTableModel() {
        NodeDescriptor descriptor = getProperty().getDescriptor();
        Set<String> matches = new HashSet<String>();
        String[] relationshipTypes = DescriptorHelper.getShortNames(descriptor);
        for (String relationshipType : relationshipTypes) {
            ArchetypeDescriptor relationship
                    = DescriptorHelper.getArchetypeDescriptor(relationshipType);
            NodeDescriptor target = relationship.getNodeDescriptor("target");
            for (String shortName : target.getArchetypeRange()) {
                matches.add(shortName);
            }
        }
        String[] shortNames = matches.toArray(new String[0]);

        return IMObjectTableModelFactory.create(shortNames, getLayoutContext());
    }

    /**
     * Returns the objects to display.
     *
     * @return the objects to display
     */
    @Override
    protected List<IMObject> getObjects() {
        List<IMObject> objects = super.getObjects();
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject object : objects) {
            ActRelationship relationship = (ActRelationship) object;
            IMObject target = IMObjectHelper.getObject(
                    relationship.getTarget());
            if (target != null) {
                result.add(target);
            }
        }
        return result;
    }

}
