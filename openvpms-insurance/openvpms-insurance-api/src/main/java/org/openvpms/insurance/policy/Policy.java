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

package org.openvpms.insurance.policy;

import org.openvpms.component.model.party.Party;
import org.openvpms.insurance.exception.InsuranceException;

import java.util.Date;

/**
 * Animal insurance policy.
 *
 * @author Tim Anderson
 */
public interface Policy {

    /**
     * Returns the OpenVPMS identifier for this policy.
     *
     * @return the identifier
     */
    long getId();

    /**
     * Returns the policy identifier, issued by the insurer.
     *
     * @return the policy identifier
     * @throws InsuranceException for any error
     */
    String getInsurerId();

    /**
     * Returns the date when the policy expires.
     *
     * @return the policy expiry date
     * @throws InsuranceException for any error
     */
    Date getExpiryDate();

    /**
     * Returns the policy holder.
     *
     * @return the policy holder
     * @throws InsuranceException for any error
     */
    PolicyHolder getPolicyHolder();

    /**
     * Returns the animal that the policy applies to.
     *
     * @return the animal
     * @throws InsuranceException for any error
     */
    Animal getAnimal();

    /**
     * Returns the insurer that issued the policy.
     *
     * @return insurer that issued the policy
     * @throws InsuranceException for any error
     */
    Party getInsurer();

}
