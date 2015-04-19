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
 *  $Id: IEntityService.java 149 2005-09-26 23:07:52Z jalateras $
 */

package org.openvpms.component.business.service.act;

// openvpms-framework
import org.openvpms.component.business.domain.im.act.Act;


/**
 * This service interface, provides standard CRUD (create, retrieve, update and 
 * delete) functionality for acts. It is a generic service that can be used
 * to operate on subtypes.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-09-27 09:07:52 +1000 (Tue, 27 Sep 2005) $
 * @deprecated no replacement
 */
@Deprecated
public interface IActService {
    /**
     * Create an act using the specified shortName. The short name is a 
     * reference to the archetype that is used to create the object. An archetype 
     * restricts the instances of the domain class by declaring constraints on 
     * it.
     * <p>
     * The returned object is a default (but not necessarily valid) representation
     * of the archetype. The returned object is transient
     * 
     * @param shortName
     *            the short name to the archetype
     * @return Act
     * @throws ActServiceException
     *             a runtime exception
     * 
     */
    public Act create(String shortName);

    /**
     * Insert the specified {@link Act}. This service is now responsible for
     * managing the act.
     * 
     * @param act
     *            the act to insert
     * @throws ActServiceException                      
     */
    public void insert(Act act);
    
    /**
     * Remove the specified act. If the act cannot be removed for whatever
     * reason then raise a {@link ActServiceException}.
     * 
     * @param act
     *            the act to remove
     * @throws ActServiceException
     *             a runtime exception
     */
    public void remove(Act act);

    /**
     * Update the specified act. The act is validated against its
     * archetype before it is updated. If the act is invalid or it cannot be 
     * updated the {@link ActServiceException} exception is thrown.
     * <p>
     * The update method implies both save and upate semantics
     * 
     * @param act
     *            the act to update
     * @throws ActServiceException
     *             a runtime exception
     */
    public void update(Act act);

    /**
     * The save should be used in preference to the {@link #insert(Act)} or
     * {@link #update(Act)} methods. This will check that whether the act
     * is new and if it is do an insert otherwise do an update.
     * 
     * @param act
     *            the act to insert or update
     * @throws ActServiceException
     *             a runtime exception
     */
    public void save(Act act);   

    /**
     * Uses the specified criteria to return zero, one or more matching . 
     * acts. This is a very generic query which will constrain the 
     * returned set on one or more of the supplied values.
     * <p>
     * Each of the parameters can denote an exact match or a partial match. If
     * a partial match is required then the last character of the value must be
     * a '*'. In every other case the search will look for an exact match.
     * <p>
     * All the values are optional. In the case where all the values are null
     * then all the entities will be returned. In the case where two or more 
     * values are specified (i.e. rmName and entityName) then only acts 
     * satisfying both conditions will be returned.
     * 
     * @param rmName
     *            the reference model name (partial or complete)
     * @param actName
     *            the name of the act
     * @param concept
     *            the concept name
     * @param instanceName
     *            the particular instance name
     * @return Act[]                                    
     * @throws ActServiceException
     *            a runtime exception                         
     */
    public Act[] get(String rmName, String entityName, 
            String conceptName, String instanceName);
}
