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

package org.openvpms.plugin.service.archetype;

import org.openvpms.component.business.domain.bean.IMObjectBean;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.Collection;

/**
 * Archetype service for plugins.
 *
 * @author Tim Anderson
 */
public interface ArchetypeService {

    /**
     * Returns the {@link ArchetypeDescriptor} for the given archetype.
     *
     * @param archetype the archetype
     * @return the descriptor corresponding to the archetype, or {@code null} if none is found
     */
    ArchetypeDescriptor getArchetypeDescriptor(String archetype);

    /**
     * Returns a bean for an object.
     *
     * @param object the object
     * @return the bean
     */
    IMObjectBean getBean(IMObject object);

    /**
     * Create a domain object given its archetype.
     *
     * @param archetype the archetype name
     * @return a new object, or {@code null} if there is no corresponding archetype descriptor for {@code shortName}
     */
    IMObject create(String archetype);

    /**
     * Saves an object, executing any <em>save</em> rules associated with its archetype.
     *
     * @param object the object to save
     */
    void save(IMObject object);

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects the objects to insert or update
     */
    void save(Collection<? extends IMObject> objects);

    /**
     * Remove the specified object.
     *
     * @param object the object to remove
     */
    void remove(IMObject object);

    /**
     * Validates an object.
     *
     * @param object the object to validate
     */
    void validate(IMObject object);

    /**
     * Returns an object given its reference.
     *
     * @param reference the reference
     * @return the object, or {@code null} if none is found
     */
    IMObject get(IMObjectReference reference);
}
