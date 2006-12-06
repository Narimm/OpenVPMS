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

package org.openvpms.component.business.dao.im.common;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * Factory for {@link ResultCollector}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ResultCollectorFactory {

    /**
     * Creates a new collector of {@link IMObject}s.
     *
     * @return a new collector
     */
    ResultCollector<IMObject> createIMObjectCollector();

    /**
     * Creates a new collector of partially populated {@link IMObject}s.
     * This may be used to selectively load parts of object graphs to improve
     * performance.
     *
     * @param nodes the nodes to collect
     * @return a new collector
     */
    ResultCollector<IMObject> createIMObjectCollector(Collection<String> nodes);

    /**
     * Creates a new collector of {@link ObjectSet}s.
     *
     * @param names the names to assign the objects in the set. May be
     *              <code>null</code>
     * @param types a map of type aliases to their corresponding archetype short
     *              names. May be <code>null</code>
     * @return a new collector
     */
    ResultCollector<ObjectSet> createObjectSetCollector(
            Collection<String> names, Map<String, Set<String>> types);

    /**
     * Creates a new collector of {@link NodeSet}s.
     *
     * @param nodes the nodes to collect
     * @return a new collector
     */
    ResultCollector<NodeSet> createNodeSetCollector(Collection<String> nodes);

}
