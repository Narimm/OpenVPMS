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
 * Class that represents non-living, non organisational parties such as devices, 
 * software systems etc.
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Agent extends Actor {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    protected Agent() {
        // do nothing
    }
    
    /**
     * Constructs an animal entity.
     * 
     * @param archetypeId
     *            the archetype id constraining this object
     * @param name
     *            the name of the entity            
     * @param description
     *            the description of this entity            
     */
    public Agent(ArchetypeId archetypeId, String name, String description) {
        super(archetypeId, name, description);
    }
}
