/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.query.TypedQuery;
import org.openvpms.component.system.common.query.criteria.MappedCriteriaQuery;

import java.util.List;

/**
 * Default implementation of {@link TypedQuery}.
 *
 * @author Tim Anderson
 */
public class TypedQueryImpl<X, Y> implements TypedQuery<X> {

    /**
     * The criteria query.
     */
    private final MappedCriteriaQuery<Y> criteriaQuery;

    /**
     * The result type.
     */
    private final Class<X> type;

    /**
     * The DAO.
     */
    private final IMObjectDAO dao;

    /**
     * The first result.
     */
    private int firstResult = 0;

    /**
     * The maximum number of results, or {@code Integer.MAX_VALUE} for all results.
     */
    private int maxResults = Integer.MAX_VALUE;


    /**
     * Constructs a {@link TypedQueryImpl}.
     *
     * @param criteriaQuery the criteria query
     * @param type          the result type
     * @param dao           the data access object
     */
    public TypedQueryImpl(MappedCriteriaQuery<Y> criteriaQuery, Class<X> type, IMObjectDAO dao) {
        this.criteriaQuery = criteriaQuery;
        this.type = type;
        this.dao = dao;
    }

    /**
     * Executes a select query.
     *
     * @return the results of the query
     */
    @Override
    public List<X> getResultList() {
        return dao.getResults(criteriaQuery, type, firstResult, maxResults);
    }

    /**
     * Executes a select query that returns a single result.
     *
     * @return the result of the query
     */
    @Override
    public X getSingleResult() {
        return dao.getSingleResult(criteriaQuery, type);
    }

    /**
     * Sets the maximum number of results to retrieve.
     *
     * @param maxResults the maximum number of results, or {@code Integer.MAX_VALUE} to not limit results
     * @return this query
     */
    @Override
    public TypedQuery<X> setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    /**
     * Sets the position of the first result to retrieve.
     *
     * @param position the first result position
     * @return this query
     */
    @Override
    public TypedQuery<X> setFirstResult(int position) {
        this.firstResult = position;
        return this;
    }


}
