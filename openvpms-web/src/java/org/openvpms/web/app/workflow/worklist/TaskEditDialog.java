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
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.resource.util.Messages;


/**
 * Edit dialog for task acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskEditDialog extends EditDialog {

    /**
     * Construct a new <code>AppointmentEditDialog</code>.
     *
     * @param editor  the editor
     * @param context the layout context
     */
    public TaskEditDialog(IMObjectEditor editor, LayoutContext context) {
        super(editor, context);
    }

    /**
     * Save the current object.
     */
    @Override
    protected void onApply() {
        if (checkMaxSlots()) {
            super.onApply();
        }
    }

    /**
     * Save the current object, and close the editor.
     */
    @Override
    protected void onOK() {
        if (checkMaxSlots()) {
            super.onOK();
        }
    }

    /**
     * Determines if the appointment overlaps an existing appointment.
     * If so, and double scheduling is allowed, a confirmation dialog is shown
     * prompting to save or continue editing. If double scheduling is not
     * allowed, an error dialog is shown and no save is performed.
     *
     * @return <code>true</code> if there are less than maxSlots tasks, otherwise
     *         <code>false</code>
     */
    private boolean checkMaxSlots() {
        final IMObjectEditor editor = getEditor();
        IMObject object = editor.getObject();
        boolean result = true;
        if (editor.isValid()) {
            Act act = (Act) object;
            if (tooManyTasks(act)) {
                String title = Messages.get(
                        "workflow.worklist.toomanytasks.title");
                String message = Messages.get(
                        "workflow.worklist.toomanytasks.message");
                ErrorDialog.show(title, message);
                result = false;
            }
        }
        return result;
    }

    /**
     * Determines there are too many outstanding tasks.
     *
     * @return <code>true</code> if there are too many outstanding tasks;
     *         otherwise <code>false</code>
     */
    private boolean tooManyTasks(Act act) {
        boolean result = false;
        ActBean actBean = new ActBean(act);
        Party workList = (Party) actBean.getParticipant(
                "participation.worklist");
        if (workList != null) {
            IMObjectBean bean = new IMObjectBean(workList);
            int maxSlots = bean.getInt("maxSlots");
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            ArchetypeQuery query
                    = new ArchetypeQuery("act.customerTask", false, true);
            query.add(new ParticipantConstraint("worklist",
                                                "participation.worklist",
                                                workList));
            query.add(new NodeConstraint("uid", RelationalOp.NE, act.getUid()));
            query.add(
                    new NodeConstraint("status", RelationalOp.NE, "Cancelled"));
            query.setFirstRow(0);
            query.setNumOfRows(1);
            IPage<IMObject> page = service.get(query);
            if (page.getTotalNumOfRows() >= maxSlots) {
                result = true;
            }
        }
        return result;
    }

}
