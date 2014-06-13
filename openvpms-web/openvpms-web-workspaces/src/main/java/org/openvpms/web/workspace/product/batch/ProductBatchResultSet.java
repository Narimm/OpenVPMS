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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.batch;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.EntityResultSet;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;

import java.util.Date;

import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.isNull;
import static org.openvpms.component.system.common.query.Constraints.join;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class ProductBatchResultSet extends EntityResultSet<Entity> {

    private static final String EXPIRY_DATE = "activeEndTime";
    private final String product;

    private final Date from;

    private final Date to;

    private final String manufacturer;

    /**
     * Construct a new {@code EntityResultSet}.
     *
     * @param archetypes   the archetypes to query
     * @param value        the value to query on. May be {@code null}
     * @param product      the product name to search on. May be {@code null}
     * @param manufacturer the manufacturer to search on. May be {@code null}
     * @param sort         the sort criteria. May be {@code null}
     * @param rows         the maximum no. of rows per page
     */
    public ProductBatchResultSet(ShortNameConstraint archetypes, String value, String product,
                                 Date from, Date to, String manufacturer, SortConstraint[] sort, int rows) {
        super(archetypes, value, false, null, sort, rows, true);
        this.product = product;
        this.from = DateRules.getDate(from);
        this.to = DateRules.getPreviousDate(to); // createDateConstraint() uses < to + 1
        this.manufacturer = manufacturer;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();
        JoinConstraint productJoin = null;
        if (product != null || from != null || to != null) {
            productJoin = join("product", "product");
            if (product != null) {
                productJoin.add(join("target", "t1").add(eq("name", product)));
            }
            IConstraint expiryDateConstraint = QueryHelper.createDateConstraint(EXPIRY_DATE, from, to);
            if (expiryDateConstraint != null) {
                if (from != null && to == null) {
                    // handle the case where a batch may have no expiry date
                    productJoin.add(Constraints.or(expiryDateConstraint, isNull(EXPIRY_DATE)));
                } else {
                    productJoin.add(expiryDateConstraint);
                }
            }
            query.add(productJoin);
        }
        if (manufacturer != null) {
            query.add(join("manufacturer").add(join("target", "t2").add(eq("name", manufacturer))));
        }
        for (SortConstraint sort : getSortConstraints()) {
            if (sort instanceof VirtualNodeSortConstraint) {
                VirtualNodeSortConstraint node = (VirtualNodeSortConstraint) sort;
                if ("expiryDate".equals(node.getNodeName())) {
                    if (productJoin == null) {
                        productJoin = join("product", "product");
                        query.add(productJoin);
                    }
                    productJoin.add(Constraints.sort("product", "activeEndTime", node.isAscending()));
                }
            } else if (sort instanceof NodeSortConstraint) {
                NodeSortConstraint node = (NodeSortConstraint) sort;
                NodeDescriptor descriptor = QueryHelper.getDescriptor(getArchetypes(), node.getNodeName());
                if (descriptor != null && QueryHelper.isEntityLinkNode(descriptor)) {
                    ShortNameConstraint shortNames = (ShortNameConstraint) query.getArchetypeConstraint();
                    QueryHelper.addSortOnEntityLink(shortNames, query, descriptor, node.isAscending());
                } else {
                    query.add(sort);
                }
            } else {
                query.add(sort);
            }
        }

        return query;
    }

    /**
     * Adds sort constraints.
     * <p/>
     * This implementation is a no-op as this is handled by {@link #createQuery()} above.
     *
     * @param query the query to add the constraints to
     */
    @Override
    protected void addSortConstraints(ArchetypeQuery query) {
    }
}
