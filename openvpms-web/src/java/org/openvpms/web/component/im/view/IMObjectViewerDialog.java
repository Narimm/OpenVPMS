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

package org.openvpms.web.component.im.view;

import org.openvpms.web.component.dialog.PopupDialog;


/**
 * Displays an {@link IMObjectViewer} in popup window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectViewerDialog extends PopupDialog {

    /**
     * Dialog style name.
     */
    private static final String STYLE = "IMObjectViewerDialog";


    /**
     * Construct a new <code>IMObjectViewerDialog</code>.
     *
     * @param browser the browser to display
     */
    public IMObjectViewerDialog(IMObjectViewer browser) {
        super(browser.getTitle(), STYLE, OK);
        setModal(true);
        getLayout().add(browser.getComponent());
        show();
    }
}
