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

package org.openvpms.web.workspace.workflow.scheduling;

import nextapp.echo2.app.Color;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.AbstractMonitoringIMObjectCache;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.echo.colour.ColourHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache for schedule colours.
 * <p/>
 * This caches colours for users, appointment types, calendar block types, and task types.
 *
 * @author Tim Anderson
 */
public class ScheduleColours extends AbstractMonitoringIMObjectCache<Entity> {

    /**
     * The colours, keyed on reference.
     */
    private final Map<IMObjectReference, String> colours
            = Collections.synchronizedMap(new HashMap<IMObjectReference, String>());

    /**
     * The archetypes to cache colours for. Must have a 'colour' node.
     */
    private static final String[] ARCHETYPES = {UserArchetypes.USER, ScheduleArchetypes.APPOINTMENT_TYPE,
                                                ScheduleArchetypes.CALENDAR_BLOCK_TYPE, ScheduleArchetypes.TASK_TYPE};

    /**
     * Constructs an {@link AbstractMonitoringIMObjectCache}.
     *
     * @param service the archetype service
     */
    public ScheduleColours(IArchetypeService service) {
        super(service, ARCHETYPES, Entity.class);
        load();
    }

    /**
     * Returns the colour for a particular reference.
     *
     * @param reference the reference
     * @return the colour, or {@code null} if none is found
     */
    public Color getColour(IMObjectReference reference) {
        return ColourHelper.getColor(colours.get(reference));
    }

    /**
     * Adds an object to the cache.
     *
     * @param object the object to add
     */
    @Override
    protected void addObject(Entity object) {
        IMObjectBean bean = new IMObjectBean(object, getService());
        colours.put(object.getObjectReference(), bean.getString("colour"));
    }

    /**
     * Removes an object.
     *
     * @param object the object to remove
     */
    @Override
    protected void removeObject(Entity object) {
        colours.remove(object.getObjectReference());
    }

}
