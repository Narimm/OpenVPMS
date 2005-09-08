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

package org.openvpms.component.business.dao.im.party;

// openvpms-domain
import org.openvpms.component.business.domain.im.party.Party;

/**
 * This interface provides data access object (DAO) support for objects of 
 * type {@link Party}. The class includes the capability to perform insert,
 * delete, update and remove data.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IPartyDAO {
    /**
     * Insert the specified {@link Party}.
     * 
     * @param party
     *            the party to insert
     * @throws PartyDAOException
     *             a runtime exception if the request cannot complete
     */
    public void insert(Party party);

    /**
     * Update the specified {@link Party}
     * 
     * @param party
     *            the party to update
     * @throws PartyDAOException
     *             a runtime exception if the request cannot complete
     */
    public void update(Party party);

    /**
     * Delete the specified {@link Party}
     * 
     * @param party
     *            the party to delete
     * @throws PartyDAOException
     *             a runtime exception if the request cannot complete
     */
    public void delete(Party party);

    /**
     * Retrieve the entity with the specified id
     * 
     * @param id
     *            the id of the entity to retrieve
     * @param return
     *            Entity
     * @throws PartyDAOException
     *             a runtime exception if the request cannot complete
     */
    public Party findById(String id);
}
