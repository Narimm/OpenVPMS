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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.workspace.history;

import org.openvpms.component.business.domain.im.party.Party;

import java.util.Date;


/**
 * Customer/patient pair, used to track customer/patient selections.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerPatient {

    /**
     * The customer. May be <tt>null</tt>
     */
    private Party customer;

    /**
     * The patient. May be <tt>null</tt>
     */
    private Party patient;

    /**
     * The time when the selection occurred.
     */
    private Date selected;


    /**
     * Creates a new <tt>CustomerPatient</tt>.
     *
     * @param customer the customer. May be <tt>null</tt>
     * @param patient  the patient. May be <tt>null</tt>
     * @param selected the time when the selection occurred
     */
    public CustomerPatient(Party customer, Party patient, Date selected) {
        this.customer = customer;
        this.patient = patient;
        this.selected = selected;
    }

    /**
     * Returns the customer.
     *
     * @return the customer. May be <tt>null</tt>
     */
    public Party getCustomer() {
        return customer;
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be <tt>null</tt>
     */
    public Party getPatient() {
        return patient;
    }

    /**
     * Returns the time when the selection occurred.
     *
     * @return the time
     */
    public Date getSelected() {
        return selected;
    }

}
