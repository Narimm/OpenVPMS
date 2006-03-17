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


package org.openvpms.component.business.service.ruleengine;

import java.util.Date;

import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 * These are rules specific to the animal.pet archetype.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class AnimalPetRules {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(AnimalPetRules.class);
    
    /**
     * Set the active end date for all existing (i.e not new) entity
     * relationships.
     * 
     * @param service
     *            the archetype service
     * @param pet
     *            the pet entity
     * @throws RuleEngineException            
     */
    public static void setActiveEndDates(IArchetypeService service, Party pet) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing AnimalPetRules.setActiveEndDates");
        }
        
        for (EntityRelationship rel : pet.getEntityRelationships()) {
            if (!rel.isNew()) {
                rel.setActiveEndTime( new Date(System.currentTimeMillis() - 1000));
            }
        }
    }
}
