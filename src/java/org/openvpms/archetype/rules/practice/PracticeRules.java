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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.practice;

import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.util.List;


/**
 * Practice rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PracticeRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>PracticeRules</tt>.
     */
    public PracticeRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>PracticeRules</tt>.
     *
     * @param service the archetype service
     */
    public PracticeRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Determines if the specified practice is the only active practice.
     *
     * @param practice the practice
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isActivePractice(Party practice) {
        if (practice.isActive()) {
            ArchetypeQuery query = new ArchetypeQuery(PracticeArchetypes.PRACTICE, true, true);
            IMObjectQueryIterator<Party> iter = new IMObjectQueryIterator<Party>(service, query);
            IMObjectReference practiceRef = practice.getObjectReference();
            while (iter.hasNext()) {
                Party party = iter.next();
                if (!party.getObjectReference().equals(practiceRef)) {
                    return false; // there is another active practice
                }
            }
            return true; // no other active practice
        }
        return false;    // practice is inactive
    }

    /**
     * Returns the practice.
     *
     * @return the practice, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getPractice() {
        ArchetypeQuery query = new ArchetypeQuery(PracticeArchetypes.PRACTICE, true, true);
        query.setMaxResults(1);
        IMObjectQueryIterator<Party> iter = new IMObjectQueryIterator<Party>(service, query);
        return (iter.hasNext()) ? iter.next() : null;
    }

    /**
     * Returns the locations associated with a practice.
     *
     * @param practice the practice
     * @return the locations associated with the user
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    public List<Party> getLocations(Party practice) {
        EntityBean bean = new EntityBean(practice, service);
        List locations = bean.getNodeTargetEntities("locations");
        return (List<Party>) locations;
    }

    /**
     * Returns the default location associated with a practice.
     *
     * @param practice the practice
     * @return the default location, or the first location if there is no
     *         default location or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getDefaultLocation(Party practice) {
        return (Party) EntityRelationshipHelper.getDefaultTarget(practice, "locations", service);
    }

}
