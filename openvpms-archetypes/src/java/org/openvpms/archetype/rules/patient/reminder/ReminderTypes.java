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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.HashMap;
import java.util.Map;


/**
 * A cache for {@link ReminderType}s.
 *
 * @author Tim Anderson
 */
public class ReminderTypes {

    /**
     * The cached reminder types.
     */
    private final Map<IMObjectReference, ReminderType> reminderTypes = new HashMap<>();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a {@link ReminderTypes}.
     *
     * @param service the archetype service
     */
    public ReminderTypes(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Adds a reminder type to the cache.
     *
     * @param reminderType the reminder type to add
     * @return the reminder type
     */
    public ReminderType add(Entity reminderType) {
        ReminderType result = new ReminderType(reminderType, service);
        reminderTypes.put(reminderType.getObjectReference(), result);
        return result;
    }

    /**
     * Returns a reminder type given its corresponding <em>entity.reminderType</em> reference.
     *
     * @param ref the reminder type reference
     * @return the corresponding reminder type, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ReminderType get(IMObjectReference ref) {
        ReminderType result = null;
        if (ref != null) {
            result = reminderTypes.get(ref);
            if (result == null) {
                Entity entity = load(ref);
                if (entity != null) {
                    result = add(entity);
                }
            }
        }
        return result;
    }

    /**
     * Returns a reminder type given its corresponding <em>entity.reminderType</em>.
     *
     * @param entity the entity
     * @return the corresponding reminder type, or {@code null} if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ReminderType get(Entity entity) {
        return (entity != null) ? get(entity.getObjectReference()) : null;
    }

    /**
     * Helper to return an <em>entity.reminderType</em> given its reference.
     *
     * @param ref the reference
     * @return the corresponding entity or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Entity load(IMObjectReference ref) {
        return (Entity) service.get(ref);
    }

}
