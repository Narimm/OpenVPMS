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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * The load context.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LoadContext {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The load cache.
     */
    private final LoadCache cache;

    /**
     * If <tt>true</tt>, only validate, don't save.
     */
    private final boolean validateOnly;


    /**
     * Creates a new <tt>LoadContext</tt>.
     *
     * @param service      the archetype service
     * @param cache        the load cache
     * @param validateOnly if <tt>true</tt>, only validate, don't save.
     */
    public LoadContext(IArchetypeService service, LoadCache cache,
                       boolean validateOnly) {
        this.service = service;
        this.cache = cache;
        this.validateOnly = validateOnly;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    public IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the object reference for the given identifier.
     *
     * @param id the identifier
     * @return the corresponding reference, or <tt>null</tt> if none is found
     */
    public IMObjectReference getReference(String id) {
        return cache.getReference(id);
    }

    /**
     * Returns an object for the given identifier.
     *
     * @param id the object identifier
     * @return the corresponding object, or <tt>null</tt> if none is found
     */
    public IMObject getObject(String id) {
        IMObjectReference ref = getReference(id);
        IMObject object = null;
        if (ref != null) {
            object = cache.get(ref);
            if (object == null) {
                if (validateOnly) {
                    object = service.create(ref.getArchetypeId());
                } else if (!ref.isNew()) {
                    object = service.get(ref);
                }
            }
        }
        return object;
    }

    /**
     * Returns the load cache.
     *
     * @return the load cache
     */
    public LoadCache getCache() {
        return cache;
    }

    /**
     * Determines if only validation should be performed.
     *
     * @return <tt>true</tt> if only validation should be performed; otherwise
     *         returns <tt>false</tt>, indicating that both validation and save
     *         should be performed.
     */
    public boolean validateOnly() {
        return validateOnly;
    }

}
