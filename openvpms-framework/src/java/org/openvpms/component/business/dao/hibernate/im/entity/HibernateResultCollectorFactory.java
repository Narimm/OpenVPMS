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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.business.dao.im.common.ResultCollectorFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * Implementation of the {@link ResultCollectorFactory} for hibernate.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class HibernateResultCollectorFactory implements ResultCollectorFactory {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The object loader.
     */
    private final ObjectLoader loader;


    /**
     * Constructs a new <code>HibernateResultCollectorFactory</code>.
     */
    public HibernateResultCollectorFactory() {
        this(null, new DefaultObjectLoader());
    }

    /**
     * Constructs a new <code>HibernateResultCollectorFactory</code>.
     *
     * @param service the archetype service. May be <code>null</code>
     * @param loader  the object loader
     */
    public HibernateResultCollectorFactory(IArchetypeService service,
                                           ObjectLoader loader) {
        this.service = service;
        this.loader = loader;
    }

    /**
     * Creates a new collector of {@link IMObject}s.
     *
     * @return a new collector
     */
    public ResultCollector<IMObject> createIMObjectCollector() {
        HibernateResultCollector<IMObject> result
                = new IMObjectResultCollector();
        result.setLoader(loader);
        return result;
    }

    /**
     * Creates a new collector of partially populated {@link IMObject}s.
     * This may be used to selectively load parts of object graphs to improve
     * performance.
     *
     * @param nodes the nodes to collect
     * @return a new collector
     */
    public ResultCollector<IMObject> createIMObjectCollector(
            Collection<String> nodes) {
        HibernateResultCollector<IMObject> result
                = new IMObjectNodeResultCollector(getArchetypeService(), nodes);
        result.setLoader(loader);
        return result;
    }

    /**
     * Creates a new collector of {@link ObjectSet}s.
     *
     * @param names the names to assign the objects in the set. May be
     *              <code>null</code>
     * @param types a map of type aliases to their corresponding archetype short
     *              names. May be <code>null</code>
     * @return a new collector
     */
    public ResultCollector<ObjectSet> createObjectSetCollector(
            Collection<String> names, Map<String, Set<String>> types) {
        HibernateResultCollector<ObjectSet> result
                = new ObjectSetResultCollector(names, types);
        result.setLoader(loader);
        return result;
    }

    /**
     * Creates a new collector of {@link NodeSet}s.
     *
     * @param nodes the nodes to collect
     * @return a new collector
     */
    public ResultCollector<NodeSet> createNodeSetCollector(
            Collection<String> nodes) {
        HibernateResultCollector<NodeSet> result
                = new NodeSetResultCollector(getArchetypeService(), nodes);
        result.setLoader(loader);
        return result;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     * @throws ArchetypeServiceException if the archetype service isn't
     *                                   initialised
     */
    private synchronized IArchetypeService getArchetypeService() {
        if (service == null) {
            service = ArchetypeServiceHelper.getArchetypeService();
        }
        return service;
    }
}
