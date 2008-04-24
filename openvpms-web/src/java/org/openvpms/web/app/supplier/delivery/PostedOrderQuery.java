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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.supplier.delivery;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.supplier.SupplierActQuery;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PostedOrderQuery extends SupplierActQuery<FinancialAct> {

    private static final String[] ACTS = {"act.supplierOrder"};

    /**
     * Constructs a new <tt>PostedOrderQuery</tt>.
     */
    public PostedOrderQuery() {
        super(ACTS, null, FinancialAct.class);
        setStatus(ActStatus.POSTED);
    }

    /**
     * Performs the query.
     * If constraining acts to a particular entity, the entity must be non-null
     * or a <tt>null</tt> will be returned.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set. May be <tt>null</tt>
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<FinancialAct> query(SortConstraint[] sort) {
        if (canQuery()) {
            return super.query(sort);
        }
        return null;
    }

    /**
     * Lays out the component in a container,.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addSupplierSelector(container);
        addStockLocationSelector(container);
    }

    /**
     * Creates a new result set.
     *
     * @param participants the participant constraints
     * @param sort         the sort criteria
     * @return a new result set
     */
    protected ResultSet<FinancialAct> createResultSet(
            ParticipantConstraint[] participants, SortConstraint[] sort) {
        return new ActResultSet<FinancialAct>(getArchetypeConstraint(),
                                              participants, null, null,
                                              getStatuses(), false,
                                              getConstraints(), getMaxResults(),
                                              sort);
    }

    /**
     * Notify listeners to perform a query.
     * <p/>
     * This only proceeds if the supplier and stock location are set.
     */
    @Override
    protected void onQuery() {
        if (canQuery()) {
            super.onQuery();
        }
    }

    private boolean canQuery() {
        return (getSupplier() != null && getStockLocation() != null);
    }
}
