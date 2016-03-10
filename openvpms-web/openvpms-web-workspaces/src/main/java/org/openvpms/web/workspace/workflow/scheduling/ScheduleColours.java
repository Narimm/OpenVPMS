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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import nextapp.echo2.app.Color;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.echo.colour.ColourHelper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Cache for schedule colours.
 *
 * @author Tim Anderson
 */
public class ScheduleColours {

    /**
     * The colours, keyed on reference.
     */
    private final Map<IMObjectReference, String> colours;

    /**
     * Constructs a {@link ScheduleColours}.
     *
     * @param shortName the archetype to source colours from. Must have a 'colour' node
     */
    public ScheduleColours(String shortName) {
        colours = getColours(shortName);
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
     * Returns a map of object references and their corresponding 'colour' node
     * values for the specified short name.
     *
     * @param shortName the archetype short name
     * @return a map of the matching objects and their 'colour' node  values
     */
    private Map<IMObjectReference, String> getColours(String shortName) {
        Map<IMObjectReference, String> result = new HashMap<>();
        ArchetypeQuery query = new ArchetypeQuery(shortName, true, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        Iterator<IMObject> iter = new IMObjectQueryIterator<>(query);
        while (iter.hasNext()) {
            IMObject object = iter.next();
            IMObjectBean bean = new IMObjectBean(object);
            result.put(object.getObjectReference(), bean.getString("colour"));
        }
        return result;
    }
}
