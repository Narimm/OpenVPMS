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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.AbstractIMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;


/**
 * Copy handler for {@link Act}s. This copies acts, act relationships and
 * participations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActCopyHandler extends AbstractIMObjectCopyHandler {

    /**
     * Determines how {@link IMObjectCopier} should treat an object.
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <code>object</code> if the object shouldn't be copied,
     *         <code>null</code> if it should be replaced with
     *         <code>null</code>, or a new instance if the object should be
     *         copied
     */
    public IMObject getObject(IMObject object, IArchetypeService service) {
        IMObject result;
        if (object instanceof Act || object instanceof ActRelationship
                || object instanceof Participation) {
            result = super.getObject(object, service);
        } else {
            result = object;
        }
        return result;
    }
}
