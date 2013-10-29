/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;


/**
 * Act status helper methods.
 *
 * @author Tim Anderson
 */
public class ActStatusHelper {

    /**
     * Determines if an act was posted previously.
     *
     * @param act     the act
     * @param service the archetype service
     * @return {@code true} if the act was posted previously.
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static boolean isPosted(Act act, IArchetypeService service) {
        boolean result;
        if (!act.isNew()) {
            ArchetypeQuery query = new ArchetypeQuery(new ObjectRefConstraint("act", act.getObjectReference()));
            query.add(new NodeConstraint("act.status", ActStatus.POSTED));
            query.add(new NodeSelectConstraint("act.id"));
            result = !service.getObjects(query).getResults().isEmpty();
        } else {
            result = false;
        }
        return result;
    }

}
