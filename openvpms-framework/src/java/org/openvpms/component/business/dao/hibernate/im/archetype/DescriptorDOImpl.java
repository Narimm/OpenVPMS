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

package org.openvpms.component.business.dao.hibernate.im.archetype;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;

/**
 * All the descriptor classes inherit from this base class, which provides
 * support for identity, hibernate and serialization
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-24 09:20:29 +1000 (Mon, 24 Apr 2006) $
 */
public abstract class DescriptorDOImpl extends IMObjectDOImpl
        implements DescriptorDO {


    /**
     * Default constructor.
     */
    public DescriptorDOImpl() {
    }
}
