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

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.app.Context;

import java.util.List;

/**
 * Edit dialog for calendar blocks.
 *
 * @author Tim Anderson
 */
public class CalendarBlockEditDialog extends CalendarEventEditDialog {

    /**
     * Constructs a {@link CalendarBlockEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public CalendarBlockEditDialog(CalendarEventEditor editor, Context context) {
        super(editor, context);
    }

    /**
     * Verifies that the event or events in the series don't overlap existing events.
     * <p/>
     * If they do, and double scheduling is allowed, a confirmation dialog is shown prompting to save or continue
     * editing.
     * <br/>
     * If double scheduling is not allowed, an error dialog is shown and no save is performed.
     *
     * @param times the event and series times
     * @param close determines if the dialog should close if the user OKs overlapping appointments
     * @return {@code true} if the event and series is valid, otherwise {@code false}
     */
    protected boolean checkEventTimes(List<Times> times, final boolean close) {
        boolean result = true;
        CalendarEventEditor editor = getEditor();
        Entity schedule = editor.getSchedule();
        Times overlap = getService().getOverlappingEvent(times, schedule);
        if (overlap != null) {
            displayOverlapError(overlap);
            result = false;
        }
        return result;
    }

}
