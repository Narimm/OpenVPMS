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
 * This is a set of business rules for the party.person archetype
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PartyPersonRules {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(PartyPersonRules.class);
    
    /**
     * Display the onSave message
     * 
     * @param person
     *            the person entity
     * @throws RuleEngineException            
     */
    public static void onSaveMessage(IArchetypeService service, Party person) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing PartyPersonRules.onSaveMessage");
        }        
        
        System.out.println("We are about to do a save on " + person.getDetails().get("firstName") 
                + " " + person.getDetails().get("lastName")
                + " using service " + service.getClass().getName());
    }
     
    /**
     * Set the active end date for all existing entity relationships of type
     * entityRelationship.animalOwner.
     * 
     * @param service
     *            the archetype service
     * @param pet
     *            the pet entity
     * @throws RuleEngineException            
     */
    public static void setActiveEndDates(IArchetypeService service, Party person) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing PartyPersonRules.setActiveEndDates");
        }
        
        for (EntityRelationship rel : person.getEntityRelationships()) {
            if ((!rel.isNew()) &&
                (rel.getArchetypeId().getShortName().equals("entityRelationship.animalOwner"))) {
                rel.setActiveEndTime( new Date(System.currentTimeMillis() - 1000));
            }
        }
    }
}
