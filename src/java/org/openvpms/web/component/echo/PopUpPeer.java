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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.echo;

import nextapp.echo2.webrender.Service;
import nextapp.echo2.webrender.WebRenderServlet;
import nextapp.echo2.webrender.service.JavaScriptService;


/**
 * A peer to register a modified popup.js in order to change the sizing of popups.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class PopUpPeer extends echopointng.ui.syncpeer.PopUpPeer {

    public static final Service POPUP_SERVICE = JavaScriptService.forResource("EPNG.PopUp",
                                                                              "/org/openvpms/web/resource/js/popup.js");

   static {
        // NOTE: as this extends EPNG PopUpPeer, the EPNG popup.js script will always be registered prior to
        // this due to static construction order. It can therefore safely be removed and replaced.
        WebRenderServlet.getServiceRegistry().remove(POPUP_SERVICE);
        WebRenderServlet.getServiceRegistry().add(POPUP_SERVICE);
    }
}
