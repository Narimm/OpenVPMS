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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.customer.document;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;

/**
 * Queries <em>act.customerDocumentForm</em>, <em>act.customerDocumentLetter</em> and
 * <em>act.customerDocumentAttachment</em> acts for a customer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerDocumentQuery<T extends Act> extends DateRangeActQuery<T> {

    /**
     * Customer document shortnames.
     */
    public static final String[] SHORT_NAMES = {"act.customerDocumentForm", "act.customerDocumentLetter",
                                                "act.customerDocumentAttachment"};

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES;

    static {
        STATUSES = new ActStatuses("act.customerDocumentLetter");
        STATUSES.setDefault((String) null);
    }

    /**
     * Cosntructs a <tt>CustomerDocumentQuery</tt>.
     *
     * @param customer the customer
     */
    public CustomerDocumentQuery(Party customer) {
        super(customer, "customer", "participation.customer", SHORT_NAMES, STATUSES, DocumentAct.class);
    }

}
