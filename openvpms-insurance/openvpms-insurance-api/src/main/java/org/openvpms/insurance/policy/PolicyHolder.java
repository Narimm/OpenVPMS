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

import org.openvpms.component.model.party.Contact;

/**
 * Insurance policy holder.
 *
 * @author Tim Anderson
 */
public interface PolicyHolder {

    /**
     * Returns the OpenVPMS identifier for the policy holder.
     *
     * @return the OpenVPMS identifier for the policy holder
     */
    long getId();

    /**
     * Returns the policy holder name.
     *
     * @return the policy holder name
     */
    String getName();

    /**
     * Returns the policy holder's address.
     *
     * @return the policy holder's address. May be {@code null}
     */
    Contact getAddress();

    /**
     * Returns the policy holder's daytime telephone.
     *
     * @return the daytime telephone. May be {@code null}
     */
    Contact getDaytimePhone();

    /**
     * Returns the policy holder's evening telephone.
     *
     * @return the evening telephone. May be {@code null}
     */
    Contact getEveningPhone();

    /**
     * Returns the policy holder's mobile telephone.
     *
     * @return the mobile telephone. May be {@code null}
     */
    Contact getMobilePhone();

    /**
     * Returns the policy holder's email address.
     *
     * @return the email address. May be {@code null}
     */
    Contact getEmail();

}
