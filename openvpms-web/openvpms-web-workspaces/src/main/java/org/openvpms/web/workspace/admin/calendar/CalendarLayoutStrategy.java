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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.calendar;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.SplitPaneFactory;

import java.util.List;


/**
 * Layout strategy for <em>entity.calendar*</em> calendars.
 *
 * @author Tim Anderson
 */
public class CalendarLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The calendar event viewer.
     */
    private final CalendarEventViewer viewer;

    /**
     * Constructs a {@link CalendarLayoutStrategy}.
     *
     * @param viewer the calendar event viewer
     */
    public CalendarLayoutStrategy(CalendarEventViewer viewer) {
        this.viewer = viewer;
    }

    /**
     * Lay out out the object.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component
     */
    @Override
    protected Component doLayout(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        SplitPane container = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM, "Calendar");
        doLayout(object, properties, parent, container, context);
        return container;
    }

    /**
     * Lays out each child component in a tabbed pane.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doComplexLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                   LayoutContext context) {
        container.add(viewer.getComponent());
    }
}
