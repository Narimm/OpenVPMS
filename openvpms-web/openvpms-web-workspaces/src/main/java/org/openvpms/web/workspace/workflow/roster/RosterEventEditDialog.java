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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.archetype.rules.workflow.roster.RosterService;
import org.openvpms.component.model.user.User;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.AbstractCalendarEventEditDialog;

import java.util.List;

/**
 * An edit dialog for <em>act.rosterEvent</em> acts.
 *
 * @author Tim Anderson
 */
public class RosterEventEditDialog extends AbstractCalendarEventEditDialog {

    /**
     * The roster service.
     */
    private final RosterService service;

    /**
     * The user.
     */
    private User user;

    /**
     * Constructs a {@link RosterEventEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public RosterEventEditDialog(RosterEventEditor editor, Context context) {
        super(editor, context);
        service = ServiceHelper.getBean(RosterService.class);
    }

    /**
     * Returns the editor.
     *
     * @return the editor, or {@code null} if none has been set
     */
    @Override
    public RosterEventEditor getEditor() {
        return (RosterEventEditor) super.getEditor();
    }

    /**
     * Caches the event start and end times, and user.
     */
    protected void getState() {
        super.getState();
        this.user = getEditor().getUser();
    }

    /**
     * Determines if the event can be saved without checking the event times.
     *
     * @return {@code true} if the event can be saved
     */
    @Override
    protected boolean noTimeCheckRequired() {
        boolean result = super.noTimeCheckRequired();
        if (result) {
            User newUser = getEditor().getUser();
            result = ObjectUtils.equals(newUser, user);
        }
        return result;
    }

    /**
     * Verifies that the event or events in the series don't overlap existing events.
     *
     * @param times the event and series times
     * @param close determines if the dialog should close if the user OKs overlapping appointments
     * @return {@code true} if the event and series is valid, otherwise {@code false}
     */
    @Override
    protected boolean checkEventTimes(List<Times> times, final boolean close) {
        boolean result = true;
        RosterEventEditor editor = getEditor();
        User user = editor.getUser();
        if (user != null) {
            List<Times> overlaps = service.getOverlappingEvents(times, user, 1);
            if (overlaps != null) {
                result = false;
                displayOverlapError(overlaps.get(0));
            }
        }
        return result;
    }

}
