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
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ContactAssembler extends IMObjectAssembler<Contact, ContactDO> {

    private static final SetAssembler<Lookup, LookupDO> LOOKUPS
            = SetAssembler.create(Lookup.class, LookupDO.class);

    public ContactAssembler() {
        super(Contact.class, ContactDO.class);
    }

    @Override
    protected void assembleDO(ContactDO result, Contact source,
                              Context context) {
        super.assembleDO(result, source, context);
        result.setActiveStartTime(source.getActiveStartTime());
        result.setActiveEndTime(source.getActiveEndTime());
        result.setParty(getDO(source.getParty(), PartyDO.class, context));

        LOOKUPS.assembleDO(result.getClassifications(),
                           source.getClassifications(),
                           context);
    }

    @Override
    protected void assembleObject(Contact result, ContactDO source,
                                  Context context) {
        super.assembleObject(result, source, context);
        result.setActiveStartTime(source.getActiveStartTime());
        result.setActiveEndTime(source.getActiveEndTime());
        result.setParty(getObject(source.getParty(), Party.class, context));

        LOOKUPS.assembleObject(result.getClassifications(),
                               source.getClassifications(),
                               context);
    }

    protected Contact create(ContactDO object) {
        return new Contact();
    }

    protected ContactDO create(Contact object) {
        return new ContactDO();
    }
}
