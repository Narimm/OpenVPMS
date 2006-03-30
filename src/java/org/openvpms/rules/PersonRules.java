package org.openvpms.rules;

import java.util.Date;

import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */

public class PersonRules {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(PersonRules.class);
    
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
        
        System.out.println("We are about to do a save on " + person.getDetails().getAttribute("firstName") 
                + " " + person.getDetails().getAttribute("lastName")
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
