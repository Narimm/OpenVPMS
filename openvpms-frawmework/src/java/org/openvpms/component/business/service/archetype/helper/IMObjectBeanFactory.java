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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Factory for {@link IMObjectBean}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectBeanFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs an <tt>IMObjectBeanFactory</tt>.
     *
     * @param service the archetype service
     */
    public IMObjectBeanFactory(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Creates an {@link IMObjectBean} for an object.
     *
     * @param object the object
     * @return a new {@link IMObjectBean} for the object
     */
    public IMObjectBean createBean(IMObject object) {
        return new IMObjectBean(object, service);
    }

    /**
     * Creates an object with the specified short name, and wraps it in an {@link IMObjectBean}.
     *
     * @param shortName the archetype short name
     * @return an {@link IMObjectBean} wrapping the new object
     * @throws ArchetypeServiceException if the object can't be created
     * @throws IMObjectBeanException     if the archetype short name is invalid
     */
    public IMObjectBean createBean(String shortName) {
        IMObject object = service.create(shortName);
        if (object == null) {
            throw new IMObjectBeanException(IMObjectBeanException.ErrorCode.ArchetypeNotFound, shortName);
        }
        return createBean(object);
    }

    /**
     * Retrieves the object with the specified reference, and wraps it in an {@link IMObjectBean}.
     *
     * @param reference the object reference
     * @return an {@link IMObjectBean} wrapping the retrieved object, or <tt>null</tt> if the object couldn't be
     *         retrieved
     * @throws ArchetypeServiceException if the query fails
     */
    public IMObjectBean createBean(IMObjectReference reference) {
        IMObject object = service.get(reference);
        return (object != null) ? createBean(object) : null;
    }

    /**
     * Creates an {@link ActBean} for an act.
     *
     * @param act the act
     * @return a new {@link ActBean} for the act
     */
    public ActBean createActBean(Act act) {
        return new ActBean(act, service);
    }

    /**
     * Creates an act with the specified short name, and wraps it in an {@link ActBean}.
     *
     * @param shortName the act archetype short name
     * @return an {@link ActBean} wrapping the new act
     * @throws ArchetypeServiceException if the object can't be created
     * @throws IMObjectBeanException     if the archetype short name is invalid
     */
    public ActBean createActBean(String shortName) {
        Act object = (Act) service.create(shortName);
        if (object == null) {
            throw new IMObjectBeanException(IMObjectBeanException.ErrorCode.ArchetypeNotFound, shortName);
        }
        return createActBean(object);
    }

    /**
     * Retrieves the act with the specified reference, and wraps it in an {@link ActBean}.
     *
     * @param reference the object reference
     * @return an {@link ActBean} wrapping the retrieved object, or <tt>null</tt> if the object couldn't be retrieved
     * @throws ArchetypeServiceException if the query fails
     * @throws ClassCastException if the retrieved object isn't an <tt>Act</tt>
     */
    public ActBean createActBean(IMObjectReference reference) {
        Act object = (Act) service.get(reference);
        return (object != null) ? createActBean(object) : null;
    }

    /**
     * Creates an {@link EntityBean} for an entity.
     *
     * @param entity the entity
     * @return a new {@link ActBean} for the entity
     */
    public EntityBean createEntityBean(Entity entity) {
        return new EntityBean(entity, service);
    }

    /**
     * Creates an entity with the specified short name, and wraps it in an {@link EntityBean}.
     *
     * @param shortName the entity archetype short name
     * @return an {@link EntityBean} wrapping the new entity
     * @throws ArchetypeServiceException if the object can't be created
     * @throws IMObjectBeanException     if the archetype short name is invalid
     */
    public EntityBean createEntityBean(String shortName) {
        Entity object = (Entity) service.create(shortName);
        if (object == null) {
            throw new IMObjectBeanException(IMObjectBeanException.ErrorCode.ArchetypeNotFound, shortName);
        }
        return createEntityBean(object);
    }

    /**
     * Retrieves the act with the specified reference, and wraps it in an {@link ActBean}.
     *
     * @param reference the object reference
     * @return an {@link EntityBean} wrapping the retrieved object, or <tt>null</tt> if the object couldn't be retrieved
     * @throws ArchetypeServiceException if the query fails
     * @throws ClassCastException if the retrieved object isn't an <tt>Entity</tt>
     */
    public EntityBean createEntityBean(IMObjectReference reference) {
        Entity object = (Entity) service.get(reference);
        return (object != null) ? createEntityBean(object) : null;
    }
}
