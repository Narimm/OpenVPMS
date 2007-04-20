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

package org.openvpms.component.business.service.lookup;

import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;


/**
 * This is a helper class for the lookup service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-06-22 07:51:44Z $
 */
public class LookupServiceHelper {

    /**
     * A reference to the lookup service.
     */
    private static ILookupService lookupService;


    /**
     * Register the lookup service.
     *
     * @param service the service to register
     */
    public LookupServiceHelper(ILookupService service) {
        lookupService = service;
    }

    /**
     * Return a reference to the {@link ILookupService}, creating an
     * instance of {@link LookupService} if one is not set.
     *
     * @return the lookup service
     * @throws ArchetypeServiceException for any error
     */
    public static synchronized ILookupService getLookupService() {
        if (lookupService == null) {
            lookupService = new LookupService(
                    ArchetypeServiceHelper.getArchetypeService());
        }
        return lookupService;
    }
}
