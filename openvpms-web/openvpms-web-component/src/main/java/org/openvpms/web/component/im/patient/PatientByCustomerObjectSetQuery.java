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

import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.customer.CustomerPatientObjectSetQuery;
import org.openvpms.web.component.im.query.ResultSet;

/**
 * Searches patients by customer.
 *
 * @author Tim Anderson
 */
public class PatientByCustomerObjectSetQuery extends CustomerPatientObjectSetQuery {

    /**
     * The default sort constraint.
     */
    protected static final SortConstraint[] DEFAULT_SORT = {new NodeSortConstraint("customer", "name"),
                                                            new NodeSortConstraint("customer", "id"),
                                                            new NodeSortConstraint("patient", "name"),
                                                            new NodeSortConstraint("patient", "id")};

    /**
     * Constructs a {@link PatientByCustomerObjectSetQuery}.
     *
     * @param shortNames the short names
     * @param customer   the customer. May be {@code null}
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public PatientByCustomerObjectSetQuery(String[] shortNames, Party customer) {
        super(shortNames, false, customer);
        setDefaultSortConstraint(DEFAULT_SORT);
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return {@code true} if the object reference is selected by the query
     */
    @Override
    public boolean selects(IMObjectReference reference) {
        PatientResultSet set = (PatientResultSet) createResultSet(null);
        set.setReferenceConstraint(reference);
        return set.hasNext();
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        String patientWildcard = getWildcardedText(getPatient());
        String contactWildcard = getWildcardedText(getContact(), true);

        return new PatientResultSet(getArchetypeConstraint(), patientWildcard, isIdentitySearch(), getCustomer(),
                                    getValue(), contactWildcard, getConstraints(), sort, getMaxResults());
    }

}
