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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.TimePropertyTransformer;
import org.openvpms.web.component.util.TimeFieldFactory;


/**
 * Layout strategy for <em>party.organisationSchedule</em> archetypes.
 * <p/>
 * This displays the <em>startTime</em> and <em>endTime</em> nodes as time
 * fields.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ScheduleLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <code>property</code>
     */
    @Override
    protected ComponentState createComponent(Property property, IMObject parent,
                                             LayoutContext context) {
        String name = property.getName();
        if ("startTime".equals(name) || "endTime".equals(name)) {
            Component time = TimeFieldFactory.create(property);
            if (!context.isEdit()) {
                time.setEnabled(false);
            } else {
                property.setTransformer(new TimePropertyTransformer(null));
            }
            return new ComponentState(time, property);
        }
        return super.createComponent(property, parent, context);
    }
}
