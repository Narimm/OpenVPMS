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


package org.openvpms.component.business.service.archetype.descriptor;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Holds the set of valid archetypeDescriptors 
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeDescriptors implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * A map of valid archetypeDescriptors
     */
    private HashMap archetypeDescriptors = new HashMap();

    /**
     * Default constructor
     */
    public ArchetypeDescriptors() {
    }

    /**
     * @return Returns the archetypeDescriptors.
     */
    public HashMap getArchetypeDescriptors() {
        return archetypeDescriptors;
    }

    /**
     * @param archetypeDescriptors The archetypeDescriptors to set.
     */
    public void setArchetypeDescriptors(HashMap archetypes) {
        this.archetypeDescriptors = archetypes;
    }
}
