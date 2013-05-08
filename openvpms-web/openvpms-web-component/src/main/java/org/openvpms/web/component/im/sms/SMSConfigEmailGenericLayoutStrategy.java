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

package org.openvpms.web.component.im.sms;

import nextapp.echo2.app.SplitPane;
import org.openvpms.web.component.util.SplitPaneFactory;

/**
 * Layout strategy for <em>entity.SMSConfigEmailGeneric</em>.
 * <p/>
 * This includes more space to render the editor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SMSConfigEmailGenericLayoutStrategy extends SMSConfigEmailLayoutStrategy {

    /**
     * Creates a split pane to render the component and sampler in.
     *
     * @return a new split pane
     */
    protected SplitPane createSplitPane() {
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "SMSConfigEmailGeneric");
    }
}