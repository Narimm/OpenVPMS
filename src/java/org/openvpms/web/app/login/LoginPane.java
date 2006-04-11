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


/**
 * Login pane. This simply pops up an {@link LoginDialog}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $Revision$ $Date$
 */
public class LoginPane extends ContentPane {

    /**
     * Style name.
     */
    private static final String STYLE = "LoginPane";


    /**
     * Construct a new <code>LoginPane</code>.
     */
    public LoginPane() {
        setStyleName(STYLE);
    }

    /**
     * Initialise this.
     */
    @Override
    public void init() {
        super.init();
        new LoginDialog();
    }

}
