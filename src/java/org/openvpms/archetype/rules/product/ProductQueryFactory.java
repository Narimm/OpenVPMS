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

package org.openvpms.archetype.rules.product;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Product query factory.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductQueryFactory {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ProductQueryFactory.class);


    /**
     * Creates a new product query.
     *
     * @param shortName the product short name. May contain wildcards.
     * @param name      the product name. May be <code>null</code> or contain
     *                  wildcards
     * @param sort      the sort criteria. May be <code>null</code>
     * @return a new product query
     */
    public static IArchetypeQuery create(String shortName,
                                         String name,
                                         SortConstraint[] sort) {
        return create(new String[]{shortName}, name, sort);
    }

    /**
     * Creates a new product query.
     *
     * @param shortNames the product short names
     * @param name       the product name. May be <code>null</code> or contain
     *                   wildcards
     * @param sort       the sort criteria. May be <code>null</code>
     * @return a new product query
     */
    public static IArchetypeQuery create(String[] shortNames,
                                         String name,
                                         SortConstraint[] sort) {
        return create(shortNames, name, null, sort);
    }

    /**
     * Creates a new product query, optionally constrained on species type.
     *
     * @param shortNames the product short names
     * @param name       the product name. May be <code>null</code> or contain
     *                   wildcards
     * @param species    the species type. May be <code>null</code>
     * @param sort       the sort criteria. May be <code>null</code>
     * @return a new product query
     */
    public static IArchetypeQuery create(String[] shortNames,
                                         String name, String species,
                                         SortConstraint[] sort) {
        shortNames = DescriptorHelper.getShortNames(shortNames);
        if (species != null && shortNames.length <= 4) {
            return createNamedQuery(shortNames, name, species);
        } else if (species != null) {
            log.warn("Cannot perform query on species: too many products");
        }
        return createArchetypeQuery(shortNames, name, sort);
    }

    /**
     * Creates a new query.
     *
     * @param shortNames the product short names
     * @param name       the product name. May be <code>null</code> or contain
     *                   wildcards
     * @param sort       the sort criteria. May be <code>null</code>
     * @return a new query
     */
    private static ArchetypeQuery createArchetypeQuery(String[] shortNames,
                                                       String name,
                                                       SortConstraint[] sort) {
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        if (!StringUtils.isEmpty(name)) {
            query.add(new NodeConstraint("name", name));
        }
        if (sort != null) {
            for (SortConstraint s : sort) {
                query.add(s);
            }
        }
        return query;
    }

    /**
     * Creates a new named query.
     *
     * @param shortNames the product short names
     * @param name       the product name. May be <code>null</code> or contain
     *                   wildcards
     * @param name       the product name. May contain wildcards
     * @param species    the species type
     * @return a new named query
     */
    private static NamedQuery createNamedQuery(String[] shortNames, String name,
                                               String species) {
        NamedQuery query;
        int count = shortNames.length;
        if (!StringUtils.isEmpty(name)) {
            query = new NamedQuery("product.nameAndSpeciesQuery" + count);
            name = name.replace("*", "%");
            query.setParameter("name", name);
        } else {
            query = new NamedQuery("product.speciesQuery" + count);
        }
        query.setParameter("species", species);
        int index = 1;
        for (String shortName : shortNames) {
            String concept = shortName.substring(
                    shortName.lastIndexOf(".") + 1);
            query.setParameter("concept" + index, concept);
            ++index;
        }
        return query;
    }
}
