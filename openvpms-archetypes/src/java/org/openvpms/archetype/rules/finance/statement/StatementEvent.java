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

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.Date;


/**
 * Statement event.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StatementEvent {

    public enum Action {
        EMAIL, PRINT
    }

    /**
     * The statement processor that generated the event.
     */
    private final StatementProcessor processor;

    /**
     * The statement action.
     */
    private final Action action;

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The contact.
     */
    private final Contact contact;

    /**
     * The processing date.
     */
    private final Date date;


    /**
     * Constructs a new <tt>StatementEvent</tt>.
     *
     * @param processor the processor
     * @param action    the statement action
     * @param customer  the customer
     * @param contact   the customer contact
     * @param date      the processing date
     */
    public StatementEvent(StatementProcessor processor, Action action,
                          Party customer, Contact contact, Date date) {
        this.processor = processor;
        this.action = action;
        this.customer = customer;
        this.contact = contact;
        this.date = date;
    }

    /**
     * Returns the processor that generated the event.
     *
     * @return the processor that generated the event
     */
    public StatementProcessor getProcessor() {
        return processor;
    }

    /**
     * Returns the statement action.
     *
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the customer.
     *
     * @return the customer
     */
    public Party getCustomer() {
        return customer;
    }

    /**
     * Returns the contact.
     *
     * @return the contact.
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Returns the processing date.
     *
     * @return the processing date
     */
    public Date getDate() {
        return date;
    }

}
