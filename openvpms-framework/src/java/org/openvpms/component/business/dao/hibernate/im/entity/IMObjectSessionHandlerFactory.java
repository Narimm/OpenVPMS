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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;


/**
 * Factory for {@link IMObjectSessionHandler}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class IMObjectSessionHandlerFactory {

    /**
     * Handler for {@link ArchetypeDescriptor}s.
     */
    private final IMObjectSessionHandler archetype;

    /**
     * Handler for {@link NodeDescriptor}s.
     */
    private final IMObjectSessionHandler node;

    /**
     * Handler for {@link Act}s.
     */
    private final IMObjectSessionHandler act;

    /**
     * Handler for {@link Party} instances.
     */
    private final IMObjectSessionHandler party;

    /**
     * Handler for {@link Product} instances.
     */
    private final IMObjectSessionHandler product;

    /**
     * Handler for {@link Entity} instances.
     */
    private final IMObjectSessionHandler entity;

    /**
     * Handler for {@link Lookup} instances.
     */
    private final IMObjectSessionHandler lookup;

    /**
     * The default Handler.
     */
    private final IMObjectSessionHandler defaultHandler;

    /**
     * Creates a new <tt>IMObjectSessionHandlerFactory</tt>.
     *
     * @param dao the DAO
     */
    public IMObjectSessionHandlerFactory(IMObjectDAO dao) {
        archetype = new ArchetypeDescriptorSessionHandler(dao);
        node = new NodeDescriptorSessionHandler(dao);
        act = new ActSessionHandler(dao);
        party = new PartySessionHandler(dao);
        product = new ProductSessionHandler(dao);
        entity = new EntitySessionHandler(dao);
        lookup = new LookupSessionHandler(dao);
        defaultHandler = new DefaultIMObjectSessionHandler(dao);
    }

    /**
     * Returns the appropriate handler for the supplied object.
     *
     * @param object the object
     * @return a handler for <tt>object</tt>
     */
    public IMObjectSessionHandler getHandler(IMObject object) {
        if (object instanceof Act) {
            return act;
        } else if (object instanceof Party) {
            return party;
        } else if (object instanceof Product) {
            return product;
        } else if (object instanceof Entity) {
            return entity;
        } else if (object instanceof Lookup) {
            return lookup;
        } else if (object instanceof ArchetypeDescriptor) {
            return archetype;
        } else if (object instanceof NodeDescriptor) {
            return node;
        }
        return defaultHandler;

    }
}
