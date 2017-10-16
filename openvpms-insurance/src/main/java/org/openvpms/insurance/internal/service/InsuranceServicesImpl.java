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

package org.openvpms.insurance.internal.service;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.insurance.claim.Claim;
import org.openvpms.insurance.service.InsuranceService;
import org.openvpms.insurance.service.InsuranceServices;
import org.openvpms.plugin.manager.PluginManager;

/**
 * Default implementation of {@link InsuranceServices}.
 *
 * @author Tim Anderson
 */
public class InsuranceServicesImpl implements InsuranceServices {

    /**
     * The plugin manager.
     */
    private final PluginManager manager;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link InsuranceServicesImpl}.
     *
     * @param manager the plugin manager.
     * @param service the archetype service
     */
    public InsuranceServicesImpl(PluginManager manager, IArchetypeService service) {
        this.manager = manager;
        this.service = service;
    }

    /**
     * Determines if claim can be submitted via an {@link InsuranceService}.
     *
     * @param claim the claim
     * @return {@code true} if the claim can be submitted
     */
    public boolean canSubmit(Claim claim) {
        Party insurer = claim.getPolicy().getInsurer();
        return getConfig(insurer) != null;
    }

    /**
     * Returns the insurance service for the specified insurer.
     *
     * @param insurer the insurer
     * @return the insurance service, or {@code null} if none is found
     */
    @Override
    public InsuranceService getService(Party insurer) {
        InsuranceService result = null;
        Entity config = getConfig(insurer);
        if (config != null) {
            String archetype = config.getArchetypeId().getShortName();
            for (InsuranceService service : manager.getServices(InsuranceService.class)) {
                if (archetype.equals(service.getArchetype())) {
                    result = service;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the <em>entity.insuranceService*</em> configuration for an insurer.
     *
     * @param insurer the insurer
     * @return the configuration, or {@code null} if none exists or is inactive
     */
    private Entity getConfig(Party insurer) {
        IMObjectBean bean = new IMObjectBean(insurer, service);
        return (Entity) bean.getNodeTargetObject("service");
    }
}
