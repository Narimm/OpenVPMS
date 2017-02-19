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

package org.openvpms.web.component.im.edit.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * An editor for <em>act.patientReminderItem*</em> acts.
 * <p/>
 * This initialises:
 * <ul>
 * <li>the reminder count; and</li>
 * <li>the due date to the next due date of the parent reminder</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class ReminderItemEditor extends AbstractActEditor {

    /**
     * Constructs a {@link ReminderItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public ReminderItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (act.isNew() && parent != null) {
            // set the due date to to reminder's next due date
            act.setActivityEndTime(parent.getActivityStartTime());
            ActBean bean = new ActBean(parent);

            // copy the reminder count
            getProperty("count").setValue(bean.getInt("reminderCount"));
        }
    }

    /**
     * Returns the reminder count.
     *
     * @return the reminder count
     */
    public int getCount() {
        return getProperty("count").getInt();
    }
}
