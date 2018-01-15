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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.insurance.internal.service;

import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.bean.Policies;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.party.Party;
import org.openvpms.insurance.exception.InsuranceException;
import org.openvpms.insurance.internal.i18n.InsuranceMessages;
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
    private final IArchetypeRuleService service;

    /**
     * Constructs a {@link InsuranceServicesImpl}.
     *
     * @param manager the plugin manager.
     * @param service the archetype service
     */
    public InsuranceServicesImpl(PluginManager manager, IArchetypeRuleService service) {
        this.manager = manager;
        this.service = service;
    }

    /**
     * Determines if claims can be submitted to an insurer via an {@link InsuranceService}.
     *
     * @param insurer the insurer
     * @return {@code true} if insurer accepts claims via an {@link InsuranceService}
     */
    public boolean canSubmit(Party insurer) {
        return getConfig(insurer) != null;
    }

    /**
     * Returns the insurance service for the specified <em>entity.insuranceService*</em> configuration.
     *
     * @param config the service configuration
     * @return the insurance service
     * @throws InsuranceException if the service is unavailable
     */
    @Override
    public InsuranceService getServiceForConfiguration(Entity config) {
        InsuranceService result = null;
        String archetype = config.getArchetype();
        for (InsuranceService insuranceService : manager.getServices(InsuranceService.class)) {
            if (archetype.equals(insuranceService.getArchetype())) {
                result = insuranceService;
                break;
            }
        }
        if (result == null) {
            throw new InsuranceException(InsuranceMessages.serviceUnavailable(config.getName()));
        }
        return result;
    }

    /**
     * Returns the insurance service for the specified insurer.
     *
     * @param insurer the insurer
     * @return the insurance service, or {@code null} if the insurer is not associated with an insurance service
     * @throws InsuranceException if the service is unavailable
     */
    @Override
    public InsuranceService getService(Party insurer) {
        InsuranceService result = null;
        Entity config = getConfig(insurer);
        if (config != null) {
            result = getServiceForConfiguration(config);
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
        IMObjectBean bean = service.getBean(insurer);
        return bean.getTarget("service", Entity.class, Policies.active());
    }
}
