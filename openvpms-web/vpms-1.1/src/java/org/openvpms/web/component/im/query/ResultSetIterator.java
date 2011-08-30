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

import org.openvpms.component.system.common.query.IPage;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Iterator over an {@link ResultSet}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ResultSetIterator<T> implements Iterator<T> {

    /**
     * The result set.
     */
    private final ResultSet<T> set;

    /**
     * Iterator over the current page.
     */
    private Iterator<T> pageIterator;


    /**
     * Constructs a new <tt>ResultSetIterator</tt>.
     *
     * @param set the result set
     */
    public ResultSetIterator(ResultSet<T> set) {
        this.set = set;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        if (pageIterator == null || !pageIterator.hasNext()) {
            advance();
        }
        return pageIterator != null && pageIterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.  Calling this method
     * repeatedly until the {@link #hasNext()} method returns false will
     * return each element in the underlying collection exactly once.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public T next() {
        if (pageIterator == null || !pageIterator.hasNext()) {
            if (!advance()) {
                throw new NoSuchElementException();
            }
        }
        return pageIterator.next();
    }

    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Attempts to advance to the next page.
     *
     * @return <tt>true</tt> if the advance was successful.
     */
    private boolean advance() {
        if (set.hasNext()) {
            IPage<T> page = set.next();
            pageIterator = page.getResults().iterator();
            return true;
        }
        return false;
    }

}
