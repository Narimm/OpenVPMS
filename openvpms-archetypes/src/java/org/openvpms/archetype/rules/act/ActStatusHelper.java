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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;


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
        return !act.isNew() && isPosted(act.getObjectReference(), service);
    }

    /**
     * Determines if an act is posted, given its reference.
     *
     * @param reference the act reference. May be {@code null}
     * @param service   the archetype service
     * @return {@code true} if the act is posted previously.
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static boolean isPosted(IMObjectReference reference, IArchetypeService service) {
        String status = getStatus(reference, service);
        return ActStatus.POSTED.equals(status);
    }

    /**
     * Returns the status of an act, given its reference.
     *
     * @param reference the act reference. May be {@code null}
     * @param service   the archetype service
     * @return the act status, or {@code null} if the {@code reference} is not specified, or the act does not exist
     */
    public static String getStatus(IMObjectReference reference, IArchetypeService service) {
        if (reference != null) {
            ArchetypeQuery query = new ArchetypeQuery(new ObjectRefConstraint("act", reference));
            query.add(new NodeSelectConstraint("act.status"));
            ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
            if (iterator.hasNext()) {
                return iterator.next().getString("act.status");
            }
        }
        return null;
    }

}
