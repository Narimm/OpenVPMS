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

package org.openvpms.domain.internal.party;

import org.openvpms.component.business.domain.im.party.ContactDecorator;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.party.Contact;
import org.openvpms.domain.party.Email;

/**
 * Default implementation of {@link Email}.
 *
 * @author Tim Anderson
 */
public class EmailImpl extends ContactDecorator implements Email {

    /**
     * The bean.
     */
    private final IMObjectBean bean;

    /**
     * Constructs a {@link ContactDecorator}.
     *
     * @param peer the peer to delegate to
     */
    public EmailImpl(Contact peer, IArchetypeService service) {
        super(peer);
        bean = service.getBean(peer);
    }

    /**
     * Returns the email address.
     *
     * @return the email address. May be {@code null}
     */
    @Override
    public String getEmailAddress() {
        return bean.getString("emailAddress");
    }
}
