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

package org.openvpms.web.component.subsystem;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.util.Archetypes;


/**
 * Default implementation of the {@link CRUDWindow} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultCRUDWindow<T extends IMObject>
        extends AbstractViewCRUDWindow<T> {

    /**
     * Create a new <tt>DefaultCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create.
     */
    public DefaultCRUDWindow(Archetypes<T> archetypes) {
        super(archetypes);
    }
}
