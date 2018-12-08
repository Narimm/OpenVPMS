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

package org.openvpms.web.component.im.patient;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.ObjectSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityResultSet;
import org.openvpms.web.component.im.query.ObjectSetQueryExecutor;

import java.util.Date;

import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.leftJoin;
import static org.openvpms.component.system.common.query.Constraints.shortName;


/**
 * An {@link org.openvpms.web.component.im.query.ResultSet} implementation that queries patients. The search can be
 * further constrained to match on:
 * <ul>
 * <li>partial patient name or identity; and/or
 * <li>partial customer name; and/or
 * <li>partial contact
 * </ul>
 * <p>
 * The returned {@link org.openvpms.component.system.common.query.ObjectSet}s contain:
 * <ul>
 * <li>the patient:
 * <pre>Party patient = (Party) set.get("patient");</pre>
 * <li>the customer:
 * <pre>Party customer = (Party) set.get("customer");</pre>
 * </li>
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
    private Reference customer;

    /**
     * The customer value. May be a customer identifier, or name.
     */
    private String customerValue;

    /**
     * The contact value.
     */
    private String contactValue;

    /**
     * Constructs a {@link PatientResultSet}.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be {@code null}
     * @param searchIdentities if {@code true} search on identity name
     * @param customer         if specified, only return patients for the specified customer
     * @param sort             the sort criteria. May be {@code null}
     * @param rows             the maximum no. of rows per page
     */
    public PatientResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                            Reference customer, IConstraint constraints, SortConstraint[] sort, int rows) {
        super(archetypes, value, searchIdentities, constraints, sort,
              rows, false, new ObjectSetQueryExecutor());
        // can't use distinct as selecting both patient and customer, which cause count() to fail
        archetypes.setAlias("patient");
        this.customer = customer;
    }

    /**
     * Constructs a {@link PatientResultSet}.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be {@code null}
     * @param searchIdentities if {@code true} search on identity name
     * @param customer         if specified, only return patients for the specified customer
     * @param customerValue    if specified, only return patients for the specified customer names. Should be
     *                         {@code null} if {@code customer} is supplied
     * @param contactValue     if specified, only return patients for the specified contact(s)
     * @param sort             the sort criteria. May be {@code null}
     * @param rows             the maximum no. of rows per page
     */
    public PatientResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                            Reference customer, String customerValue, String contactValue, IConstraint constraints,
                            SortConstraint[] sort, int rows) {
        super(archetypes, value, searchIdentities, constraints, sort, rows, true, new ObjectSetQueryExecutor());
        archetypes.setAlias("patient");
        this.customer = customer;
        this.customerValue = customerValue;
        this.contactValue = contactValue;
    }

    /**
     * Determines if all patients are being queried.
     *
     * @return {@code true} if all patients are being queried, or {@code false} if only the patients for the
     * specified customer are being returned.
     */
    public boolean isSearchingAllPatients() {
        return customer == null;
    }

    /**
     * Determines if customer contact details are being searched on.
     *
     * @return {@code true} if customer contact details are being searched on
     */
    public boolean isSearchingByContact() {
        return !StringUtils.isEmpty(contactValue);
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
        query.add(new ObjectSelectConstraint("customer"));

        boolean customerDetails = customer != null || !StringUtils.isEmpty(customerValue) || isSearchingByContact();
        Date now = new Date();
        ShortNameConstraint owner = shortName("rel", "entityRelationship.patientOwner");
        JoinConstraint customers = (customerDetails) ? join("customers", owner) : leftJoin("customers", owner);
        JoinConstraint source = (customerDetails) ? join("source", "customer") : leftJoin("source", "customer");
        customers.add(source);
        query.add(customers);
        if (customerDetails) {
            if (customer != null) {
                query.add(Constraints.eq("rel.source", customer));
            } else if (!StringUtils.isEmpty(customerValue)) {
                Long id = getId(customerValue);
                if (id != null) {
                    source.add(Constraints.eq("id", id));
                } else {
                    source.add(Constraints.eq("name", customerValue));
                }
            }
            if (!StringUtils.isEmpty(contactValue)) {
                source.add(Constraints.join("contacts", "contact").add(Constraints.eq("description", contactValue)));
                query.add(new ObjectSelectConstraint("contact"));
            }
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