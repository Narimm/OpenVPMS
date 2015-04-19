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

import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Iterator;


/**
 * <tt>Iterable<tt> adapter for the results of an {@link ObjectSet} query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see ObjectSetQueryIterator
 */
public class IterableObjectSetQuery extends IterableQuery<ObjectSet> {

    /**
     * Constructs a new <tt>IterableObjectSetQuery</tt>.
     *
     * @param query the query
     */
    public IterableObjectSetQuery(IArchetypeQuery query) {
        this(ArchetypeServiceHelper.getArchetypeService(), query);
    }

    /**
     * Constructs a new <tt>IterableObjectSetQuery</tt>.
     *
     * @param service the archetype service
     * @param query   the query
     */
    public IterableObjectSetQuery(IArchetypeService service,
                                  IArchetypeQuery query) {
        super(service, (AbstractArchetypeQuery) query);
    }

    /**
     * Returns an iterator over the query results.
     *
     * @return an iterator.
     */
    public Iterator<ObjectSet> iterator() {
        return new ObjectSetQueryIterator(getArchetypeService(), getQuery());
    }

}
