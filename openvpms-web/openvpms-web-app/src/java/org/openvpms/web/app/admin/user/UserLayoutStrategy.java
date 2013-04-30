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

package org.openvpms.web.app.admin.user;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.ColourNodeLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Layout strategy that hides the 'password' node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UserLayoutStrategy extends ColourNodeLayoutStrategy {

    /**
     * Returns a node filter to filter nodes. This implementation filters
     * the "password" node when in view mode.
     *
     * @param object
     * @param context the context
     * @return a node filter to filter nodes
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        if (!context.isEdit()) {
            NodeFilter filter = new NamedNodeFilter("password");
            return getNodeFilter(context, filter);
        }
        return super.getNodeFilter(object, context);
    }
}
