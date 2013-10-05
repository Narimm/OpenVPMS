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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.otc;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.workspace.customer.charge.ChargeEditorQueue;
import org.openvpms.web.workspace.workflow.EditorQueueHandle;

/**
 * Tests implementation of the {@link OTCChargeTask}.
 *
 * @author Tim Anderson
 */
class TestOTCChargeTask extends OTCChargeTask implements EditorQueueHandle {

    /**
     * The popup dialog manager.
     */
    private ChargeEditorQueue queue = new ChargeEditorQueue();


    /**
     * Returns the popup dialog manager.
     *
     * @return the popup dialog manager
     */
    @Override
    public ChargeEditorQueue getEditorQueue() {
        return queue;
    }

    /**
     * Creates the charge editor.
     *
     * @param object  the object to edit
     * @param context the task context
     * @return a new charge editor
     */
    @Override
    protected IMObjectEditor createEditor(IMObject object, TaskContext context) {
        LayoutContext layout = new DefaultLayoutContext(true, context, context.getHelpContext());
        return new TestOTCChargeEditor(queue, (FinancialAct) object, null, layout);
    }
}
