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

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * A factory for {@link SubreportGenerator} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SubreportGeneratorFactory {

    /**
     * Creates a new sub-report generator.
     *
     * @param descriptor the collection descriptor
     * @param service    the archetype service
     * @return a new sub-report generator
     */
    public static SubreportGenerator create(NodeDescriptor descriptor,
                                            IArchetypeService service) {
        String[] shortNames = descriptor.getArchetypeRange();
        if (TypeHelper.matches(shortNames, "entityRelationship.*")) {
            return new EntityRelationshipSubreportGenerator(descriptor,
                                                            service);

        } else if (TypeHelper.matches(shortNames, "actRelationship.*")) {
            return new ActRelationshipSubreportGenerator(descriptor, service);
        }
        return new DefaultSubreportGenerator(descriptor, service);
    }
}
