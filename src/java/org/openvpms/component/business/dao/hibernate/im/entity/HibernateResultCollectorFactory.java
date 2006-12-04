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
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class HibernateResultCollectorFactory implements ResultCollectorFactory {

    private IArchetypeService service;

    private final ObjectLoader loader;

    public HibernateResultCollectorFactory() {
        this(null, new DefaultObjectLoader());
    }

    public HibernateResultCollectorFactory(IArchetypeService service,
                                           ObjectLoader loader) {
        this.service = service;
        this.loader = loader;
    }

    public ResultCollector<IMObject> createIMObjectCollector() {
        HibernateResultCollector<IMObject> result
                = new IMObjectResultCollector();
        result.setLoader(loader);
        return result;
    }

    public ResultCollector<IMObject> createIMObjectCollector(
            Collection<String> nodes) {
        HibernateResultCollector<IMObject> result
                = new IMObjectNodeResultCollector(getArchetypeService(), nodes);
        result.setLoader(loader);
        return result;
    }

    public ResultCollector<ObjectSet> createObjectSetCollector(
            Collection<String> names, Map<String, Set<String>> types) {
        HibernateResultCollector<ObjectSet> result
                = new ObjectSetResultCollector(names, types);
        result.setLoader(loader);
        return result;
    }

    public ResultCollector<NodeSet> createNodeSetCollector(
            Collection<String> nodes) {
        HibernateResultCollector<NodeSet> result
                = new NodeSetResultCollector(getArchetypeService(), nodes);
        result.setLoader(loader);
        return result;
    }

    private synchronized IArchetypeService getArchetypeService() {
        if (service == null) {
            service = ArchetypeServiceHelper.getArchetypeService();
        }
        return service;
    }
}
