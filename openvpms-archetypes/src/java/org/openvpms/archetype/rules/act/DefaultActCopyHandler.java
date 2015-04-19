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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.Participation;


/**
 * Default act copy handler.
 * <p/>
 * This copies all {@link Act Acts}, {@link ActRelationship ActRelationships}
 * and {@link Participation Participations}, and reference all other objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultActCopyHandler extends ActCopyHandler {

    /**
     * Creates a new <tt>DefaultActCopyHandler</tt>.
     */
    public DefaultActCopyHandler() {
        super();
    }

    /**
     * Creates a new <tt>DefaultActCopyHandler</tt>.
     *
     * @param shortNameMap a list of short name pairs, indicating the short name
     *                     to map from and to. If the 'to' short name is null,
     *                     then any instance of the 'from' is ignored
     */
    public DefaultActCopyHandler(String[][] shortNameMap) {
        super(shortNameMap);
    }
}
