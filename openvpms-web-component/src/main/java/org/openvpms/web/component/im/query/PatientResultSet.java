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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ObjectSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Date;


/**
 * An {@link org.openvpms.web.component.im.query.ResultSet} implementation that queries patients. The search can be
 * further constrained to match on:
 * <ul>
 * <li>partial patient name; and/or
 * <li>partial identity
 * </ul>
 * <p/>
 * The returned {@link org.openvpms.component.system.common.query.ObjectSet}s contain:
 * <ul>
 * <li>the patient:
 * <pre>Party patient = (Party) set.get("patient");</pre>
 * <li>the identity, if searching on identities:
 * <pre>EntityIdentity identity = (EntityIdentity) set.get("identity");</pre>
 * </ul>
 *
 * @author Tim Anderson
 */
public class PatientResultSet extends AbstractEntityResultSet<ObjectSet> {

    /**
     * The customer to return patients for. If {@code null}, queries all patients.
     */
    private Party customer;

    /**
     * Creates a new {@code PatientResultSet}.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be {@code null}
     * @param searchIdentities if {@code true} search on identity name
     * @param customer         if specified, only return patients for the specified customer
     * @param sort             the sort criteria. May be {@code null}
     * @param rows             the maximum no. of rows per page
     */
    public PatientResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                            Party customer, IConstraint constraints, SortConstraint[] sort, int rows) {
        super(archetypes, value, searchIdentities, constraints, sort,
              rows, true, new ObjectSetQueryExecutor());
        archetypes.setAlias("patient");
        this.customer = customer;
    }

    /**
     * Determines if all patients are being queried.
     *
     * @return {@code true} if all patients are being queried, or {@code false} if only the patients for the
     *         specified customer are being returned.
     */
    public boolean isSearchingAllPatients() {
        return customer == null;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = super.createQuery();
        query.add(new ObjectSelectConstraint("patient"));

        if (customer != null) {
            Date now = new Date();
            query.add(Constraints.join("customers", Constraints.shortName("rel", "entityRelationship.patientOwner")));
            query.add(Constraints.eq("rel.source", customer.getObjectReference()));
            if (getArchetypes().isActiveOnly()) {
                query.add(Constraints.lte("rel.activeStartTime", now));
                query.add(Constraints.or(Constraints.gte("rel.activeEndTime", now),
                                         Constraints.isNull("rel.activeEndTime")));
            }
        }

        if (isSearchingIdentities()) {
            query.add(new ObjectSelectConstraint("identity"));
        }
        return query;
    }

}