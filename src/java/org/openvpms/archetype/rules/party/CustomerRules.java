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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.party;

import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Rules for <em>party.customer*</em> objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerRules extends PartyRules {

    /**
     * Creates a new <tt>CustomerRules</tt>.
     */
    public CustomerRules() {
    }

    /**
     * Creates a new <tt>CustomerRules</tt>.
     *
     * @param service the archetype service
     */
    public CustomerRules(IArchetypeService service) {
        super(service);
    }

    /**
     * Returns the <em>lookup.customerAccountType</em> for a customer.
     *
     * @param party the party
     * @return the account type, or <tt>null</tt> if one doesn't exist
     */
    public Lookup getAccountType(Party party) {
        Lookup result = null;
        IMObjectBean bean = new IMObjectBean(party, getArchetypeService());
        if (bean.hasNode("type")) {
            List<Lookup> types = bean.getValues("type", Lookup.class);
            result = (types.isEmpty()) ? null : types.get(0);
        }
        return result;
    }

    /**
     * Returns the <em>lookup.customerAlertType</em> for a customer.
     * <p/>
     * This includes any alert associated with their account.
     *
     * @param customer the customer
     * @return a list of alerts associated with the customer
     */
    public Set<Lookup> getAlerts(Party customer) {
        Set<Lookup> result = new HashSet<Lookup>();
        Lookup accountTypeLookup = getAccountType(customer);
        if (accountTypeLookup != null) {
            AccountType accountType = new AccountType(accountTypeLookup, getArchetypeService());
            Lookup alert = accountType.getAlert();
            if (alert != null) {
                result.add(alert);
            }
        }
        IMObjectBean bean = new IMObjectBean(customer, getArchetypeService());
        if (bean.hasNode("alerts")) {
            List<Lookup> alerts = bean.getValues("alerts", Lookup.class);
            result.addAll(alerts);
        }
        return result;
    }

    /**
     * Merges two customers.
     *
     * @param from the customer to merge
     * @param to   the customer to merge to
     * @throws MergeException            if the customers cannot be merged
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void mergeCustomers(Party from, Party to) {
        CustomerMerger merger = new CustomerMerger(getArchetypeService());
        merger.merge(from, to);
    }

}
