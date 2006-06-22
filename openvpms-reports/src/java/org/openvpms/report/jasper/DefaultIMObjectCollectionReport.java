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

package org.openvpms.report.jasper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Default report generator for a collection of <code>IMObject</code>s.
 * This displays the name and description nodes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultIMObjectCollectionReport
        extends AbstractIMObjectCollectionReport {

    /**
     * Construct a new <code>DefaultIMObjectCollectionReport</code>.
     *
     * @param descriptor the collection node descriptor
     * @param service    the archetype service
     */
    public DefaultIMObjectCollectionReport(NodeDescriptor descriptor,
                                           IArchetypeService service) {
        super(descriptor, service);
    }

    /**
     * Returns the descriptors of the nodes to display.
     *
     * @return the descriptors of the nodes to display
     */
    protected NodeDescriptor[] getDescriptors() {
        ArchetypeDescriptor archetype = getArchetypes().get(0);
        NodeDescriptor name = archetype.getNodeDescriptor("name");
        NodeDescriptor description = archetype.getNodeDescriptor("description");
        return new NodeDescriptor[]{name, description};
    }

}
