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

package org.openvpms.archetype.rules.party;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;

/**
 * Party Relationship Rules 
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */

public class PartyRelationshipRules {

    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(PartyRelationshipRules.class);
    

    /**
     * Check the relationship records for a Patient and make sure they are valid 
     * and only one is active for each relationship type.
     * 
     * @param service
     *            the archetype service
     * @param pet
     *            the pet entity
     * @throws RuleEngineException            
     */
    
    public static void checkPatientRelationships(IArchetypeService service, Party party) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing PartyRelationship.checkPatientRelationships");
        }
        Map<String, EntityRelationship> currentActives = new HashMap<String, EntityRelationship>();
        EntityRelationship currentActive = null;
        
        // Loop through all the patient relationships.
        // If one is new then assume it is the active and set the ActiveEndTime on any others.
        // If more than 1 is new then set active to youngest activeStartTime. 
        // If no new relationship and more than one active relationship of same type then set the one with the youngest 
        // ActiveStartTime as Active. 
        for (EntityRelationship rel : party.getEntityRelationships()) {
            if (rel.getActiveEndTime() == null) {
                String shortname = rel.getArchetypeId().getShortName();
                currentActive = currentActives.get(shortname);
                if (rel.isNew()) {
                    if (currentActive == null)
                        currentActive = rel;
                    else if (currentActive.isNew()) {
                        if (rel.getActiveStartTime().after(currentActive.getActiveStartTime())) {
                            currentActive.setActiveEndTime( new Date(System.currentTimeMillis() - 1000));
                            currentActive = rel;
                        }
                    }
                    else {
                        currentActive.setActiveEndTime( new Date(System.currentTimeMillis() - 1000));
                        currentActive = rel;
                    }
                }
                else {
                    if (currentActive == null)
                        currentActive = rel;
                    else if (currentActive.isNew())
                        rel.setActiveEndTime( new Date(System.currentTimeMillis() - 1000));                                               
                    else if (rel.getActiveStartTime().after(currentActive.getActiveStartTime())) {
                        currentActive.setActiveEndTime( new Date(System.currentTimeMillis() - 1000));
                        currentActive = rel;                        
                    }
                    else if (currentActive.getActiveStartTime().after(rel.getActiveStartTime())) {
                        rel.setActiveEndTime( new Date(System.currentTimeMillis() - 1000));                        
                    }
                }
                currentActives.put(shortname,currentActive);
            }
        }
    }
}
