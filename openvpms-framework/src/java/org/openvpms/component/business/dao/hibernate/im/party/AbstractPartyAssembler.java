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

package org.openvpms.component.business.dao.hibernate.im.party;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDOImpl;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityAssembler;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.Set;


/**
 * Assembles {@link Party} instances from {@link PartyDO} instances and vice-versa.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPartyAssembler<T extends Party, DO extends PartyDO> extends EntityAssembler<T, DO> {

    /**
     * Assembles sets of contacts.
     */
    private SetAssembler<Contact, ContactDO> CONTACTS = SetAssembler.create(Contact.class, ContactDO.class);

    /**
     * Constructs a {@link AbstractPartyAssembler}.
     *
     * @param type   the object type
     * @param typeDO the data object interface type
     * @param impl   the data object implementation type
     */
    public AbstractPartyAssembler(Class<T> type, Class<DO> typeDO, Class<? extends IMObjectDOImpl> impl) {
        super(type, typeDO, impl);
    }

    /**
     * Assembles a data object from an object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param state   the data object state
     * @param context the assembly context
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void assembleDO(DO target, T source, DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        CONTACTS.assembleDO(target.getContacts(), (Set<Contact>) (Set) source.getContacts(), state, context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void assembleObject(T target, DO source, Context context) {
        super.assembleObject(target, source, context);
        CONTACTS.assembleObject((Set<Contact>) (Set) target.getContacts(), source.getContacts(), context);
    }

}
