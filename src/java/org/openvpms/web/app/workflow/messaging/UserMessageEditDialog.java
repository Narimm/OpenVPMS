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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.workflow.messaging;

import org.openvpms.archetype.rules.act.DefaultActCopyHandler;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class UserMessageEditDialog extends EditDialog {

    /**
     * Send button identifier.
     */
    public static final String SEND_ID = "send";

    /**
     * The editor button identifiers.
     */
    private static final String[] SEND_CANCEL = {SEND_ID, CANCEL_ID};

    /**
     * Constructs a {@code UserMessageEditDialog}.
     *
     * @param editor the editor
     */
    public UserMessageEditDialog(UserMessageEditor editor) {
        super(editor, SEND_CANCEL, false);
        setStyleName("UserMessageEditDialog");
    }

    /**
     * Returns the editor.
     *
     * @return the editor
     */
    @Override
    public UserMessageEditor getEditor() {
        return (UserMessageEditor) super.getEditor();
    }

    /**
     * Invoked when a button is pressed.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (SEND_ID.equals(button)) {
            if (send()) {
                close(SEND_ID);
            }
        } else {
            super.onButton(button);
        }
    }

    private boolean send() {
        boolean result = false;
        Validator validator = new Validator();
        if (getEditor().validate(validator)) {
            result = doSend();
        } else {
            ValidationHelper.showError(validator);
        }
        return result;
    }

    private boolean doSend() {
        UserMessageEditor editor = getEditor();
        Act template = (Act) editor.getObject();
        IMObjectCopier copier = new IMObjectCopier(new DefaultActCopyHandler());
        List<IMObject> toSave = new ArrayList<IMObject>();
        Date startTime = new Date();
        for (User user : editor.getToUsers()) {
            List<IMObject> objects = copier.apply(template);
            Act act = (Act) objects.get(0);
            act.setActivityStartTime(startTime);
            act.setStatus(WorkflowStatus.PENDING);
            ActBean bean = new ActBean(act);
            bean.setParticipant("participation.user", user);
            toSave.addAll(objects);
        }
        return SaveHelper.save(toSave);
    }
}
