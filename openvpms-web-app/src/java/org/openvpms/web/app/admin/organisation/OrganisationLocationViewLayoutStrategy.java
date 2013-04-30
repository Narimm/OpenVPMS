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

package org.openvpms.web.app.admin.organisation;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Layout strategy for <em>party.organisationLocation<em> that masks the "mailPassword" node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class OrganisationLocationViewLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The mail password node name.
     */
    private static final String MAIL_PASSWORD = "mailPassword";

    /**
     * Returns a node filter to filter nodes. This implementation filters the "mailPassword" node when in view mode.
     *
     * @param context the context
     * @return a node filter to filter nodes
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        NodeFilter filter = new NamedNodeFilter(MAIL_PASSWORD);
        return getNodeFilter(context, filter);
    }

}
