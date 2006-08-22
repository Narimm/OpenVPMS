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
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.edit.act.AbstractActEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


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
        Property endTime = getProperty("endTime");
        if (endTime.getValue() == null) {
            endTime.setValue(Context.getInstance().getScheduleDate());
        }
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

}
