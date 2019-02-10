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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.workspace.admin.calendar.CalendarEventEditor;

import java.util.Date;

/**
 * Editor for roster events.
 * <p/>
 * This requires the location to be set.
 *
 * @author Tim Anderson
 */
public class RosterEventEditor extends CalendarEventEditor {

    /**
     * Constructs a {@link RosterEventEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public RosterEventEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, false, context);
        if (act.isNew()) {
            getStartTimeEditor().getTimeField().setText("09:00");
            initParticipant("location", context.getContext().getLocation());
        }
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return {@code null}
     */
    @Override
    public IMObjectEditor newInstance() {
        return new RosterEventEditor(reload(getObject()), getParent(), getLayoutContext());
    }

    /**
     * Sets the user.
     *
     * @param user the user. May be {@code null}
     */
    public void setUser(User user) {
        setParticipant("user", user);
    }

    /**
     * Returns the user.
     *
     * @return the user. May be {@code null}
     */
    public User getUser() {
        return (User) getParticipant("user");
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        Party location = (Party) getParticipant("location");
        AreaParticipationEditor area = getAreaEditor();
        area.setLocation(location);
    }

    /**
     * Calculates the default start time of an event, using the supplied date.
     *
     * @param date the start date
     * @return the start time
     */
    @Override
    protected Date getDefaultStartTime(Date date) {
        return DateRules.getDate(date, 8, DateUnits.HOURS);
    }

    /**
     * Calculates the end time.
     */
    @Override
    protected void calculateEndTime() {
        Date start = getStartTime();
        if (start != null) {
            Date end = DateRules.getDate(start, 8, DateUnits.HOURS);
            setEndTime(end);
        }
    }

    /**
     * Returns the area participation editor.
     *
     * @return the area participation editor
     */
    private AreaParticipationEditor getAreaEditor() {
        return (AreaParticipationEditor) getParticipationEditor("schedule", false);
    }

}
