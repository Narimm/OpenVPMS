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

package org.openvpms.web.component.dialog;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Window;
import nextapp.echo2.app.WindowPane;


/**
 * Dialog manager.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DialogManager {

    /**
     * Z-index allocator.
     */
    private static volatile int zIndex;


    /**
     * Shows a dialog.
     *
     * @param dialog the dialog to show
     */
    public static void show(WindowPane dialog) {
        Window root = ApplicationInstance.getActive().getDefaultWindow();
        dialog.setZIndex(++zIndex);
        root.getContent().add(dialog);
    }

}
