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
 * A peer to register a fixed TextComponent.js as a workaround for the echo2 bug referred to in
 * http://jira.openvpms.org/jira/browse/OVPMS-1017.
 * <p/>
 * This simply replaces the broken TextComponent.js script with a corrected one. It will therefore affect TextAreaPeer,
 * which uses the same script.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class TextFieldPeer extends nextapp.echo2.webcontainer.syncpeer.TextFieldPeer {

    /**
     * Service to provide supporting JavaScript library.
     */
    public static final Service TEXT_COMPONENT_SERVICE = JavaScriptService.forResource(
            "Echo.TextComponent", "/org/openvpms/web/resource/js/TextComponent.js");

    static {
        // NOTE: as this extends TextFieldPeer, the broken TextComponent.js script will always be registered prior to
        // this due to static construction order. It can therefore safely be removed and replaced.
        WebRenderServlet.getServiceRegistry().remove(TEXT_COMPONENT_SERVICE);
        WebRenderServlet.getServiceRegistry().add(TEXT_COMPONENT_SERVICE);
    }
}
