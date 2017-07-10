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
 */

package org.openvpms.archetype.rules.party;

import org.openvpms.archetype.rules.util.MappingCopyHandler;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;


/**
 * An {@link IMObjectCopyHandler} for copying {@link EntityRelationship}s and {@link EntityLink}s.
 * This copies the relationship, and references the existing source and target objects.
 *
 * @author Tim Anderson
 */
public class IMObjectRelationshipCopyHandler extends MappingCopyHandler {

    /**
     * Constructs an {@link IMObjectRelationshipCopyHandler}.
     */
    public IMObjectRelationshipCopyHandler() {
        setCopy(EntityRelationship.class, EntityLink.class);
        setDefaultTreatment(Treatment.REFERENCE);
    }

}
