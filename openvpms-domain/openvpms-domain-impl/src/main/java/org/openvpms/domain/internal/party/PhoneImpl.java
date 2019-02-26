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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.party.ContactDecorator;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.party.Contact;
import org.openvpms.domain.party.Phone;

/**
 * Default implementation of {@link Phone}.
 *
 * @author Tim Anderson
 */
public class PhoneImpl extends ContactDecorator implements Phone {

    /**
     * The bean.
     */
    private final IMObjectBean bean;

    /**
     * Constructs a {@link PhoneImpl}.
     *
     * @param peer    the peer to delegate to
     * @param service the archetype service
     */
    public PhoneImpl(Contact peer, IArchetypeService service) {
        super(peer);
        bean = service.getBean(peer);
    }

    /**
     * Returns the phone number.
     *
     * @return the phone number. May be {@code null}
     */
    @Override
    public String getPhoneNumber() {
        String result = null;
        String areaCode = bean.getString("areaCode");
        String phone = bean.getString("telephoneNumber");
        if (!StringUtils.isEmpty(phone)) {
            if (!StringUtils.isEmpty(areaCode)) {
                result = "(" + areaCode + ") " + phone;
            } else {
                result = phone;
            }
        }
        return result;
    }

    /**
     * Determines if this is the preferred phone contact.
     *
     * @return {@code true} if this is the preferred phone contact
     */
    @Override
    public boolean isPreferred() {
        return bean.getBoolean("preferred");
    }
}
