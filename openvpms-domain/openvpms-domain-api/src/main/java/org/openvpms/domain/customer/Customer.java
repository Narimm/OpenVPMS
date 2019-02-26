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

package org.openvpms.domain.customer;

import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.domain.party.ContactableParty;
import org.openvpms.domain.party.Phone;

/**
 * Represents a customer.
 *
 * @author Tim Anderson
 */
public interface Customer extends ContactableParty {

    /**
     * Returns the person's title name.
     *
     * @return the title name. May be {@code null}
     */
    String getTitleName();

    /**
     * Returns the person's title code.
     *
     * @return the title code. May be {@code null}
     */
    String getTitleCode();

    /**
     * Returns the title lookup.
     *
     * @return the title lookup. May be {@code null}
     */
    Lookup getTitleLookup();

    /**
     * Returns the person's first name.
     *
     * @return the first name. May be {@code null}
     */
    String getFirstName();

    /**
     * Returns the person's last name.
     *
     * @return the last name. May be {@code null}
     */
    String getLastName();

    /**
     * Returns the person's full name.
     *
     * @return the full name. May be {@code null}
     */
    String getFullName();

    /**
     * Returns the company name, if this customer is a representative of a company.
     *
     * @return the company name. May be {@code null}
     */
    String getCompanyName();

    /**
     * Returns the work telephone.
     *
     * @return the work telephone. May be {@code null}
     */
    Phone getWorkPhone();

    /**
     * Returns the home telephone.
     *
     * @return the home telephone. May be {@code null}
     */
    Phone getHomePhone();

    /**
     * Returns the mobile telephone.
     *
     * @return the mobile telephone. May be {@code null}
     */
    Phone getMobilePhone();

}
