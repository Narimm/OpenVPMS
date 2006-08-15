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

package org.openvpms.web.component.im.table.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;

import java.util.HashSet;
import java.util.Set;


/**
 * A table model for {@link ActRelationship}s that models the target acts
 * referred to by the relationships. The model for the target acts is created
 * via {@link IMObjectTableModelFactory} using the union of archetype ranges
 * from each act relationship's target node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultActRelationshipTableModel
        extends AbstractActRelationshipTableModel {


    /**
     * Creates a new <code>DefaultActRelationshipTableModel</code>.
     *
     * @param relationshipTypes the act relationship short names
     * @param context           the layout context
     */
    public DefaultActRelationshipTableModel(String[] relationshipTypes,
                                            LayoutContext context) {
        Set<String> matches = new HashSet<String>();
        for (String relationshipType : relationshipTypes) {
            ArchetypeDescriptor relationship
                    = DescriptorHelper.getArchetypeDescriptor(relationshipType);
            NodeDescriptor target = relationship.getNodeDescriptor("target");
            for (String shortName : target.getArchetypeRange()) {
                matches.add(shortName);
            }
        }
        String[] shortNames = matches.toArray(new String[0]);
        IMObjectTableModel<Act> model
                = IMObjectTableModelFactory.create(shortNames, context);
        setModel(model);
    }

}
