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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.workflow;

import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditDialog;


/**
 * A task for editing account acts that disables the Apply button for
 * <em>POSTED<em> acts, as a workaround for OVPMS-733.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EditAccountActTask extends EditIMObjectTask {

    /**
     * Constructs a new <tt>EditAccountActTask</tt> to edit an object
     * in the {@link TaskContext}.
     *
     * @param shortName the short name of the object to edit
     */
    public EditAccountActTask(String shortName) {
        super(shortName);
    }

    /**
     * Constructs a new <code>EditIMObjectTask</code>, to edit an object
     * in the {@link TaskContext} or create and edit a new one.
     *
     * @param shortName the object short name
     * @param create    if <code>true</code>, create the object
     */
    public EditAccountActTask(String shortName, boolean create) {
        super(shortName, create, true);
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor the editor
     * @param skip   if <tt>true</tt>, editing may be skipped
     * @return a new edit dialog
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor, boolean skip) {
        return new ActEditDialog(editor, true, skip);
    }

}
