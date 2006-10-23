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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.NodeResolverException;

import java.util.ArrayList;
import java.util.List;


/**
 * This class provides a list of helper functions for using the
 * archetype service.
 * <p/>
 * These functions can then be used as JXPath extension functions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceFunctions {

    /**
     * This will take a list of {@link IMObjectReference} instances and return
     * a list of the corresponding {@link IMObject} instances.
     *
     * @param references a list of references
     * @return List<IMObject>
     */
    public static List<IMObject> resolveRefs(
            List<IMObjectReference> references) {
        List<IMObject> objects = new ArrayList<IMObject>();
        for (IMObjectReference ref : references) {
            objects.add(ArchetypeQueryHelper.getByObjectReference(
                    ArchetypeServiceHelper.getArchetypeService(), ref));
        }

        return objects;
    }

    /**
     * Return the {@link IMObject} given a reference.
     *
     * @param reference the reference
     * @return IMObject
     */
    public static IMObject resolve(IMObjectReference reference) {
        return ArchetypeQueryHelper.getByObjectReference(
                ArchetypeServiceHelper.getArchetypeService(), reference);
    }

    /**
     * Resolves the value at the specified node.
     *
     * @param object the expression context
     * @param node   the node name
     * @return the value at the specified node
     * @throws NodeResolverException if the name is invalid
     * @see NodeResolver
     */
    public static Object get(IMObject object, String node) {
        NodeResolver resolver = new NodeResolver(
                object, ArchetypeServiceHelper.getArchetypeService());
        return resolver.getObject(node);
    }

    /**
     * Resolves the value at the specified node.  
     * If Node doesn't exist returns the defaultValue.
     *
     * @param object the expression context
     * @param node   the node name
     * @param defaultValue  the value to return if node does not exist
     * @return the value at the specified node or defaultValue if node does not exist
     * @see NodeResolver
     */
    public static Object get(IMObject object, String node, Object defaultValue) {
        try
        {
           return get(object, node);  
        }
        catch (NodeResolverException exception)
        {
            return defaultValue;
        }
    }
}
