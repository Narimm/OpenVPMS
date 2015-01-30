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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractArchetypeQuery;
import org.openvpms.web.component.im.query.DefaultResultSet;
import org.openvpms.web.component.im.query.ResultSet;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.sort;

/**
 * Queries email contacts for parties.
 * <p/>
 * Allows contacts to be filtered by party type and name.
 *
 * @author Tim Anderson
 */
class EmailQuery extends AbstractArchetypeQuery<Contact> {

    /**
     * The archetypes to query.
     */
    private static final String[] PARTY_SHORT_NAMES = {
            CustomerArchetypes.PERSON, PracticeArchetypes.PRACTICE, PracticeArchetypes.LOCATION,
            SupplierArchetypes.SUPPLIER_ORGANISATION, SupplierArchetypes.SUPPLIER_PERSON,
            SupplierArchetypes.MANUFACTURER, SupplierArchetypes.SUPPLIER_VET, SupplierArchetypes.SUPPLIER_VET_PRACTICE,
            UserArchetypes.USER};

    /**
     * The default sort construct.
     */
    private static final SortConstraint[] DEFAULT_SORT = new SortConstraint[]{sort("party", "name"),
                                                                              sort("party", "id")};


    /**
     * Constructs an {@link EmailQuery}.
     */
    public EmailQuery() {
        super(PARTY_SHORT_NAMES, false, IMObject.class);
        setAuto(true);
        setDefaultSortConstraint(DEFAULT_SORT);
    }

    /**
     * Invoked when the short name is selected.
     */
    @Override
    protected void onShortNameChanged() {
        onQuery();
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Contact> createResultSet(SortConstraint[] sort) {
        if (sort == null) {
            sort = getDefaultSortConstraint();
        }
        ShortNameConstraint contact = Constraints.shortName("contact", ContactArchetypes.EMAIL);
        ShortNameConstraint parties = Constraints.shortName("party", getArchetypeConstraint().getShortNames());
        parties.add(Constraints.join("contacts", "c").add(Constraints.idEq("contact", "c")));
        contact.add(parties);
        return new DefaultResultSet<Contact>(contact, getValue(), null, sort, getMaxResults(), isDistinct()) {
            @Override
            protected List<IConstraint> createValueConstraints(String value, List<String> nodes) {
                List<IConstraint> constraints = new ArrayList<IConstraint>();
                if (!StringUtils.isEmpty(value)) {
                    constraints.add(Constraints.eq("party.name", value));
                }
                return constraints;
            }
        };
    }

}
