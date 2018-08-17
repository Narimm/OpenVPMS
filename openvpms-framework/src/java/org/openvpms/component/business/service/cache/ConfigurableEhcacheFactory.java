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

package org.openvpms.component.business.service.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.cache.ehcache.EhCacheFactoryBean;

/**
 * Implementation of the {@link EhcacheFactory} interface that makes cache configurations persistent.
 *
 * @author Tim Anderson
 */
public class ConfigurableEhcacheFactory implements EhcacheFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The archetype.
     */
    private final String archetype;


    /**
     * Constructs a {@link ConfigurableEhcacheFactory}.
     *
     * @param service   the archetype service
     * @param archetype the singleton archetype, used to make cache configurations persistent
     */
    public ConfigurableEhcacheFactory(IArchetypeRuleService service, String archetype) {
        this.service = service;
        this.archetype = archetype;
    }

    /**
     * Creates an eternal cache.
     *
     * @param name the cache name. Must be unique
     * @return a new cache
     */
    @Override
    public Ehcache create(String name) {
        EhCacheFactoryBean factory = new EhCacheFactoryBean();
        factory.setDiskPersistent(false);
        factory.setCacheName(name);
        long elements = getMaxElements(name);
        if (elements <= 0) {
            elements = 100;
        }
        factory.setMaxEntriesLocalHeap(elements);
        factory.setOverflowToDisk(false);
        factory.setTimeToIdle(0);
        factory.setTimeToLive(0);
        factory.setEternal(true);
        factory.setStatisticsEnabled(true);
        factory.afterPropertiesSet();
        Ehcache cache = factory.getObject();
        CacheConfiguration config = cache.getCacheConfiguration();
        if (config.getSizeOfPolicyConfiguration() == null) {
            // register a configuration to limit warnings.
            SizeOfPolicyConfiguration policyConfiguration = new SizeOfPolicyConfiguration()
                    .maxDepth(1000000)
                    .maxDepthExceededBehavior(SizeOfPolicyConfiguration.MaxDepthExceededBehavior.CONTINUE);
            config.sizeOfPolicy(policyConfiguration);
        }
        return cache;
    }

    /**
     * Sets the maximum number of elements that a cache may hold in memory.
     * <p>
     * This will be made persistent for use when the cache is next constructed.
     * <br/>
     * This requires the configuration to exist, and have a Long node named {@code <cache name>MaxElements}.
     *
     * @param cache       the cache
     * @param maxElements the maximum number of elements
     */
    @Override
    public void setMaxElements(Ehcache cache, long maxElements) {
        cache.getCacheConfiguration().setMaxEntriesLocalHeap(maxElements);
        IMObjectBean config = getConfig();
        String node = getMaxElementsNode(cache.getName());
        if (config != null && config.hasNode(node)) {
            config.setValue(node, maxElements);
            config.save();
        }
    }

    /**
     * Returns the maximum number of elements for the named cache.
     *
     * @param name the cache name
     * @return the maximum elements, or {@code <=0} to use the default
     */
    protected long getMaxElements(String name) {
        long result = -1;
        IMObjectBean config = getConfig();
        String node = getMaxElementsNode(name);
        if (config != null && config.hasNode(node)) {
            result = config.getLong(node);
            if (result == 0) {
                // use the default value, if any
                Object value = config.getDefaultValue(node);
                if (value instanceof Number) {
                    result = ((Number) value).longValue();
                }
            }
        }
        return result;
    }

    /**
     * Returns the 'max elements' node name for the cache.
     *
     * @param name the cache
     * @return the 'max elements' node name
     */
    private String getMaxElementsNode(String name) {
        return name + "MaxElements";
    }

    /**
     * Returns the configuration. This must be an existing singleton object.
     *
     * @return the configuration or {@code null} if none is defined
     */
    private IMObjectBean getConfig() {
        ArchetypeQuery query = new ArchetypeQuery(archetype, true, true);
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Party> iterator = new IMObjectQueryIterator<>(service, query);
        return iterator.hasNext() ? service.getBean(iterator.next()) : null;
    }

}
