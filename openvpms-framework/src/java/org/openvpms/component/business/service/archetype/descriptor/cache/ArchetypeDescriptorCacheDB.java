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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.business.service.archetype.descriptor.cache;

// java core
import java.util.List;

// log4j
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptors;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * This implementation reads the archetype descriptors from the databases and
 * caches them in memory
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeDescriptorCacheDB extends BaseArchetypeDescriptorCache
    implements IArchetypeDescriptorCache {
    /** 
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeDescriptorCacheDB.class);

    /**
     * The DAO instance it will use
     */
    private IMObjectDAO dao;
    
    

    /**
     * Construct and instance of this cache and then proceed to load all 
     * the archetypes in the database.
     * 
     * @param dao
     *            data access object
     * @throws ArchetypeDescriptorCacheException
     *             thrown if it cannot bootstrap the cache
     */
    public ArchetypeDescriptorCacheDB(IMObjectDAO dao) {
        this.dao = dao;
        loadAssertionTypeDescriptors();
        loadArchetypeDescriptors();
    }

    /**
     * Construct and instance of this cache and then proceed to load all 
     * the archetypes in the database.
     * <p>
     * If a scanInterval greater than 0 is specified then a thread will be
     * created to monitor changes in the archetype definition.
     * 
     * @param dao
     *            data access object
     * @param scanInterval
     *            the interval that the archetype directory is scanned.
     * @throws ArchetypeDescriptorCacheException
     *             thrown if it cannot bootstrap the cache
     */
    public ArchetypeDescriptorCacheDB(IMObjectDAO dao, long scanInterval) {
        this(dao);
        // determine whether we should create a monitor thread
    }

    /**
     * Return the {@link AssertionTypeDescriptors} in the specified file
     * 
     * @throws ArchetypeDescriptorCacheException
     */
    private void loadAssertionTypeDescriptors() {
        List<IMObject> atypes = dao.get("system", "descriptor", "assertionType", 
                null, AssertionTypeDescriptor.class.getName());
        for (IMObject imobj : atypes) {
            if (!(imobj instanceof AssertionTypeDescriptor)) {
                logger.warn("Invalid assertion type object: Found object of type "
                        + imobj.getClass().getName());
                continue;
            }
            
            AssertionTypeDescriptor descriptor = (AssertionTypeDescriptor)imobj;
            assertionTypes.put(descriptor.getName(), descriptor);

            if (logger.isDebugEnabled()) {
                logger.debug("Loaded assertion type " + descriptor.getName());
            }
            
        }
    }
    
    /**
     * Load the archetype descriptors from the database into the cache
     * 
     * @throws ArchetypeDescriptorCacheException
     */
    private void loadArchetypeDescriptors() {
        processArchetypeDescriptors(dao.get("system", "descriptor", 
                null, null, ArchetypeDescriptor.class.getName()));
    }

    /**
     * Process all the archetype descriptors in the specified list
     * 
     * @param descriptors
     *            the descriptors to process
     * @throws ArchetypeDescriptorCacheException
     */
    private void processArchetypeDescriptors(List<IMObject> descriptors) {
        for (IMObject imobj : descriptors) {
            if (!(imobj instanceof ArchetypeDescriptor)) {
                logger.warn("processArchetypeDescriptors found object of type "
                        + imobj.getClass().getName());
                continue;
            }
            
            ArchetypeDescriptor descriptor = (ArchetypeDescriptor)imobj;
            ArchetypeId archId = descriptor.getType();

            if (logger.isDebugEnabled()) {
                logger.debug("Processing archetype record "
                        + archId.getShortName());
            }

            try {
                // make sure that the underlying type is loadable
                Thread.currentThread().getContextClassLoader().loadClass(
                        descriptor.getClassName());

                // only store one copy of the archetype by short name
                if ((archetypesByShortName.containsKey(archId.getShortName()) == false)
                        || (descriptor.isLatest())) {
                    archetypesByShortName
                            .put(archId.getShortName(), descriptor);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Loading  [" + archId.getShortName()
                                + "] in shortNameCache");
                    }
                }

                archetypesById.put(archId.getQName(), descriptor);

                if (logger.isDebugEnabled()) {
                    logger.debug("Loading [" + archId.getShortName()
                            + "] in archIdCache");
                }
                // check that the assertions are specified correctly
                if (descriptor.getNodeDescriptors().size() > 0) {
                    checkAssertionsInNode(descriptor.getNodeDescriptors());
                }
            } catch (Exception exception) {
                logger.warn("Failed to load descriptor " + descriptor.getName() 
                        + " b/c of error  [" + exception.toString() + "].");
            }
        }
    }
}
