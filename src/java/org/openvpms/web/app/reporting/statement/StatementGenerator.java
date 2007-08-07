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

package org.openvpms.web.app.reporting.statement;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.finance.account.CustomerBalanceSummaryQuery;
import org.openvpms.archetype.rules.finance.statement.StatementEvent;
import org.openvpms.archetype.rules.finance.statement.StatementProcessor;
import org.openvpms.archetype.rules.finance.statement.StatementProcessorException;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Statement generator.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class StatementGenerator extends AbstractStatementGenerator {

    /**
     * The statement processor progress bar.
     */
    private StatementProgressBarProcessor progressBarProcessor;


    /**
     * Constructs a new <tt>StatementGenerator</tt> for a single customer.
     *
     * @param customer the customer reference
     * @param date     the statement date
     * @param context  the context
     */
    public StatementGenerator(IMObjectReference customer, Date date,
                              Context context) {
        super(Messages.get("reporting.statements.run.title"),
              Messages.get("reporting.statements.run.cancel.title"),
              Messages.get("reporting.statements.run.cancel.message"));
        List<Party> customers = new ArrayList<Party>();
        Party party = (Party) IMObjectHelper.getObject(customer);
        if (party != null) {
            customers.add(party);
        }
        init(customers, date, context);
    }

    /**
     * Constructs a new <tt>StatementGenerator</tt> for statements returned by a
     * query.
     *
     * @param query   the query
     * @param context the context
     */
    public StatementGenerator(CustomerBalanceQuery query, Context context) {
        super(Messages.get("reporting.statements.run.title"),
              Messages.get("reporting.statements.run.cancel.title"),
              Messages.get("reporting.statements.run.cancel.message"));
        List<ObjectSet> balances = query.getObjects();
        List<Party> customers = new ArrayList<Party>();
        for (ObjectSet set : balances) {
            IMObjectReference ref = (IMObjectReference) set.get(
                    CustomerBalanceSummaryQuery.CUSTOMER_REFERENCE);
            Party customer = (Party) IMObjectHelper.getObject(ref);
            if (customer != null) {
                customers.add(customer);
            }
            init(customers, query.getDate(), context);
        }
    }

    /**
     * Returns the processor.
     *
     * @return the processor
     */
    protected StatementProgressBarProcessor getProcessor() {
        return progressBarProcessor;
    }

    /**
     * Initialises this.
     *
     * @param customers the customers to generate statements for
     * @param date      the statement date
     * @param context   the context
     * @throws ArchetypeServiceException   for any archetype service error
     * @throws StatementProcessorException for any statement processor exception
     */
    private void init(List<Party> customers, Date date, Context context) {
        Party practice = context.getPractice();
        if (practice == null) {
            throw new StatementProcessorException(
                    StatementProcessorException.ErrorCode.InvalidConfiguration,
                    "Context has no practice");
        }
        Contact email = getEmail(practice);
        if (email == null) {
            throw new StatementProcessorException(
                    StatementProcessorException.ErrorCode.InvalidConfiguration,
                    "Practice " + practice.getName()
                            + " has no email contact for statements");
        }
        IMObjectBean bean = new IMObjectBean(email);
        String address = bean.getString("emailAddress");
        String name = practice.getName();
        if (StringUtils.isEmpty(address)) {
            throw new StatementProcessorException(
                    StatementProcessorException.ErrorCode.InvalidConfiguration,
                    "Practice " + practice.getName()
                            + " email contact address is empty");
        }

        StatementProcessor processor = new StatementProcessor(date);
        progressBarProcessor = new StatementProgressBarProcessor(
                processor, customers);

        StatementPrintProcessor printer
                = new StatementPrintProcessor(progressBarProcessor,
                                              getCancelListener());

        StatementEmailProcessor emailer = new StatementEmailProcessor(
                ServiceHelper.getMailSender(), address, name);

        processor.addListener(StatementEvent.Action.PRINT, printer);
        processor.addListener(StatementEvent.Action.EMAIL, emailer);
    }

    /**
     * Returns an email contact for the practice.
     *
     * @param practice the practice
     * @return an email contact, or <tt>null</tt> if none is configured
     */
    private Contact getEmail(Party practice) {
        Contact preferred = null;
        Contact fallback = null;
        for (Contact contact : practice.getContacts()) {
            if (TypeHelper.isA(contact, "contact.email")) {
                IMObjectBean bean = new IMObjectBean(contact);
                List<Lookup> purposes = bean.getValues("purposes",
                                                       Lookup.class);
                for (Lookup purpose : purposes) {
                    if ("BILLING".equals(purpose.getCode())) {
                        return contact;
                    }
                }
                if (preferred == null && bean.hasNode("preferred")
                        && bean.getBoolean("preferred")) {
                    preferred = contact;
                } else if (fallback == null) {
                    fallback = contact;
                }
            }
        }
        return (preferred != null) ?
                preferred : (fallback != null) ? fallback : null;
    }

}
