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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.rule;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;

import java.util.Collection;


/**
 * <tt>IArchetypeRuleService</tt> enables business rules to be executed for
 * particular {@link IArchetypeService} operations.
 * <p/>
 * Rules are simply java methods registered for a particular archetype.
 * Rules may be executed prior to an operation commencing (<em>before</em>
 * rules), and/or on the completion of the operation (<em>after</em> rules).
 * <p/>
 * Rules may be supplied with:
 * <ul>
 * <li>the object that triggered the rule
 * <li>a reference to an archetype service, <em>with rules disabled</em>. This
 * is required to avoid recursive execution of rules.
 * </ul>
 * <p/>
 * All rules are executed in a transaction. If a current transaction exists,
 * it will be used, else one will be created.
 * <p/>
 * The order of events for each operation is therefore:
 * <ol>
 * <li>begin transaction
 * <li>execute <em>before</em> rules
 * <li>perform archetype service operation
 * <li>execute <em>after</em> rules
 * <li>commit on success/rollback on failure
 * </ol>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface IArchetypeRuleService extends IArchetypeService {

    /**
     * Saves an object, executing any <em>save</em> rules associated with its
     * archetype.
     *
     * @param object the object to save
     * @throws ArchetypeServiceException if the service cannot save the
     *                                   specified object
     * @throws ValidationException       if the specified entity cannot be
     *                                   validated
     */
    void save(IMObject object);

    /**
     * Save a collection of {@link IMObject} instances. executing any
     * <em>save</em> rules associated with their archetypes.
     * <p/>
     * Rules will be executed in the order that the objects are supplied.
     *
     * @param objects the objects to save
     * @throws ArchetypeServiceException if an object can't be saved
     * @throws ValidationException       if an object can't be validated
     */
    void save(Collection<IMObject> objects);

    /**
     * Removes an object, executing any <em>remove</em> rules associated with
     * its archetype.
     *
     * @param object the object to remove
     * @throws ArchetypeServiceException if the object cannot be removed
     */
    void remove(IMObject object);
}
