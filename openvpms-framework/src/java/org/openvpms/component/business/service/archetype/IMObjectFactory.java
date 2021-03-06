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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * A factory for {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface IMObjectFactory {

    /**
     * Creates a domain object given an archetype short name.
     *
     * @param shortName the archetype short name
     * @return a new object, or <tt>null</tt> if there is no corresponding archetype descriptor for <tt>shortName</tt>
     * @throws ArchetypeServiceException if the object can't be created
     */
    public IMObject create(String shortName);

    /**
     * Creates a domain object given an {@link ArchetypeId}.
     *
     * @param id the archetype identifier
     * @return a new object, or <tt>null</tt> if there is no corresponding archetype descriptor for <tt>shortName</tt>
     * @throws ArchetypeServiceException if the object can't be created
     */
    public IMObject create(ArchetypeId id);

}
