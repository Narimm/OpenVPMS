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


import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.component.system.common.query.ArchetypeQueryException.ErrorCode.CloneNotSupported;


/**
 * Adapter to make {@link IArchetypeQuery} results iterable.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class IterableQuery<T> implements Iterable<T> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The query.
     */
    private final AbstractArchetypeQuery query;

    /**
     * Determines if {@link #getQuery()} is being invoked for the first time.
     */
    private boolean first = true;

    /**
     * Cached value of the first result.
     */
    private final int firstResult;


    /**
     * Creates a new <tt>IterableQuery</tt>.
     *
     * @param service the archetype service
     * @param query   the query
     */
    public IterableQuery(IArchetypeService service,
                         AbstractArchetypeQuery query) {
        this.query = query;
        this.firstResult = query.getFirstResult();
        this.service = service;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return service;
    }

    /**
     * Returns the query. As multiple iterators may be used, copies are returned
     * for all but the first call.
     *
     * @return the query or its copy
     * @throws ArchetypeQueryException if the copy fails
     */
    protected IArchetypeQuery getQuery() {
        IArchetypeQuery result;
        try {
            if (first) {
                result = query;
                first = false;
            } else {
                result = (AbstractArchetypeQuery) query.clone();
                result.setFirstResult(firstResult);
            }
        } catch (CloneNotSupportedException exception) {
            throw new ArchetypeQueryException(CloneNotSupported, exception,
                                              query);
        }
        return result;
    }
}
