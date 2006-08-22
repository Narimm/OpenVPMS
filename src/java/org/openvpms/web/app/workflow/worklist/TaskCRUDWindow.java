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

import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.app.workflow.WorkflowCRUDWindow;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Task CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskCRUDWindow extends WorkflowCRUDWindow {

    /**
     * Constructs a new <code>TaskCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public TaskCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor  the editor
     * @param context the layout context
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor,
                                          LayoutContext context) {
        return new TaskEditDialog(editor, context);
    }

}
