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


package org.openvpms.component.business.domain.im.party;


// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * An organization is an {@link Entity} that can participat in {@link Act}.
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Organization extends Actor {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public Organization() {
    }

    /**
     * Constructs an organization entity.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name of the organization            
     * @param description
     *            the description of this entity            
     */
    public Organization(ArchetypeId archetypeId, String name, String description) {
        super(archetypeId, name, description);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.party.Actor#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Organization copy = (Organization)super.clone();

        return copy;
    }
}
