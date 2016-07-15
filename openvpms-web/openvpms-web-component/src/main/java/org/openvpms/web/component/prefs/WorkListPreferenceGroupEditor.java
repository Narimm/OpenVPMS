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

package org.openvpms.web.component.prefs;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.util.List;

/**
 * Editor for <em>entity.preferenceGroupWorkList</em>.
 *
 * @author Tim Anderson
 */
public class WorkListPreferenceGroupEditor extends AbstractSchedulePreferenceGroupEditor {

    /**
     * Constructs a {@link WorkListPreferenceGroupEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public WorkListPreferenceGroupEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Returns the schedule/work list views associated with a practice location.
     *
     * @param location the location
     * @return the views
     */
    @Override
    protected List<Entity> getViews(IMObjectBean location) {
        return location.getNodeTargetObjects("workListViews", Entity.class);
    }

    /**
     * Returns the schedules associated with a view.
     *
     * @param view the view
     * @return the schedules/work lists
     */
    protected List<Entity> getSchedules(Entity view) {
        IMObjectBean bean = new IMObjectBean(view);
        return bean.getNodeTargetObjects("workLists", Entity.class);
    }

}
