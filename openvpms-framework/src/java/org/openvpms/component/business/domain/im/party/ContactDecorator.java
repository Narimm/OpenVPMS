/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.domain.im.party;

import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.party.Contact;
import org.openvpms.component.business.domain.im.common.IMObjectDecorator;

import java.util.Date;
import java.util.Set;

/**
 * Decorator for {@link Contact}.
 *
 * @author Tim Anderson
 */
public class ContactDecorator extends IMObjectDecorator implements Contact {

    /**
     * Constructs a {@link ContactDecorator}.
     *
     * @param peer the peer to delegate to
     */
    public ContactDecorator(Contact peer) {
        super(peer);
    }

    /**
     * Returns the time when the contact is active from.
     *
     * @return the start time. May be {@code null}.
     */
    @Override
    public Date getActiveStartTime() {
        return getPeer().getActiveStartTime();
    }

    /**
     * Sets the time when the contact is active from.
     *
     * @param time the start time. May be {@code null}.
     */
    @Override
    public void setActiveStartTime(Date time) {
        getPeer().setActiveStartTime(time);
    }

    /**
     * Returns the time when the contact is active to.
     *
     * @return the end time. May be {@code null}.
     */
    @Override
    public Date getActiveEndTime() {
        return getPeer().getActiveEndTime();
    }

    /**
     * Sets the time when the contact is active to.
     *
     * @param time the end time. May be {@code null}.
     */
    @Override
    public void setActiveEndTime(Date time) {
        getPeer().setActiveEndTime(time);
    }

    /**
     * Returns the classifications for this contact.
     *
     * @return the classifications
     */
    @Override
    public Set<Lookup> getClassifications() {
        return getPeer().getClassifications();
    }

    /**
     * Adds a classification.
     *
     * @param classification the classification to add
     */
    @Override
    public void addClassification(Lookup classification) {
        getPeer().addClassification(classification);
    }

    /**
     * Removes a classification.
     *
     * @param classification the classification to remove
     */
    @Override
    public void removeClassification(Lookup classification) {
        getPeer().removeClassification(classification);
    }

    /**
     * Returns the peer.
     *
     * @return the peer
     */
    @Override
    protected Contact getPeer() {
        return (Contact) super.getPeer();
    }
}
