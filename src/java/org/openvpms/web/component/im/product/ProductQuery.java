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

package org.openvpms.web.component.im.product;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.EntityResultSet;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Query implementation that queries {@link Product} instances on short name,
 * instance name, active/inactive status and species.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-04 07:03:58Z $
 */
public class ProductQuery extends AbstractEntityQuery<Product> {

    /**
     * The species to constrain the query to. May be <tt>null</tt>.
     */
    private String species;

    /**
     * The stock location to constrain the query to. May be <tt>null</tt>.
     */
    private Party location;


    /**
     * Construct a new <tt>ProductQuery</tt> that queries products with the
     * specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public ProductQuery(String[] shortNames) {
        super(shortNames, Product.class);
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
     * Sets the stock location to constrain the query to.
     *
     * @param location the stock location
     */
    public void setStockLocation(Party location) {
        this.location = location;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Product> query(SortConstraint[] sort) {
        ResultSet<Product> result = super.query(sort);
        if (location != null) {
            result = filterOnLocation(result);
        }
        if (!StringUtils.isEmpty(species)) {
            result = filterOnSpecies(result);
        }
        return result;
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     * @return a new result set
     */
    protected ResultSet<Product> createResultSet(SortConstraint[] sort) {
        return new EntityResultSet<Product>(getArchetypeConstraint(), getValue(),
                                            isIdentitySearch(),
                                            getConstraints(),
                                            sort, getMaxResults(),
                                            isDistinct());
    }

    /**
     * Filters products to include only those that either have no stock location
     * or have a location matching that specified.
     * TODO: could do this in HQL, but requires use of WITH clause
     *
     * @param set the set to filter
     * @return the filtered set
     */
    private ResultSet<Product> filterOnLocation(ResultSet<Product> set) {
        List<Product> matches = new ArrayList<Product>();
        IMObjectReference ref = location.getObjectReference();
        while (set.hasNext()) {
            IPage<Product> page = set.next();
            for (Product product : page.getResults()) {
                EntityBean bean = new EntityBean(product);
                if (bean.hasNode("stockLocations")) {
                    List<IMObjectReference> locations
                            = bean.getNodeTargetEntityRefs("stockLocations");
                    if (locations.isEmpty() || locations.contains(ref)) {
                        matches.add(product);
                    }
                } else {
                    matches.add(product);
                }

            }
        }
        return new IMObjectListResultSet<Product>(matches, getMaxResults());
    }

    /**
     * Filters products to include only those that either have no species
     * classification, or have a classification matching the species.
     *
     * @param set the set to filter
     * @return the filtered set
     */
    private ResultSet<Product> filterOnSpecies(ResultSet<Product> set) {
        List<Product> matches = new ArrayList<Product>();
        while (set.hasNext()) {
            IPage<Product> page = set.next();
            for (Product product : page.getResults()) {
                IMObjectBean bean = new IMObjectBean(product);
                if (bean.hasNode("species")) {
                    Collection<IMObject> list = bean.getValues("species");
                    if (list.isEmpty()) {
                        matches.add(product);
                    } else {
                        for (IMObject object : list) {
                            Lookup lookup = (Lookup) object;
                            if (StringUtils.equals(lookup.getCode(), species)) {
                                matches.add(product);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return new IMObjectListResultSet<Product>(matches, getMaxResults());
    }

}
