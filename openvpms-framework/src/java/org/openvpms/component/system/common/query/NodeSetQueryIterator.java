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

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Collection;


/**
 * Iterates over the results of an {@link NodeSet} query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeSetQueryIterator extends QueryIterator<NodeSet> {

    /**
     * The nodes to query.
     */
    private final Collection<String> nodes;

    /**
     * Constructs a new <code>NodeSetQueryIterator</code>.
     *
     * @param query the query
     * @param nodes the nodes to query
     * @throws ArchetypeServiceException if the archetype service isn't
     *                                   initialised
     */
    public NodeSetQueryIterator(IArchetypeQuery query, Collection<String> nodes) {
        this(ArchetypeServiceHelper.getArchetypeService(), query, nodes);
    }

    /**
     * Constructs a new <code>NodeSetQueryIterator</code>.
     *
     * @param service the archetype service
     * @param query   the query
     * @param nodes   the nodes to query
     */
    public NodeSetQueryIterator(IArchetypeService service, IArchetypeQuery query,
                                Collection<String> nodes) {
        super(service, query);
        this.nodes = nodes;
    }

    /**
     * Returns the next page.
     *
     * @return the next page
     * @throws ArchetypeServiceException if the query fails
     */
    protected IPage<NodeSet> getPage(IArchetypeService service,
                                     IArchetypeQuery query) {
        return service.getNodes(query, nodes);
    }
}
