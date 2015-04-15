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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import static org.openvpms.tools.data.loader.ArchetypeDataLoaderException.ErrorCode.NullReference;
import static org.openvpms.tools.data.loader.LoadState.ID_PREFIX;

import java.util.HashMap;
import java.util.Map;


/**
 * A cache of loaded objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LoadCache {

    /**
     * A map of identifiers to their corresponding object references.
     */
    private Map<String, IMObjectReference> refs
            = new HashMap<String, IMObjectReference>();

    /**
     * A map of references to their corresponding identifiers.
     */
    private Map<IMObjectReference, String> ids
            = new HashMap<IMObjectReference, String>();

    /**
     * A map of {@link IMObject}s, keyed on {@link IMObjectReference}.
     * This uses weak references so objects may be garbage collected as
     * required.
     */
    private ReferenceMap objects = new ReferenceMap(ReferenceMap.HARD,
                                                    ReferenceMap.WEAK);


    /**
     * Returns an object given its reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none is found
     */
    public IMObject get(IMObjectReference reference) {
        IMObject result = (IMObject) objects.get(reference);
        if (result == null && objects.containsKey(reference)) {
            objects.remove(reference); // value has been garbage collected
        }
        return result;
    }

    /**
     * Adds an object.
     *
     * @param object the object to add
     * @param id     the object identifier. May be <tt>null</tt>
     */
    public void add(IMObject object, String id) {
        IMObjectReference ref = object.getObjectReference();
        if (id != null) {
            id = stripPrefix(id);
            ids.put(ref, id);
            refs.put(id, ref);
        }
        objects.put(object.getObjectReference(), object);
    }

    /**
     * Returns the object references, keyed on identifier.
     *
     * @return the object references
     */
    public Map<String, IMObjectReference> getReferences() {
        return refs;
    }

    /**
     * Updates a reference. This is used to update a transient reference with
     * its saved version.
     *
     * @param reference the updated reference
     */
    public void update(IMObjectReference reference) {
        String id = ids.get(reference);
        if (id != null) {
            ids.put(reference, id);
            refs.put(id, reference);
        }
    }

    /**
     * Returns a reference given its identifier.
     *
     * @param id the identifier
     * @return the corresponding reference, or <tt>null</tt> if none is found
     */
    public IMObjectReference getReference(String id) {
        return refs.get(stripPrefix(id));
    }

    /**
     * Returns an identifier given its object reference.
     *
     * @param reference the object reference
     * @return the corresponding identifier, or <tt>null</tt> if none is found
     */
    public String getId(IMObjectReference reference) {
        return ids.get(reference);
    }

    /**
     * Helper to strip any prefix from an identifier.
     *
     * @param id the identifier
     * @return the id with any prefix removed
     * @throws ArchetypeDataLoaderException if the id is invalid
     */
    public static String stripPrefix(String id) {
        if (id.startsWith(ID_PREFIX)) {
            id = id.substring(ID_PREFIX.length());
        }
        if (id.length() == 0) {
            throw new ArchetypeDataLoaderException(NullReference);
        }
        return id;
    }

}
