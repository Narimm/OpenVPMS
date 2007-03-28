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

package org.openvpms.etl.load;

import org.openvpms.etl.ETLValue;
import org.openvpms.etl.ETLValueDAO;
import org.openvpms.etl.ETLValueDAOImpl;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Paging iterator for {@link ETLValue} instances returned by
 * {@link ETLValueDAOImpl#get(int, int)}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ETLValueIterator implements Iterator<ETLValue> {

    /**
     * The DAO.
     */
    private final ETLValueDAO dao;

    /**
     * The first result to retrieve.
     */
    private int first;

    /**
     * The maximum no. of results to retrieve.
     */
    private int maxResults = 1000;

    /**
     * The current page.
     */
    private List<ETLValue> page;

    /**
     * Iterator over the current page.
     */
    private Iterator<ETLValue> iterator;


    /**
     * Constructs a new <code>ETLValueIterator</code>.
     *
     * @param dao the DAO
     */
    public ETLValueIterator(ETLValueDAO dao) {
        this.dao = dao;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        if (page == null || !iterator.hasNext()) {
            page = dao.get(first, maxResults);
            first += maxResults;
            iterator = page.iterator();
        }
        return iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.  Calling this method
     * repeatedly until the {@link #hasNext()} method returns false will
     * return each element in the underlying collection exactly once.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public ETLValue next() {
        return iterator.next();
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
