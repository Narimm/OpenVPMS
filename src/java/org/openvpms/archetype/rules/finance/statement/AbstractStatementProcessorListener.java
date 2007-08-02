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

package org.openvpms.archetype.rules.finance.statement;

import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQueryException;

import java.util.Date;


/**
 * Abstract implementation of the {@link ProcessorListener} interface for
 * statement processing.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractStatementProcessorListener
        implements ProcessorListener<StatementEvent> {

    /**
     * The query helper.
     */
    private final StatementActHelper acts;


    /**
     * Creates a new <tt>AbstractStatementProcessorLister</tt>.
     *
     * @param service the archetype service
     */
    public AbstractStatementProcessorListener(IArchetypeService service) {
        acts = new StatementActHelper(service);
    }

    /**
     * Helper to get all account act types between the opening balance prior to
     * the specified date, and the corresponding closing balance.
     *
     * @param customer the customer
     * @param date     the date
     * @return a new query
     * @throws ArchetypeServiceException for any archetype service error
     * @throws ArchetypeQueryException   for any archetype query error
     */
    protected Iterable<Act> getActs(Party customer, Date date) {
        return acts.getActs(customer, date);
    }

    protected Iterable<Act> getActsWithAccountFees(Party customer,
                                                   Date date) {
        return acts.getActsWithAccountFees(customer, date);
    }
}
