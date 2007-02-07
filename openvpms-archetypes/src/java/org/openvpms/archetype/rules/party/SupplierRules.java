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

package org.openvpms.archetype.rules.party;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.Date;


/**
 * Supplier rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SupplierRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a new <code>SupplierRules</code>.
     *
     * @throws ArchetypeServiceException if the archetype service is not
     *                                   configured
     */
    public SupplierRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Construct a new <code>SupplierRules/code>.
     *
     * @param service the archetype service
     */
    public SupplierRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returhs the referral vet practice for a vet overlapping the specified
     * time.
     *
     * @param vet  the vet
     * @param time the time
     * @return the practice the vet is associated with or <code>null</code> if
     *         the vet is not associated with any practice for the time frame
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getReferralVetPractice(Party vet, Date time) {
        EntityBean bean = new EntityBean(vet, service);
        return (Party) bean.getNodeSourceEntity("practices", time);
    }
}
