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

import org.openvpms.archetype.component.processor.Processor;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.math.BigDecimal;
import java.util.Date;


/**
 * End-of-period statement processor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EndOfPeriodProcessor implements Processor<Party> {

    private final Date date;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Statement act query helper.
     */
    private final StatementActHelper acts;

    /**
     * Customer account rules.
     */
    private final CustomerAccountRules account;

    /**
     * Statement rules.
     */
    private final StatementRules statement;


    /**
     * Creates a new <tt>EndOfPeriodProcessor</tt>.
     *
     * @param date    the date
     * @param service the archetype service
     */
    public EndOfPeriodProcessor(Date date, IArchetypeService service) {
        this.date = date;
        this.service = service;
        acts = new StatementActHelper(service);
        account = new CustomerAccountRules(service);
        statement = new StatementRules(service);
    }

    /**
     * Process a customoer.
     *
     * @param customer the customer to process
     * @throws OpenVPMSException for any error
     */
    public void process(Party customer) {
        for (Act act : acts.getCompletedActs(customer, date)) {
            act.setStatus(ActStatus.POSTED);
            service.save(act);
        }
        BigDecimal fee = statement.getAccountFee(customer, date);
        if (fee.compareTo(BigDecimal.ZERO) != 0) {
            statement.applyAccountingFee(customer, fee, date);
        }
        account.createPeriodEnd(customer, date);
    }
}
