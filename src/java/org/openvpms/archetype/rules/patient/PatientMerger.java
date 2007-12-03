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

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.rules.party.PartyMerger;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Merges <em>party.patientpet</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientMerger extends PartyMerger {

    /**
     * Creates a new <tt>PatientMerger</tt>.
     */
    public PatientMerger() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>PatientMerger</tt>.
     *
     * @param service the archetype service
     */
    public PatientMerger(IArchetypeService service) {
        super("party.patientpet", service);
    }

    /**
     * Copies entity relationships from one party to another,
     * excluding any relationships which would duplicate an existing
     * relationship in the 'to' party.
     *
     * @param from the party to copy from
     * @param to   the party to copy to
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    protected void copyEntityRelationships(Party from, Party to) {
        super.copyEntityRelationships(from, to);
        PatientRelationshipRules.checkRelationships(to);
    }

}
