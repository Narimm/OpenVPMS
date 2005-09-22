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

// openvpms-framework
import org.openvpms.component.business.domain.im.party.Party;


/**
 * This service interface, provides standard CRUD (create, retrieve, update and 
 * delete) functionality.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IPartyService {
    /**
     * Create an party using the specified shortName. The short name is a 
     * reference to the archetype that is used to create the object. An archetype 
     * restricts the instances of the domain class by declaring constraints on 
     * it.
     * <p>
     * The returned object is a default (but not necessarily valid) representation
     * of the archetype. The returned object is transient
     * 
     * @param shortName
     *            the short name to the archetype
     * @return Party
     * @throws PartyServiceException
     *             a runtime exception
     * 
     */
    public Party createParty(String shortName);

    /**
     * Insert the specified {@link Party}. This service is now responsible for
     * managing the party.
     * 
     * @param party
     *            the party to insert
     * @throws PartyServiceException                      
     */
    public void insertParty(Party party);
    
    /**
     * Remove the specified party. If the party cannot be removed for whatever
     * reason then raise a {@link PartyServiceException} is raised.
     * 
     * @param party
     *            the party to remove
     * @throws PartyServiceException
     *             a runtime exception
     */
    public void removeParty(Party party);

    /**
     * Update the specified party. The party is validated against its
     * archetype before it is updated. If the party is invalid or it cannot be 
     * updated the {@link PartyServiceException} exception is thrown.
     * <p>
     * The updateEntity method implies both save and upate semantics
     * 
     * @param party
     *            the party to update
     * @throws PartyServiceException
     *             a runtime exception
     */
    public void updateParty(Party party);

    /**
     * Retrieve all the parties that match the specified criteria. The criteria
     * can be a a query or a reference to a declared query. The parameters are
     * used to constrain the query.
     * <p>
     * This method will return zero, one or more parties, that match the
     * specified criteria.
     * <p>
     * If there is an error in the query, or the passed in parameters or the
     * query fails to execute then raise a {@link ArchetypeIdException}.
     * 
     * @param searchCriteria
     *            the search crtieria or reference to a search
     * @param searchParams
     *            the parameters used to constrain the search
     * @return Party[] an array (which may be empty} of matching entries
     * @throws ArchetypeIdException
     *             a runtime exception if the request cannot be serviced
     */
    public Party[] findParties(String searchCriteria, Object[] searchParams);
}
