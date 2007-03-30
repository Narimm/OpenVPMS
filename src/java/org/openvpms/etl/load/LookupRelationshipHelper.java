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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;


/**
 * Lookup relationship helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LookupRelationshipHelper {

    /**
     * Source lookup short name.
     */
    private String source;

    /**
     * Target lookup short name.
     */
    private String target;

    /**
     * Constructs a new <tt>LookupRelationshipHelper</tt>.
     *
     * @param relationship the lookup relationship
     * @param service      the archetype service
     */
    public LookupRelationshipHelper(LookupRelationship relationship,
                                    IArchetypeService service) {
        this(relationship.getArchetypeId().getShortName(), service);
    }

    /**
     * Constructs a new <tt>LookupRelationshipHelper</tt>.
     *
     * @param shortName the lookup relationship short name
     * @param service   the archetype service
     */
    public LookupRelationshipHelper(String shortName,
                                    IArchetypeService service) {
        ArchetypeDescriptor archetype = service.getArchetypeDescriptor(
                shortName);
        if (archetype != null) {
            source = getShortName(archetype, "source", service);
            target = getShortName(archetype, "target", service);
        }
    }

    /**
     * Returns the source lookup short name.
     *
     * @return the source lookup short name. May be <tt>null</tt>
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the target lookup short name.
     *
     * @return the target lookup short name. May be <tt>null</tt>
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns the archetype short name for a node.
     *
     * @param descriptor the archetype descriptor
     * @param name       the node name
     * @param service    the archetype service
     * @return the short name for the node, or <tt>null</tt>
     */
    private String getShortName(ArchetypeDescriptor descriptor,
                                String name, IArchetypeService service) {
        NodeDescriptor node = descriptor.getNodeDescriptor(name);
        if (node != null) {
            String[] shortNames = DescriptorHelper.getShortNames(node, service);
            if (shortNames.length != 0) {
                return shortNames[0];
            }
        }
        return null;
    }

}
