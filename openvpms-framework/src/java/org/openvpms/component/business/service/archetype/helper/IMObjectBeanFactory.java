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
     * Constructs a <tt>IMObjectBeanFactory</tt>.
     *
     * @param service the archetype service
     */
    public IMObjectBeanFactory(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Creates a new {@link IMObjectBean} for an object.
     *
     * @param object the object
     * @return a new {@link IMObjectBean} for the object
     */
    public IMObjectBean create(IMObject object) {
        return new IMObjectBean(object, service);
    }

    /**
     * Creates a new {@link ActBean} for an act.
     *
     * @param act the act
     * @return a new {@link ActBean} for the act
     */
    public ActBean create(Act act) {
        return new ActBean(act, service);
    }

    /**
     * Creates a new {@link EntityBean} for an entity.
     *
     * @param entity the entity
     * @return a new {@link ActBean} for the entity
     */
    public EntityBean create(Entity entity) {
        return new EntityBean(entity, service);
    }

}
