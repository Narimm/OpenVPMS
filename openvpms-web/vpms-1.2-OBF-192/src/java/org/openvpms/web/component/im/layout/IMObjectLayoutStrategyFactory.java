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

package org.openvpms.web.component.im.layout;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Factory for {@link IMObjectLayoutStrategy} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IMObjectLayoutStrategyFactory {

    /**
     * Creates a new layout strategy for an object.
     *
     * @param object the object to create the layout strategy for
     * @return a new layout strategy
     */
    IMObjectLayoutStrategy create(IMObject object);

    /**
     * Creates a new layout strategy for an object.
     *
     * @param object the object to create the layout strategy for
     * @param parent the parent object. May be <code>null</code>
     */
    IMObjectLayoutStrategy create(IMObject object, IMObject parent);
}
