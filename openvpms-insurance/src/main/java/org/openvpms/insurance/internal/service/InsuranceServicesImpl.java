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
     * Determines if an insurer has an associated insurance service.
     *
     * @param insurer the insurer
     * @return {@code true} if the insurer has an insurance service
     */
    @Override
    public boolean hasInsuranceService(Party insurer) {
        IMObjectBean bean = new IMObjectBean(insurer, service);
        return bean.getNodeTargetObjectRef("service") != null;
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
        IMObjectBean bean = new IMObjectBean(insurer, service);
        Entity config = (Entity) bean.getNodeTargetObject("service");
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
}
