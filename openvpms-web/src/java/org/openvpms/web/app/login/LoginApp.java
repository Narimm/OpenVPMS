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

package org.openvpms.web.app.login;

import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Window;
import org.openvpms.web.component.util.ContentPaneFactory;
import org.openvpms.web.resource.util.Styles;
import org.openvpms.web.system.SpringApplicationInstance;


/**
 * Login application.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LoginApp extends SpringApplicationInstance {

    /**
     * Invoked to initialize the application, returning the default window. The
     * returned window must be visible.
     *
     * @return the default window of the application
     */
    public Window init() {
        setStyleSheet(Styles.DEFAULT_STYLE_SHEET);
        Window window = new Window();
        window.setTitle("OpenVPMS");
        ContentPane pane = ContentPaneFactory.create("LoginPane");
        pane.add(new LoginDialog());
        window.setContent(pane);
        return window;
    }
}
