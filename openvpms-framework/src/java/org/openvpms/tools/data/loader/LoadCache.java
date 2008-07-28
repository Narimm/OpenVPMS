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

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import static org.openvpms.tools.data.loader.ArchetypeDataLoaderException.ErrorCode.NullReference;
import static org.openvpms.tools.data.loader.LoadState.ID_PREFIX;

import java.util.HashMap;
import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LoadCache {

    private Map<String, IMObjectReference> refs
            = new HashMap<String, IMObjectReference>();
    private Map<IMObjectReference, String> ids
            = new HashMap<IMObjectReference, String>();

    private ReferenceMap objects = new ReferenceMap(ReferenceMap.HARD,
                                                    ReferenceMap.WEAK);

    public IMObject get(IMObjectReference reference) {
        IMObject result = (IMObject) objects.get(reference);
        if (result == null && objects.containsKey(reference)) {
            objects.remove(reference); // value has been garbage collected
        }
        return result;
    }

    public void add(IMObject object, String id) {
        IMObjectReference ref = object.getObjectReference();
        if (id != null) {
            id = stripPrefix(id);
            ids.put(ref, id);
            refs.put(id, ref);
        }
        objects.put(object.getObjectReference(), object);
    }

    public void add(String id, IMObjectReference ref) {
        id = stripPrefix(id);
        ids.put(ref, id);
        refs.put(id, ref);
    }

    public Map<String, IMObjectReference> getReferences() {
        return refs;
    }

    public void update(IMObjectReference reference) {
        String id = ids.get(reference);
        if (id != null) {
            ids.put(reference, id);
            refs.put(id, reference);
        }
    }

    public IMObjectReference getReference(String id) {
        return refs.get(stripPrefix(id));
    }

    public String getId(IMObjectReference ref) {
        return ids.get(ref);
    }


    private String stripPrefix(String id) {
        if (id.startsWith(ID_PREFIX)) {
            id = id.substring(ID_PREFIX.length());
            if (StringUtils.isEmpty(id)) {
                throw new ArchetypeDataLoaderException(NullReference);
            }
        }
        return id;
    }

}
