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
import java.util.List;


/**
 * Customer statement.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Statement {

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * The preferred contacts.
     */
    private final List<Contact> contacts;

    /**
     * The statement date.
     */
    private final Date statementDate;

    /**
     * The statement acts. This represents all acts between the opening balance
     * and closing balance timestamps.
     */
    private final Iterable<Act> acts;

    /**
     * The opening balance timestamp. May be <tt>null</tt>
     */
    private final Date openingBalanceTimestamp;

    /**
     * The closing balance timestamp. May be <tt>null</tt>
     */
    private final Date closingBalanceTimestamp;

    /**
     * Determines if the statement has been printed.
     */
    private final boolean printed;


    /**
     * Constructs a new <tt>Statement</tt>.
     *
     * @param customer                the customer
     * @param contacts                the preferred contacts
     * @param statementDate           the statement date
     * @param openingBalanceTimestamp the opening balance timestamp.
     *                                May be <tt>null</tt>
     * @param closingBalanceTimestamp the closing balance timestamp.
     *                                May be <tt>null</tt>
     * @param acts                    all statement for the statement period
     * @param printed                 determines if the statement has already
     *                                been printed
     */
    public Statement(Party customer, List<Contact> contacts,
                     Date statementDate, Date openingBalanceTimestamp,
                     Date closingBalanceTimestamp, Iterable<Act> acts,
                     boolean printed) {
        this.customer = customer;
        this.contacts = contacts;
        this.statementDate = statementDate;
        this.openingBalanceTimestamp = openingBalanceTimestamp;
        this.closingBalanceTimestamp = closingBalanceTimestamp;
        this.acts = acts;
        this.printed = printed;
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
     * Returns the preferred customer contacts for the statement.
     *
     * @return the contacts. May be empty if the customer has no contacts
     */
    public List<Contact> getContacts() {
        return contacts;
    }

    /**
     * Returns the statement date.
     *
     * @return the statement date
     */
    public Date getStatementDate() {
        return statementDate;
    }

    /**
     * Returns the opening balance timestamp. This is the startTime of the
     * <em>act.customerAccountOpeningBalance</em> act for the customer,
     * prior to the statement date.
     *
     * @return the opening balance timestamp. If <tt>null</tt> there is no
     *         <em>act.customerAccountOpeningBalance<em> act prior to the
     *         statement date
     */
    public Date getOpeningBalanceTimestamp() {
        return openingBalanceTimestamp;
    }

    /**
     * Returns the closing balance timestamp. This is the startTime of the
     * <em>act.customerAccountClosingBalance</em> act for the customer,
     * after the opening balance timestamp, or the statement date if there is
     * no opening balance.
     *
     * @return the opening balance timestamp. If <tt>null</tt> there is no
     *         <em>act.customerAccountClosingBalance<em> act after the opening
     *         balance/statement date
     */
    public Date getClosingBalanceTimestamp() {
        return closingBalanceTimestamp;
    }

    /**
     * Determines if this is a preview statement or an official statement.
     * A preview statement is one for which no end-of-period has yet been
     * run, i.e there is no closing balance.
     *
     * @return <tt>true</tt> if this ia a preview statement
     */
    public boolean isPreview() {
        return (closingBalanceTimestamp == null);
    }

    /**
     * Determines if the statement has been printed previously.
     *
     * @return <tt>true</tt> if the statement has been printed
     */
    public boolean isPrinted() {
        return printed;
    }

    /**
     * Returns the statement acts for the statement period.
     * This represents all acts between the opening and closing balance,
     * inclusive. If this is a preview statement, a dummy
     * <em>act.customerAccountDebitAdjust<em> containg any accounting fees
     * may be included.
     *
     * @return the statement acts.
     */
    public Iterable<Act> getActs() {
        return acts;
    }

}
