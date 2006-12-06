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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Collection;


/**
 * Iterates over the results of an {@link IMObject} query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectQueryIterator<T extends IMObject>
        extends QueryIterator<T> {

    /**
     * The nodes to query.If <code>null</code> query all rows
     */
    private final Collection<String> nodes;


    /**
     * Constructs a new <code>IMObjectQueryIterator</code>.
     *
     * @param query the query
     * @throws ArchetypeServiceException if the archetype service isn't
     *                                   initialised
     */
    public IMObjectQueryIterator(IArchetypeQuery query) {
        this(ArchetypeServiceHelper.getArchetypeService(), query, null);
    }

    /**
     * Constructs a new <code>IMObjectQueryIterator</code>.
     *
     * @param query the query
     * @param nodes the nodes to query. If <code>null</code> queries all nodes
     * @throws ArchetypeServiceException if the archetype service isn't
     *                                   initialised
     */
    public IMObjectQueryIterator(IArchetypeQuery query,
                                 Collection<String> nodes) {
        this(ArchetypeServiceHelper.getArchetypeService(), query, nodes);
    }

    /**
     * Constructs a new <code>IMObjectQueryIterator</code>.
     *
     * @param service the archetype service
     * @param query   the query
     */
    public IMObjectQueryIterator(IArchetypeService service,
                                 IArchetypeQuery query) {
        this(service, query, null);
    }

    /**
     * Constructs a new <code>IMObjectQueryIterator</code>.
     *
     * @param service the archetype service
     * @param query   the query
     * @param nodes   the nodes to query. If <code>null</code> queries all nodes
     */
    public IMObjectQueryIterator(IArchetypeService service,
                                 IArchetypeQuery query,
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
    @SuppressWarnings("unchecked")
    protected IPage<T> getPage(IArchetypeService service,
                               IArchetypeQuery query) {
        IPage result;
        result = (nodes == null) ? service.get(query) : service.get(query,
                                                                    nodes);
        return result;
    }
}
