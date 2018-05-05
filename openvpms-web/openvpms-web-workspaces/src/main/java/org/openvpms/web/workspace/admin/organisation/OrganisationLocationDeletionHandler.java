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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.organisation;

import org.apache.commons.lang.ArrayUtils;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.component.im.delete.AbstractEntityDeletionHandler;
import org.openvpms.web.component.im.delete.IMObjectDeletionHandler;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * An {@link IMObjectDeletionHandler} for <em>party.organisationLocation</em>.
 *
 * @author Tim Anderson
 */
public class OrganisationLocationDeletionHandler extends AbstractEntityDeletionHandler<Party> {

    private final String[] participations;

    /**
     * Constructs a {@link AbstractEntityDeletionHandler}.
     *
     * @param object             the object to delete
     * @param factory            the editor factory
     * @param transactionManager the transaction manager
     * @param service            the archetype service
     */
    public OrganisationLocationDeletionHandler(Party object, IMObjectEditorFactory factory,
                                               PlatformTransactionManager transactionManager,
                                               IArchetypeRuleService service) {
        super(object, factory, transactionManager, service);

        String[] archetypes = DescriptorHelper.getShortNames(DEFAULT_PARTICIPATIONS, false, service);
        participations = (String[]) ArrayUtils.removeElement(archetypes, "participation.logo");
    }

    /**
     * Returns the participation archetypes to check.
     *
     * @return the participation archetypes to check
     */
    @Override
    protected String[] getParticipations() {
        return participations;
    }
}
