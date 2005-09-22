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

package org.openvpms.component.business.service.party;

// openvpms-domain

// openvpms-orm
import org.openvpms.component.business.dao.im.party.IPartyDAO;
import org.openvpms.component.business.dao.im.party.PartyDAOException;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeRecord;
import org.openvpms.component.business.service.archetype.IArchetypeService;

// openvpms-service

/**
 * This is an implementation of a generic entity service, which provides basic
 * CRUD functionality. The user should extend this class if the specific entity
 * service requires additional functionality.
 * <p>
 * The following code snippet illustrates how to create an instance of a types
 * entity service
 * <p>
 * <code>
 * PersonService pService = new PartyService<Person>(registry);
 * </code>
 * 
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PartyService implements IPartyService {
    /**
     * Cache a reference to an archetype archetype service..
     */
    private IArchetypeService archetypeService;
    
    /**
     * The DAO instance it will use 
     */
    private IPartyDAO dao;
    
    /**
     * Instantiate an instance of this service passing it a reference to the 
     * archetype servie and a DAO.
     * <p>
     * The archetype service is mandatory, since it provides a means of mapping
     * a name (i.e. party.person) to a archetype identifier and retrieving 
     * various archetype information
     * <p>
     * The data access object is required to interact with persistent or non-
     * persistent store. The store is used to cache the data.
     * 
     * @param archetypeService
     *            the archetype service reference
     * @param dao
     *            the reference to the data access object it will use for the 
     *            service             
     * @throws PartyServiceException
     *             a runtime exception tha is raised if the service cannot be
     *             instatiated
     */
    public PartyService(IArchetypeService archetypeService, IPartyDAO dao) {
        this.archetypeService = archetypeService;
        this.dao = dao;
    }


    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.party.IPartyService#createParty(java.lang.String)
     */
    public Party createParty(String shortName) {
        // ensure that we can retrieve an arhetype record for the
        // specified short name
        ArchetypeRecord record = archetypeService.getArchetypeRecord(shortName);
        if (record == null) {
            throw  new PartyServiceException(
                    PartyServiceException.ErrorCode.FailedToLocateArchetype,
                    new Object[] { shortName });
        }
        
        // create and return the party object
        return (Party)archetypeService.createDefaultObject(record.getArchetypeId());
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.party.IPartyService#insertParty(org.openvpms.component.business.domain.im.common.party.Party)
     */
    public void insertParty(Party party) {
        if (archetypeService.validateObject(party)) {
            try {
                dao.insert(party);
            } catch (PartyDAOException exception) {
                throw new PartyServiceException(
                        PartyServiceException.ErrorCode.FailedToCreateParty,
                        new Object[]{party.toString()}, exception);
            }
        } else {
            throw new PartyServiceException(
                    PartyServiceException.ErrorCode.InvalidPartyObject,
                    new Object[]{party, party.getArchetypeId()});
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.party.IPartyService#findParties(java.lang.String, java.lang.Object[])
     */
    public Party[] findParties(String searchCriteria, Object[] searchParams) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.party.IPartyService#removeParty(org.openvpms.component.business.domain.im.common.party.Party)
     */
    public void removeParty(Party party) {
        try {
            dao.delete(party);
        } catch (PartyDAOException exception) {
            throw new PartyServiceException(
                    PartyServiceException.ErrorCode.FailedToDeleteParty,
                    new Object[]{party.getArchetypeId().toString(),
                                 party.toString()}, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.party.IPartyService#updateParty(org.openvpms.component.business.domain.im.common.party.Party)
     */
    public void updateParty(Party party) {
        if (archetypeService.validateObject(party)) {
            try {
                dao.update(party);
            } catch (PartyDAOException exception) {
                throw new PartyServiceException(
                        PartyServiceException.ErrorCode.FailedToUpdateParty,
                        new Object[]{party.toString()}, exception);
            }
        } else {
            throw new PartyServiceException(
                    PartyServiceException.ErrorCode.FailedToUpdateParty,
                    new Object[]{party.getArchetypeId().toString(),
                                 party.toString()});
        }
    }
}
