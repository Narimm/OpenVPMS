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

package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelperException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Lookup name helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupNameHelper {

    /**
     * Helper to return a lookup name.
     *
     * @param descriptor the node descriptor
     * @param context    the context object
     * @return the lookup name, or <tt>null</tt> if it can't be found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static String getLookupName(NodeDescriptor descriptor,
                                       IMObject context) {
        if (descriptor != null) {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            return LookupHelper.getName(service, descriptor, context);
        }
        return null;
    }

    /**
     * Returns a list of lookups for the specified archetype short name and node
     * name.
     *
     * @param shortName the archetype short name
     * @param node      the node name
     * @return a map of lookup codes to lookup names
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Map<String, String> getLookupNames(String shortName,
                                                     String node) {
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(shortName);
        if (archetype != null) {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
            if (descriptor != null) {
                return getLookupNames(descriptor);
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Returns a map of lookup codes to lookup names for the specified
     * {@link NodeDescriptor}.
     *
     * @param descriptor the node descriptor
     * @return a map of lookup codes to lookup names
     * @throws ArchetypeServiceException for any archetype service error
     * @throws LookupHelperException     if the lookup is incorrectly specified
     */
    public static Map<String, String> getLookupNames(
            NodeDescriptor descriptor) {
        Map<String, String> result = new HashMap<String, String>();
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        List<Lookup> lookups = LookupHelper.get(service, descriptor);
        for (Lookup lookup : lookups) {
            if (lookup.isActive()) {
                result.put(lookup.getCode(), lookup.getName());
            }
        }
        return result;
    }

}
