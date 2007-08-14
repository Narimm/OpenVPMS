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

package org.openvpms.web.component.im.table.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Default table model for displaying {@link Act}s.
 * Any "items" nodes are filtered.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultActTableModel extends AbstractActTableModel {

    /**
     * Construct a <tt>DefaultActTableModel</tt>.
     *
     * @param shortNames the act archetype short names
     */
    public DefaultActTableModel(String[] shortNames) {
        this(shortNames, null);
    }

    /**
     * Constructs a <tt>DefaultActTableModel</tt>.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context. May be <tt>null</tt>
     */
    public DefaultActTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

}
