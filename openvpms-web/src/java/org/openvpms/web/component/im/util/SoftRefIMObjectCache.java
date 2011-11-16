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

package org.openvpms.web.component.im.util;

import org.apache.commons.collections.map.ReferenceMap;
import org.openvpms.web.system.ServiceHelper;


/**
 * An {@link IMObjectCache} that allows objects to be reclaimed by the garbage collector if they are not referenced by
 * any other object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SoftRefIMObjectCache extends AbstractIMObjectCache {

    /**
     * Constructs a <tt>SoftRefIMObjectCache</tt>.
     */
    @SuppressWarnings("unchecked")
    public SoftRefIMObjectCache() {
        super(new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT), ServiceHelper.getArchetypeService());
    }

}
