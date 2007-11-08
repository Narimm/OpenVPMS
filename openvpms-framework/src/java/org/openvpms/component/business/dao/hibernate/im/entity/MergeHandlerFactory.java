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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;


/**
 * Factory for {@link MergeHandler}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class MergeHandlerFactory {

    /**
     * Merge handler for {@link ArchetypeDescriptor}s.
     */
    private static final MergeHandler ARCHETYPE
            = new ArchetypeDescriptorMergeHandler();

    /**
     * Merge handler for {@link Act}s.
     */
    private static final MergeHandler ACT = new ActMergeHandler();

    /**
     * Merge handler for {@link Party} instances.
     */
    private static final MergeHandler PARTY = new PartyMergeHandler();

    /**
     * Merge handler for {@link Product} instances.
     */
    private static final MergeHandler PRODUCT = new ProductMergeHandler();

    /**
     * Merge handler for {@link Entity} instances.
     */
    private static final MergeHandler ENTITY = new EntityMergeHandler();

    /**
     * Merge handler for {@link Lookup} instances.
     */
    private static final MergeHandler LOOKUP = new LookupMergeHandler();

    /**
     * The default merge handler.
     */
    private static final MergeHandler DEFAULT = new DefaultMergeHandler();


    /**
     * Returns the appropriate handler for the supplied object.
     *
     * @param object the object
     * @return a handler for <tt>object</tt>
     */
    public static MergeHandler getHandler(IMObject object) {
        if (object instanceof Act) {
            return ACT;
        } else if (object instanceof Party) {
            return PARTY;
        } else if (object instanceof Product) {
            return PRODUCT;
        } else if (object instanceof Entity) {
            return ENTITY;
        } else if (object instanceof Lookup) {
            return LOOKUP;
        } else if (object instanceof ArchetypeDescriptor) {
            return ARCHETYPE;
        }
        return DEFAULT;

    }
}
