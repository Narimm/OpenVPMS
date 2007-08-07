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

import org.openvpms.component.business.domain.im.act.Act;
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
     * The statement date.
     */
    private final Date date;

    /**
     * The statement acts. This represents all acts after the last statement
     * period, and prior to the statement date. May be <tt>null</tt>
     */
    private final Iterable<Act> acts;


    /**
     * Constructs a new <tt>StatementEvent</tt>.
     *
     * @param action   the statement action
     * @param customer the customer
     * @param contact  the customer contact
     * @param date     the statement date
     * @param acts     all statement acts after the last statement period, and
     *                 prior to the statement date. May be <tt>null</tt>
     */
    public StatementEvent(Action action, Party customer, Contact contact,
                          Date date, Iterable<Act> acts) {
        this.action = action;
        this.customer = customer;
        this.contact = contact;
        this.date = date;
        this.acts = acts;
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
     * Returns the statement date.
     *
     * @return the statement date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns the statement acts.
     * This represents all acts after the last statement period, and prior to
     * the statement date.
     *
     * @return the statement acts.
     */
    public Iterable<Act> getActs() {
        return acts;
    }

}
