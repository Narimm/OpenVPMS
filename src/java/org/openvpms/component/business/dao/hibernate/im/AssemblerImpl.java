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

package org.openvpms.component.business.dao.hibernate.im;

import org.openvpms.component.business.dao.hibernate.im.act.ActAssembler;
import org.openvpms.component.business.dao.hibernate.im.act.ActRelationshipAssembler;
import org.openvpms.component.business.dao.hibernate.im.act.DocumentActAssembler;
import org.openvpms.component.business.dao.hibernate.im.act.FinancialActAssembler;
import org.openvpms.component.business.dao.hibernate.im.act.ParticipationAssembler;
import org.openvpms.component.business.dao.hibernate.im.archetype.ActionTypeDescriptorAssembler;
import org.openvpms.component.business.dao.hibernate.im.archetype.ArchetypeDescriptorAssembler;
import org.openvpms.component.business.dao.hibernate.im.archetype.AssertionDescriptorAssembler;
import org.openvpms.component.business.dao.hibernate.im.archetype.AssertionTypeDescriptorAssembler;
import org.openvpms.component.business.dao.hibernate.im.archetype.NodeDescriptorAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.document.DocumentAssembler;
import org.openvpms.component.business.dao.hibernate.im.entity.DefaultEntityAssembler;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityIdentityAssembler;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityRelationshipAssembler;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupAssembler;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupRelationshipAssembler;
import org.openvpms.component.business.dao.hibernate.im.party.ContactAssembler;
import org.openvpms.component.business.dao.hibernate.im.party.PartyAssembler;
import org.openvpms.component.business.dao.hibernate.im.product.ProductAssembler;
import org.openvpms.component.business.dao.hibernate.im.product.ProductPriceAssembler;
import org.openvpms.component.business.dao.hibernate.im.security.ArchetypeAuthorityAssembler;
import org.openvpms.component.business.dao.hibernate.im.security.SecurityRoleAssembler;
import org.openvpms.component.business.dao.hibernate.im.security.UserAssembler;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;


/**
 * Concrete implemention of the {@link CompoundAssembler}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AssemblerImpl extends CompoundAssembler {

    /**
     * Creates a new <tt>AssemblerImpl</tt>.
     *
     * @param cache the archetype descriptor cache
     */
    public AssemblerImpl(IArchetypeDescriptorCache cache) {
        addAssembler(new ActAssembler());
        addAssembler(new ActRelationshipAssembler());
        addAssembler(new DocumentActAssembler());
        addAssembler(new FinancialActAssembler());

        addAssembler(new ActionTypeDescriptorAssembler());
        addAssembler(new ArchetypeDescriptorAssembler());
        addAssembler(new AssertionDescriptorAssembler(cache));
        addAssembler(new AssertionTypeDescriptorAssembler());
        addAssembler(new NodeDescriptorAssembler());

        addAssembler(new DocumentAssembler());

        addAssembler(new DefaultEntityAssembler());
        addAssembler(new EntityRelationshipAssembler());
        addAssembler(new EntityIdentityAssembler());
        addAssembler(new ParticipationAssembler());

        addAssembler(new LookupAssembler());
        addAssembler(new LookupRelationshipAssembler());

        addAssembler(new ContactAssembler());
        addAssembler(new PartyAssembler());

        addAssembler(new ProductAssembler());
        addAssembler(new ProductPriceAssembler());

        addAssembler(new ArchetypeAuthorityAssembler());
        addAssembler(new SecurityRoleAssembler());
        addAssembler(new UserAssembler());
    }
}
