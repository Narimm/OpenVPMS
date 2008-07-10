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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.party;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityAssembler;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PartyAssembler extends EntityAssembler<Party, PartyDO> {

    private SetAssembler<Contact, ContactDO> CONTACTS
            = SetAssembler.create(Contact.class, ContactDO.class);

    public PartyAssembler() {
        super(Party.class, PartyDO.class);
    }

    @Override
    protected void assembleDO(PartyDO target, Party source,
                                 DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        CONTACTS.assembleDO(target.getContacts(), source.getContacts(),
                            state, context);
    }

    @Override
    protected void assembleObject(Party target, PartyDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        CONTACTS.assembleObject(target.getContacts(), source.getContacts(),
                                context);
    }

    protected Party create(PartyDO object) {
        return new Party();
    }

    protected PartyDO create(Party object) {
        return new PartyDO();
    }
}
