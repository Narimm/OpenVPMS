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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.domain.im.financial;

// java core
import java.util.Set;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Role;
import org.openvpms.component.business.domain.im.datatypes.quantity.DvInterval;
import org.openvpms.component.business.domain.im.datatypes.quantity.datetime.DvDateTime;

/**
 * An Account.
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Account extends Role {

    /**
     * Generated SUID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Default constructor
     */
    protected Account() {
        // do nothing
    }

    /**
     * Constructs an account.
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archetype id constraining this object
     * @param description
     *            the description of this entity            
     * @param contacts
     *            the collection of contacts of this role
     * @param activePeriod
     *            the period that this role is valid                        
     * @param details
     *            dynamic properties for this role
     */
    public Account(String uid, ArchetypeId archetypeId, 
            String description, Set<Contact> contacts,
            DvInterval<DvDateTime> activePeriod, DynamicAttributeMap details) {
        super(uid, archetypeId, description, contacts, activePeriod, details);
    }
}
