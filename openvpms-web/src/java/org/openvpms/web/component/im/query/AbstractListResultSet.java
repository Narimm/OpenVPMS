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

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

import java.util.ArrayList;
import java.util.List;


/**
 * Paged result set where the results are pre-loaded from list.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractListResultSet<T> extends AbstractResultSet<T> {

    /**
     * The query objects.
     */
    private final List<T> objects;


    /**
     * Constructs a new <tt>AbstractListResultSet</tt>.
     *
     * @param objects  the objects
     * @param pageSize the maximum no. of results per page
     */
    public AbstractListResultSet(List<T> objects, int pageSize) {
        super(pageSize);
        this.objects = objects;
        reset();
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    public void setDistinct(boolean distinct) {
        // no-op
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return <code>true</code> if duplicate results should be removed;
     *         otherwise <code>false</code>
     */
    public boolean isDistinct() {
        return false;
    }

    /**
     * Returns the underlying list.
     *
     * @return the underlying list
     */
    protected List<T> getObjects() {
        return objects;
    }

    /**
     * Returns the specified page.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximun no of results in the page
     * @return the page corresponding to <code>firstResult</code>, or
     *         <code>null</code> if none exists
     */
    protected IPage<T> getPage(int firstResult, int maxResults) {
        int to;
        if (maxResults == ArchetypeQuery.ALL_RESULTS
                || ((firstResult + maxResults) >= objects.size())) {
            to = objects.size();
        } else {
            to = firstResult + maxResults;
        }
        List<T> rows = new ArrayList<T>(objects.subList(firstResult, to));
        return new Page<T>(rows, firstResult, maxResults, objects.size());
    }
}
