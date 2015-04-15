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

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Collection;
import java.util.Iterator;


/**
 * <tt>Iterable<tt> adapter for the results of an {@link IMObject} query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see IMObjectQueryIterator
 */
public class IterableIMObjectQuery<T extends IMObject>
        extends IterableQuery<T> {

    /**
     * The nodes to query. If <code>null</code> query all nodes.
     */
    private final Collection<String> nodes;


    /**
     * Constructs a new <tt>IterableIMObjectQuery</tt>.
     *
     * @param query the query
     */
    public IterableIMObjectQuery(IArchetypeQuery query) {
        this(ArchetypeServiceHelper.getArchetypeService(), query);
    }

    /**
     * Constructs a new <tt>IterableIMObjectQuery</tt>.
     *
     * @param service the archetype service
     * @param query   the query
     */
    public IterableIMObjectQuery(IArchetypeService service,
                                 IArchetypeQuery query) {
        this(service, query, null);
    }

    /**
     * Constructs a new <tt>IterableIMObjectQuery</tt>.
     *
     * @param query the query
     * @param nodes the nodes to query. If <tt>null</tt> queries all nodes
     */
    public IterableIMObjectQuery(IArchetypeQuery query,
                                 Collection<String> nodes) {
        this(ArchetypeServiceHelper.getArchetypeService(), query, nodes);
    }

    /**
     * Constructs a new <tt>IterableIMObjectQuery</tt>.
     *
     * @param service the archetype service
     * @param query   the query
     * @param nodes   the nodes to query. If <tt>null</tt> queries all nodes
     */
    public IterableIMObjectQuery(IArchetypeService service,
                                 IArchetypeQuery query,
                                 Collection<String> nodes) {
        super(service, (AbstractArchetypeQuery) query);
        this.nodes = nodes;
    }

    /**
     * Returns an iterator over the query results.
     *
     * @return an iterator.
     */
    public Iterator<T> iterator() {
        return new IMObjectQueryIterator<T>(getArchetypeService(), getQuery(),
                                            nodes);
    }

}
