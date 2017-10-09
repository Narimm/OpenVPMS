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

package org.openvpms.web.component.prefs;

import echopointng.Separator;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.LabelFactory;

import java.util.List;

/**
 * Layout strategy for <em>entity.preferenceGroupScheduling</em>.
 *
 * @author Tim Anderson
 */
public class SchedulingPreferenceLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Lays out components in a grid.
     *
     * @param object     the object to lay out
     * @param properties the properties
     * @param context    the layout context
     * @param columns    the no. of columns to use
     */
    @Override
    protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context, int columns) {
        String[] names = {"dates", "show"};
        List<Property> common = ArchetypeNodes.exclude(properties, names);
        List<Property> multiday = ArchetypeNodes.include(properties, names);
        ComponentGrid grid = new ComponentGrid();
        grid.add(createComponentSet(object, common, context), columns);
        grid.add(new ComponentState(new Separator()), columns);
        grid.add(LabelFactory.create("workflow.scheduling.multiday"));
        grid.add(createComponentSet(object, multiday, context), columns);
        return grid;
    }
}
