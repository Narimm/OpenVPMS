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

package org.openvpms.smartflow.event.impl;

import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;

/**
 * Service for retrieving the current {@link FlowSheetConfig}.
 *
 * @author Tim Anderson
 */
public class FlowSheetConfigService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The default configuration, if none is registered.
     */
    private static final FlowSheetConfig DEFAULT = new FlowSheetConfig();


    /**
     * Constructs a {@link FlowSheetConfigService}.
     *
     * @param service         the archetype service
     * @param practiceService the practice service
     */
    public FlowSheetConfigService(IArchetypeService service, PracticeService practiceService) {
        this.service = service;
        this.practiceService = practiceService;
    }

    /**
     * Returns the configuration.
     *
     * @return the configuration
     */
    public FlowSheetConfig getConfig() {
        FlowSheetConfig config = null;
        Party practice = practiceService.getPractice();
        if (practice != null) {
            IMObjectBean bean = service.getBean(practice);
            IMObject object = bean.getTarget("smartflowConfiguration", IMObject.class);
            if (object != null) {
                config = new FlowSheetConfig(service.getBean(object));
            }
        }
        return (config != null) ? config : DEFAULT;
    }

}
