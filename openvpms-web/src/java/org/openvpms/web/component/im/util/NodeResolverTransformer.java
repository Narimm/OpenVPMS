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

package org.openvpms.web.component.im.util;

import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LRUMap;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;


/**
 * A <tt>Transformer</tt> that provides access to a node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeResolverTransformer implements Transformer {

    /**
     * The node name.
     */
    private final String node;

    private LRUMap cache;

    /**
     * Creates a new <tt>NodeResolverTransformer</tt>, that doesn't cache node
     * values.
     *
     * @param node the node name
     */
    public NodeResolverTransformer(String node) {
        this(node, 0);
    }

    /**
     * Creates a new <tt>NodeResolverTransformer</tt>.
     *
     * @param node      the node name
     * @param cacheSize the size of the node cache. Use <= 0 to indicate no
     *                  cache
     */
    public NodeResolverTransformer(String node, int cacheSize) {
        this.node = node;
        if (cacheSize > 0) {
            cache = new LRUMap(cacheSize);
        }
    }


    /**
     * Transforms the input object (leaving it unchanged) into some output
     * object.
     *
     * @param input the object to be transformed, should be left unchanged
     * @return a transformed object
     * @throws ClassCastException       (runtime) if the input is the wrong
     *                                  class
     * @throws IllegalArgumentException (runtime) if the input is invalid
     * @throws FunctorException         (runtime) if the transform cannot be
     *                                  completed
     */
    public Object transform(Object input) {
        Object result = null;
        if (input instanceof IMObject) {
            if (cache != null) {
                result = cache.get(input);
            }
            if (result == null) {
                IArchetypeService service
                        = ArchetypeServiceHelper.getArchetypeService();
                NodeResolver resolver = new NodeResolver((IMObject) input,
                                                         service);
                result = resolver.getObject(node);
                if (cache != null) {
                    cache.put(input, result);
                }
            }
        }
        return result;
    }

}
