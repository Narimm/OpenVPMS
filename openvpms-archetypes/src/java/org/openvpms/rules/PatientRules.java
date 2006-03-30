package org.openvpms.rules;

import java.util.Date;

import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;

/**
 * Business Rules for Patient {@link Party} instances
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */

public class PatientRules {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(PatientRules.class);
    
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
            logger.debug("Executing PatientRules.setActiveEndDates");
        }
        
        for (EntityRelationship rel : pet.getEntityRelationships()) {
            if (!rel.isNew()) {
                rel.setActiveEndTime( new Date(System.currentTimeMillis() - 1000));
            }
        }
    }
}
