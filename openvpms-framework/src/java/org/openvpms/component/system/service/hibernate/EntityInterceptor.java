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


package org.openvpms.component.system.service.hibernate;

// java core
import java.io.Serializable;

// log4j
import org.apache.log4j.Logger;

//hibernate
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;


/**
 * The hibernate interceptor is used to do some pre and prost processing
 * on specific entities. 
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class EntityInterceptor extends EmptyInterceptor {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(EntityInterceptor.class);

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Reference to the descriptor cache
     */
    private IArchetypeDescriptorCache descriptorCache;
    

    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, 
            String[] propertyNames, Type[] types) {
        return super.onSave(entity, id, state, propertyNames, types);
    }

    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, 
            Object[] previousState, String[] propertyNames, Type[] types) {

        logger.error("onSave: entity=" + entity.toString());

        // after we save the entity
        if (entity instanceof ArchetypeDescriptor) {
            ArchetypeDescriptor adesc = (ArchetypeDescriptor)entity;
            if (descriptorCache != null) {
                descriptorCache.addArchetypeDescriptor(adesc, true);
            }
        }
        
        return false;
    }

    /**
     * @return Returns the descriptorCache.
     */
    public IArchetypeDescriptorCache getDescriptorCache() {
        return descriptorCache;
    }

    /**
     * @param descriptorCache The descriptorCache to set.
     */
    public void setDescriptorCache(IArchetypeDescriptorCache descriptorCache) {
        this.descriptorCache = descriptorCache;
    }
}
