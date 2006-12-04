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

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.dao.im.common.ResultCollector;
import org.openvpms.component.system.common.query.IPage;

import java.util.List;


/**
 * Abstract implementation of the {@link ResultCollector} interface
 * for hibernate.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
abstract class HibernateResultCollector<T> implements ResultCollector<T> {

    /**
     * The page.
     */
    private Page<T> page = new Page<T>();

    /**
     * The loader.
     */
    private ObjectLoader loader;

    /**
     * Sets the loader.
     *
     * @param loader the loader
     */
    public void setLoader(ObjectLoader loader) {
        this.loader = loader;
    }

    /**
     * Returns the loader.
     *
     * @return the loader
     */
    public ObjectLoader getLoader() {
        return loader;
    }

    /**
     * Sets the first result.
     *
     * @param first the first result
     */
    public void setFirstResult(int first) {
        page.setFirstResult(first);
    }

    /**
     * Sets the page sizse.
     *
     * @param size the page size
     */
    public void setPageSize(int size) {
        page.setPageSize(size);
    }

    /**
     * Sets the total no. of results matching the query criteria.
     *
     * @param total the total no. of results matching the query criteria, or
     *              <code>-1</code> if not calculated
     */
    public void setTotalResults(int total) {
        page.setTotalResults(total);
    }

    /**
     * Returns the collected page.
     *
     * @return the collected page
     */
    public IPage<T> getPage() {
        page.setResults(getResults());
        return page;
    }

    /**
     * Returns the rsults.
     *
     * @return the results
     */
    protected abstract List<T> getResults();

}
