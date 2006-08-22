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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.worklist;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.util.Date;


/**
 * An editor for <em>act.customerTask</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskActEditor extends AbstractActEditor {

    /**
     * Construct a new <code>TaskActEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    public TaskActEditor(Act act, IMObject parent,
                         LayoutContext context) {
        super(act, parent, context);
        initParticipant("worklist", Context.getInstance().getWorkList());

        Property startTime = getProperty("startTime");
        if (startTime.getValue() == null) {
            startTime.setValue(Context.getInstance().getScheduleDate());
        }
        startTime.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStartTimeChanged();
            }
        });

        Property endTime = getProperty("endTime");
        if (endTime.getValue() == null) {
            endTime.setValue(Context.getInstance().getScheduleDate());
        }
        endTime.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onEndTimeChanged();
            }
        });

        getProperty("status").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onStatusChanged();
            }
        });
    }

    /**
     * Invoked when layout has completed. All editors have been created.
     */
    @Override
    protected void onLayoutCompleted() {
        Party workList = (Party) getParticipant("worklist");
        TaskTypeParticipationEditor editor = getTaskTypeEditor();
        editor.setWorkList(workList);
    }

    /**
     * Returns the task type editor.
     *
     * @return the task type editor
     */
    private TaskTypeParticipationEditor getTaskTypeEditor() {
        return (TaskTypeParticipationEditor) getEditor("taskType");
    }

    /**
     * Invoked when the status changes. Sets the end time to today if the
     * status is 'Completed' or 'Cancelled'.
     */
    private void onStatusChanged() {
        Property status = getProperty("status");
        String value = (String) status.getValue();
        if ("Completed".equals(value) || "Cancelled".equals(value)) {
            Property endTime = getProperty("endTime");
            endTime.setValue(new Date());
        }
    }

    /**
     * Invoked when the start time changes. Sets the value to end time if
     * start time > end time.
     */
    private void onStartTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (start.compareTo(end) > 0) {
                getProperty("startTime").setValue(end);
            }
        }
    }

    /**
     * Invoked when the end time changes. Sets the value to start time if
     * end time < start time.
     */
    private void onEndTimeChanged() {
        Date start = getStartTime();
        Date end = getEndTime();
        if (start != null && end != null) {
            if (end.compareTo(start) < 0) {
                getProperty("endTime").setValue(start);
            }
        }
    }

    /**
     * Returns the start time.
     *
     * @return the start time. May be <code>null</code>
     */
    private Date getStartTime() {
        Property property = getProperty("startTime");
        Object value = property.getValue();
        return (value instanceof Date) ? (Date) value : null;
    }

    /**
     * Returns the end time.
     *
     * @return the end time. May be <code>null</code>
     */
    private Date getEndTime() {
        Property property = getProperty("endTime");
        Object value = property.getValue();
        return (value instanceof Date) ? (Date) value : null;
    }

}
