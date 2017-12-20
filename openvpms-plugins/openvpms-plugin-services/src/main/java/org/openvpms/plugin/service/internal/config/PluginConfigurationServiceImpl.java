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

package org.openvpms.plugin.service.internal.config;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.plugin.service.archetype.PluginArchetypeService;
import org.openvpms.plugin.service.config.PluginConfigurationService;

/**
 * .
 *
 * @author Tim Anderson
 */
public class PluginConfigurationServiceImpl implements PluginConfigurationService {

    private final IArchetypeRuleService service;

    private final PluginArchetypeService pluginService;

    public PluginConfigurationServiceImpl(IArchetypeRuleService service, PluginArchetypeService pluginService) {
        this.service = service;
        this.pluginService = pluginService;
    }

    /**
     * Returns the first configuration matching the specified archetype name, creating one if none is found.
     * <p>
     * It is expected that only a single instance of the configuration exists. If multiple instances exist, that with
     * the lowest identifier will be returned.
     * <p>
     * It is the caller's responsibility to save the configuration if it was created.
     *
     * @param archetype the configuration archetype
     * @return the configuration
     */
    @Override
    public IMObjectBean getConfiguration(String archetype) {
        Entity result;
        if (!TypeHelper.matches(archetype, "entity.*")) {
            throw new IllegalArgumentException("Argument 'archetype' is not an entity: " + archetype);
        }
        ArchetypeQuery query = new ArchetypeQuery(archetype, false);
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(service, query);
        if (iterator.hasNext()) {
            result = iterator.next();
        } else {
            result = (Entity) pluginService.create(archetype);
            if (result == null) {
                throw new IllegalArgumentException("Argument 'archetype' is not an valid archetype: " + archetype);
            }
        }
        return pluginService.getBean(result);
    }
}
