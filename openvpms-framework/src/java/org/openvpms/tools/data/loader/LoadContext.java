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

package org.openvpms.tools.data.loader;

import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LoadContext {


    private final IArchetypeService service;
    private final LoadCache cache;
    private final boolean validateOnly;

    public LoadContext(IArchetypeService service,
                       LoadCache cache, boolean validateOnly) {
        this.service = service;
        this.cache = cache;
        this.validateOnly = validateOnly;
    }

    public IArchetypeService getService() {
        return service;
    }

    public IMObjectReference getReference(String id) {
        return cache.getReference(id);
    }

    public LoadCache getCache() {
        return cache;
    }

    public boolean validateOnly() {
        return validateOnly;
    }

}
