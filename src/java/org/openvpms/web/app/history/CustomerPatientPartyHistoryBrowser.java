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
package org.openvpms.web.app.history;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.BrowserAdapter;


/**
 * Helper to adapt <tt>CustomerPatient</tt> results from a CustomerPatientHistoryBrowser to <tt>Party</tt>
 * instances.
 * <p/>
 * This always returns the patient from an {@link CustomerPatient} if one is present. If not, it returns the customer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerPatientPartyHistoryBrowser extends BrowserAdapter<CustomerPatient, Party> {

    /**
     * Construct a new <code>TableBrowser</code> that queries objects using the
     * specified query, displaying them in the table.
     */
    public CustomerPatientPartyHistoryBrowser() {
        setBrowser(new CustomerPatientHistoryBrowser());
    }

    /**
     * Returns the underlying browser.
     *
     * @return the underlying browser
     */
    @Override
    public CustomerPatientHistoryBrowser getBrowser() {
        return (CustomerPatientHistoryBrowser) super.getBrowser();
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been selected
     */
    @Override
    public Party getSelected() {
        return getBrowser().getSelectedParty();
    }

    /**
     * Converts an object.
     *
     * @param object the object to convert
     * @return the converted object
     */
    protected Party convert(CustomerPatient object) {
        return object.getPatient() != null ? object.getPatient() : object.getCustomer();
    }

}
