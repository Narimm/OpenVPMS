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


package org.openvpms.component.business.service.archetype.descriptor.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.dao.hibernate.im.IMObjectDAOHibernate;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

import java.util.List;
import java.util.Map;

/**
 * This implementation reads the archetype descriptors from the databases and caches them in memory.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class ArchetypeDescriptorCacheDB implements IArchetypeDescriptorCache {

    /**
     * The DAO instance it will use
     */
    private final IMObjectDAO dao;

    /**
     * The underlying cache.
     */
    private Cache cache;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ArchetypeDescriptorCacheDB.class);


    /**
     * Construct and instance of this cache and then proceed to load all the archetypes in the database.
     *
     * @param dao data access object
     */
    public ArchetypeDescriptorCacheDB(IMObjectDAO dao) {
        this.dao = dao;
        cache = new Cache(false);
        if (dao instanceof IMObjectDAOHibernate) {
            // todo - smelly
            ((IMObjectDAOHibernate) dao).setArchetypeDescriptorCache(this);
        }
        cache.load();
    }

    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified short name.
     * <p/>
     * If there are multiple archetype descriptors with the same name then it will retrieve the first descriptor marked
     * with latest=true.
     *
     * @param name the short name
     * @return ArchetypeDescriptor the matching archetype descriptor or {@code null} if none is found
     */
    @Override
    public ArchetypeDescriptor getArchetypeDescriptor(String name) {
        return cache.getArchetypeDescriptor(name);
    }

    /**
     * Retrieve the {@link ArchetypeDescriptor} with the specified {@link ArchetypeId}.
     * <p/>
     * If the archetype version isn't specified, it will retrieve the first descriptor marked with latest=true.
     *
     * @param id the archetype id
     * @return ArchetypeDescriptor the matching archetype descriptor or {@code null} if none is found
     */
    @Override
    public ArchetypeDescriptor getArchetypeDescriptor(ArchetypeId id) {
        return cache.getArchetypeDescriptor(id);
    }

    /**
     * Return all the {@link ArchetypeDescriptor} instances managed by this cache.
     *
     * @return the descriptors
     */
    @Override
    public List<ArchetypeDescriptor> getArchetypeDescriptors() {
        return cache.getArchetypeDescriptors();
    }

    /**
     * Return all the {@link ArchetypeDescriptor} instances that match the specified shortName.
     *
     * @param shortName the short name, which may contain wildcards
     * @return the matching descriptors
     */
    @Override
    public List<ArchetypeDescriptor> getArchetypeDescriptors(String shortName) {
        return cache.getArchetypeDescriptors(shortName);
    }

    /**
     * Return the {@link AssertionTypeDescriptor} with the specified name.
     *
     * @param name the name of the assertion type
     * @return the matching assertion type descriptor, or {@code null} if none is found
     */
    @Override
    public AssertionTypeDescriptor getAssertionTypeDescriptor(String name) {
        return cache.getAssertionTypeDescriptor(name);
    }

    /**
     * Return all the {@link AssertionTypeDescriptor} instances supported by this cache.
     *
     * @return the cached assertion type descriptors
     */
    @Override
    public List<AssertionTypeDescriptor> getAssertionTypeDescriptors() {
        return cache.getAssertionTypeDescriptors();
    }

    /**
     * Return a list of archetype short names given the nominated criteria.
     *
     * @param entityName  the entity name. May contain wildcards
     * @param conceptName the concept name. May contain wildcards
     * @param primaryOnly indicates whether to return primary objects only
     */
    @Override
    public List<String> getArchetypeShortNames(String entityName, String conceptName, boolean primaryOnly) {
        return cache.getArchetypeShortNames(entityName, conceptName, primaryOnly);
    }

    /**
     * Return all the archetypes which match the specified short name
     *
     * @param shortName   the short name, which may contain wildcards
     * @param primaryOnly return only the primary archetypes
     * @return the matching archetype short names
     */
    @Override
    public List<String> getArchetypeShortNames(String shortName, boolean primaryOnly) {
        return cache.getArchetypeShortNames(shortName, primaryOnly);
    }

    /**
     * Add an archetype descriptor to the cache.
     *
     * @param descriptor the archetype descriptor to add
     */
    @Override
    public void addArchetypeDescriptor(ArchetypeDescriptor descriptor) {
        cache.addArchetypeDescriptor(descriptor);
    }

    /**
     * Adds an assertion type descriptor to the cache.
     *
     * @param descriptor the assertion type descriptor to add
     */
    @Override
    public void addAssertionTypeDescriptor(AssertionTypeDescriptor descriptor) {
        cache.addAssertionTypeDescriptor(descriptor);
    }

    /**
     * Return all the archetype short names.
     *
     * @return the archetype short names
     */
    @Override
    public List<String> getArchetypeShortNames() {
        return cache.getArchetypeShortNames();
    }

    /**
     * Refreshes the cache.
     */
    public void refresh() {
        cache = new Cache();
    }

    private class Cache extends BaseArchetypeDescriptorCache {

        public Cache() {
            this(true);
        }

        public Cache(boolean load) {
            if (load) {
                load();
            }
        }

        public void load() {
            loadAssertionTypeDescriptors();
            loadArchetypeDescriptors();
        }

        /**
         * Loads the assertion type descriptors from the database into the cache.
         */
        private void loadAssertionTypeDescriptors() {
            List<IMObject> types = dao.get("descriptor.assertionType", null, AssertionTypeDescriptor.class.getName(),
                                           true, 0, ArchetypeQuery.ALL_RESULTS).getResults();
            for (IMObject type : types) {
                if (type instanceof AssertionTypeDescriptor) {
                    AssertionTypeDescriptor descriptor = (AssertionTypeDescriptor) type;
                    addAssertionTypeDescriptor(descriptor);

                    if (log.isDebugEnabled()) {
                        log.debug("Loaded assertion type " + descriptor.getName());
                    }
                } else {
                    log.warn("Invalid assertion type object. Found object of type " + type.getClass().getName());
                }
            }
        }

        /**
         * Load the archetype descriptors from the database into the cache.
         */
        private void loadArchetypeDescriptors() {
            IPage<IMObject> page = dao.get("descriptor.*", null, ArchetypeDescriptor.class.getName(),
                                           true, 0, ArchetypeQuery.ALL_RESULTS);
            for (IMObject object : page.getResults()) {
                if (object instanceof ArchetypeDescriptor) {
                    loadArchetypeDescriptor((ArchetypeDescriptor) object);
                } else {
                    log.warn("loadArchetypeDescriptors found object of type " + object.getClass().getName());
                }
            }
        }

        /**
         * Loads an archetype descriptor.
         *
         * @param descriptor the descriptor to load
         */
        @SuppressWarnings("unchecked")
        private void loadArchetypeDescriptor(ArchetypeDescriptor descriptor) {
            ArchetypeId archId = descriptor.getType();

            if (log.isDebugEnabled()) {
                log.debug("Processing archetype " + archId.getShortName());
            }

            try {
                // make sure that the underlying type is loadable
                Thread.currentThread().getContextClassLoader().loadClass(descriptor.getClassName());

                // check that the assertions are specified correctly
                Map nodeDescriptors = descriptor.getNodeDescriptors();
                if (nodeDescriptors.size() > 0) {
                    checkAssertionsInNode((Map<String, NodeDescriptor>) nodeDescriptors);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Loading  archetype " + archId.getShortName());
                }
                addArchetypeDescriptor(descriptor);

            } catch (Exception exception) {
                log.warn("Failed to load descriptor " + descriptor.getName(), exception);
            }
        }
    }
}
