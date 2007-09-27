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

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;

import java.util.HashMap;
import java.util.Map;


/**
 * A cache for {@link ReminderType}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderTypeCache {

    /**
     * The cached reminder types.
     */
    private final Map<IMObjectReference, ReminderType> reminderTypes
            = new HashMap<IMObjectReference, ReminderType>();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>ReminderTypeCache</tt>.
     */
    public ReminderTypeCache() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>ReminderTypeCache</tt>.
     *
     * @param service the archetype service
     */
    public ReminderTypeCache(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Adds a reminder type to the cache.
     *
     * @param reminderType the reminder type to add
     */
    public void add(Entity reminderType) {
        reminderTypes.put(reminderType.getObjectReference(),
                          new ReminderType(reminderType));
    }

    /**
     * Returns a reminder type given its corresponding
     * <em>entity.reminderType</em> reference.
     *
     * @param ref the reminder type reference
     * @return the corresponding reminder type, or <tt>null</tt> if none
     *         can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ReminderType get(IMObjectReference ref) {
        ReminderType result = null;
        if (ref != null) {
            result = reminderTypes.get(ref);
            if (result == null) {
                Entity entity = getEntity(ref);
                if (entity != null) {
                    result = new ReminderType(entity);
                }
                reminderTypes.put(ref, result);
            }
        }
        return result;
    }

    /**
     * Helper to return an <em>entity.reminderType</em> given its reference.
     *
     * @param ref the reference
     * @return the corresponding entity or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity getEntity(IMObjectReference ref) {
        return (Entity) ArchetypeQueryHelper.getByObjectReference(
                service, ref);
    }

}
