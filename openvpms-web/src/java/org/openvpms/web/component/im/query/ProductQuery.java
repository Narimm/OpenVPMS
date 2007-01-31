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

import org.openvpms.archetype.rules.product.ProductQueryFactory;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Query implementation that queries {@link Product} instances on short name,
 * instance name, and active/inactive status.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-04 07:03:58Z $
 */
public class ProductQuery extends AbstractQuery<Product> {

    /**
     * The species to constrain the query to. May be <code>null</code>.
     */
    private String species;


    /**
     * Construct a new <code>DefaultQuery</code> that queries IMObjects with the
     * specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public ProductQuery(String[] shortNames) {
        super(shortNames);
    }

    /**
     * Construct a new <code>DefaultQuery</code> that queries IMObjects with the
     * specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public ProductQuery(String refModelName, String entityName,
                        String conceptName) {
        super(refModelName, entityName, conceptName);
    }

    /**
     * Sets the species to constrain the query to.
     *
     * @param species the species classification
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <code>null</code>
     * @return a new result set
     */
    protected ResultSet<Product> createResultSet(SortConstraint[] sort) {
        return new ProductResultSet(getMaxResults(), sort);
    }

    class ProductResultSet extends AbstractArchetypeServiceResultSet<Product> {

        /**
         * Construct a new <code>ProductResultSet</code>.
         *
         * @param pageSize the maximum no. of results per page
         * @param sort     the sort criteria. May be <code>null</code>
         */
        public ProductResultSet(int pageSize, SortConstraint[] sort) {
            super(pageSize, sort);
        }

        /**
         * Returns the specified page.
         *
         * @param firstResult the first result of the page to retrieve
         * @param maxResults  the maximun no of results in the page
         * @return the page corresponding to <code>firstResult</code>, or
         *         <code>null</code> if none exists
         */
        @SuppressWarnings("unchecked")
        protected IPage<Product> getPage(int firstResult, int maxResults) {
            String[] shortNames;
            if (getShortName() != null) {
                shortNames = new String[]{getShortName()};
            } else {
                shortNames = getShortNames();
            }
            IArchetypeQuery query = ProductQueryFactory.create(
                    shortNames, getName(), species, getSortConstraints());
            query.setFirstResult(firstResult);
            query.setMaxResults(maxResults);
            query.setCountResults(true);
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            IPage result = service.get(query);
            return (IPage<Product>) result;
        }

    }

}
