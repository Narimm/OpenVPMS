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
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Product query factory.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductQueryFactory {

    /**
     * Creates a new product query.
     *
     * @param shortName the product short name. May contain wildcards.
     * @param name      the product name. May be <tt>null</tt> or contain wildcards
     * @param sort      the sort criteria. May be <tt>null</tt>
     * @return a new product query
     */
    public static IArchetypeQuery create(String shortName, String name, SortConstraint[] sort) {
        return create(new String[]{shortName}, name, sort);
    }

    /**
     * Creates a new product query.
     *
     * @param shortNames the product short names
     * @param name       the product name. May be <tt>null</tt> or contain wildcards
     * @param sort       the sort criteria. May be <tt>null</tt>
     * @return a new product query
     */
    public static IArchetypeQuery create(String[] shortNames, String name, SortConstraint[] sort) {
        return create(shortNames, name, null, sort);
    }

    /**
     * Creates a new product query, optionally constrained on species type.
     *
     * @param shortNames the product short names
     * @param name       the product name. May be <tt>null</tt> or contain wildcards
     * @param species    the species lookup code. May be <tt>null</tt>
     * @param sort       the sort criteria. May be <tt>null</tt>
     * @return a new product query
     */
    public static IArchetypeQuery create(String[] shortNames, String name, String species, SortConstraint[] sort) {
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        if (!StringUtils.isEmpty(name)) {
            query.add(Constraints.eq("name", name));
        }
        if (!StringUtils.isEmpty(species)) {
            query.setDistinct(true);
            query.add(Constraints.leftJoin("species"))
                    .add(Constraints.or(Constraints.eq("species.code", species),
                                        Constraints.isNull("species.code")));
        }
        if (sort != null) {
            for (SortConstraint s : sort) {
                query.add(s);
            }
        }
        return query;
    }

}
