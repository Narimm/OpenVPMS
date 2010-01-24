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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.apache.commons.collections.map.ReferenceMap;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;


/**
 * Cache of {@link IMObject}s. Objects may be reclaimed if memory is low.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectCache {

    /**
     * The cached objects.
     */
    private final ReferenceMap objects = new ReferenceMap();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>IMObjectCache</tt>.
     */
    public IMObjectCache() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>IMObjectCache</tt>.
     *
     * @param service the archetype service
     */
    public IMObjectCache(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns an object given its corresponding reference.
     *
     * @param ref the object reference
     * @return the corresponding object, or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObject get(IMObjectReference ref) {
        IMObject result = null;
        if (ref != null) {
            result = (IMObject) objects.get(ref);
            if (result == null) {
                result = service.get(ref);
                if (result != null) {
                    objects.put(ref, result);
                }
            }
        }
        return result;
    }

}
