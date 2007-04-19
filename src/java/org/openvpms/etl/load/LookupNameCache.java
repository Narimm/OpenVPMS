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

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;

import java.util.HashMap;
import java.util.Map;


/**
 * A cache of lookup names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupNameCache {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * A map of lookup node descriptors to their corresponding lookup codes
     * and names.
     */
    private Map<NodeDescriptor, Map<String, String>> lookupsByDescriptor
            = new HashMap<NodeDescriptor, Map<String, String>>();

    /**
     * Constructs a new <tt>LookupNameCache</tt>.
     *
     * @param service the archetype service
     */
    public LookupNameCache(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns a lookup name for the specified lookup descriptor and context
     * object.
     *
     * @param descriptor the lookup descriptor
     * @param context    the context object
     * @return the corresponding name or <tt>null</tt> if none is found
     */
    public String get(NodeDescriptor descriptor, IMObject context) {
        String name = null;
        String code = (String) descriptor.getValue(context);
        if (code != null) {
            Map<String, String> pairs = lookupsByDescriptor.get(descriptor);
            if (pairs == null) {
                pairs = new HashMap<String, String>();
                lookupsByDescriptor.put(descriptor, pairs);
            }
            name = pairs.get(code);
            if (name == null) {
                name = LookupHelper.getName(service, descriptor, context);
                if (name != null) {
                    pairs.put(code, name);
                }
            }
        }
        return name;
    }

}
