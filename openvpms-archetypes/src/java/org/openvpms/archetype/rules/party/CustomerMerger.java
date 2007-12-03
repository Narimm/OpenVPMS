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

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * Merges two <em>party.customerperson</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class CustomerMerger extends PartyMerger {

    /**
     * Customer rules.
     */
    private final CustomerRules rules;


    /**
     * Creates a new <tt>CustomerMerger</tt>.
     */
    public CustomerMerger() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>CustomerMerger</tt>.
     *
     * @param service the archetype service
     */
    public CustomerMerger(IArchetypeService service) {
        super("party.customerperson", service);
        rules = new CustomerRules(service);
    }

    /**
     * Copies classifications from one party to another.
     * This ensures that only one <em>lookup.customerAccountType</em> appears in
     * the 'to' party, to avoid cardinality violations. If both parties have a
     * <em>lookup.customerAccountType</em>, the 'to' party's type takes
     * precedence.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     */
    @Override
    protected void copyClassifications(Party from, Party to) {
        for (Lookup lookup : from.getClassifications()) {
            if (!TypeHelper.isA(lookup, "lookup.customerAccountType")) {
                to.addClassification(lookup);
            }
        }
        Lookup accountType = rules.getAccountType(to);
        if (accountType == null) {
            accountType = rules.getAccountType(from);
            if (accountType != null) {
                to.addClassification(accountType);
            }
        }
    }

}
