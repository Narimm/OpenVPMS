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

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.ObjectSetResultSetAdapter;
import org.openvpms.web.component.im.query.QueryAdapter;
import org.openvpms.web.component.im.query.ResultSet;


/**
 * Query implementation that queries patients. The search can be further
 * constrained to only include those patients associated with the current
 * customer.
 *
 * @author Tim Anderson
 */
public class PatientByCustomerQuery extends QueryAdapter<ObjectSet, Party> {

    /**
     * Constructs a {@link PatientByCustomerQuery}.
     *
     * @param shortNames the short names
     * @param context    the context
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public PatientByCustomerQuery(String[] shortNames, Context context) {
        this(shortNames, context.getCustomer());
    }

    /**
     * Constructs a {@link PatientByCustomerQuery}.
     *
     * @param shortNames the short names
     * @param customer   the customer. May be {@code null}
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public PatientByCustomerQuery(String[] shortNames, Party customer) {
        super(new PatientByCustomerObjectSetQuery(shortNames, customer), Party.class);
    }

    /**
     * Determines if patients must be active.
     *
     * @param active if {@code true} only query active objects, otherwise query both active and inactive objects
     */
    public void setActiveOnly(boolean active) {
        ((PatientByCustomerObjectSetQuery) getQuery()).setActiveOnly(active);
    }

    /**
     * Converts a result set.
     *
     * @param set the set to convert
     * @return the converted set
     */
    protected ResultSet<Party> convert(ResultSet<ObjectSet> set) {
        return new ObjectSetResultSetAdapter<>(set, "patient", Party.class);
    }

}
