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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im;

import org.openvpms.component.business.dao.hibernate.im.act.ActDeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.DefaultDeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.common.DeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDeleteHandler;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDeleteHandler;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;


/**
 * Factory for {@link DeleteHandler}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DeleteHandlerFactory {

    /**
     * Handler for {@link Act}s.
     */
    private final DeleteHandler act;

    /**
     * Handler for {@link Entity} instances.
     */
    private final DeleteHandler entity;

    /**
     * Handler for {@link Lookup} instances.
     */
    private final DeleteHandler lookup;

    /**
     * The default Handler.
     */
    private final DeleteHandler defaultHandler;

    /**
     * Creates a new <tt>DeleteHandlerFactory</tt>.
     *
     * @param assembler the assembler
     * @param archetypes the archetype descriptor cache
     */
    public DeleteHandlerFactory(CompoundAssembler assembler, IArchetypeDescriptorCache archetypes) {
        act = new ActDeleteHandler(assembler);
        entity = new EntityDeleteHandler(assembler);
        lookup = new LookupDeleteHandler(assembler, archetypes);
        defaultHandler = new DefaultDeleteHandler(assembler);
    }

    /**
     * Returns the appropriate handler for the supplied object.
     *
     * @param object the object
     * @return a handler for <tt>object</tt>
     */
    public DeleteHandler getHandler(IMObject object) {
        if (object instanceof Act) {
            return act;
        } else if (object instanceof Entity) {
            return entity;
        } else if (object instanceof Lookup) {
            return lookup;
        }
        return defaultHandler;
    }
}
