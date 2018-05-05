/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient;

import org.openvpms.archetype.rules.party.PartyMerger;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Set;


/**
 * Merges <em>party.patientpet</em> instances.
 *
 * @author Tim Anderson
 */
public class PatientMerger extends PartyMerger {

    /**
     * Desexed node name.
     */
    public static final String DESEXED = "desexed";

    /**
     * Deceased node name.
     */
    public static final String DECEASED = "deceased";

    /**
     * Constructs a {@link PatientMerger}.
     *
     * @param service the archetype service
     */
    public PatientMerger(IArchetypeService service) {
        super(PatientArchetypes.PATIENT, service);
    }

    /**
     * Merges one {@link Party} with another.
     * <p/>
     * If the 'from' patient is desexed or deceased, this will be reflected in the merged patient.
     *
     * @param from   the party to merge from
     * @param to     the party to merge to
     * @param merged the set of changed objects
     */
    @Override
    protected void merge(Party from, Party to, Set<IMObject> merged) {
        IMObjectBean fromBean = new IMObjectBean(from, getArchetypeService());
        boolean desexed = fromBean.getBoolean(DESEXED);
        boolean deceased = fromBean.getBoolean(DECEASED);
        super.merge(from, to, merged);
        if (desexed || deceased) {
            IMObjectBean toBean = new IMObjectBean(to, getArchetypeService());
            if (desexed) {
                toBean.setValue(DESEXED, true);
            }
            if (deceased) {
                toBean.setValue(DECEASED, true);
            }
        }
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
