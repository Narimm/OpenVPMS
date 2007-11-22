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

package org.openvpms.archetype.rules.product;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DefaultIMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;

/**
 * {@link IMObjectCopyHandler} that creates copies of products.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-12 04:10:40Z $
 */
public class ProductCopyHandler extends DefaultIMObjectCopyHandler {

    /**
     * Determines how {@link IMObjectCopier} should treat an object.
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <tt>object</tt> if the object shouldn't be copied,
     *         <tt>null</tt> if it should be replaced with
     *         <tt>null</tt>, or a new instance if the object should be
     *         copied
     */
	@Override
    public IMObject getObject(IMObject object, IArchetypeService service) {
		IMObject result;
        if ((object instanceof Entity && !(object instanceof Product))
                || object instanceof Lookup) {
	        result = object;
	    } else {
	       result = super.getObject(object, service);
	    }
	    return result;

    }
    /**
     * Helper to determine if a node is copyable.
     *
     * @param node   the node descriptor
     * @param source if <code>true</code> the node is the source; otherwise its
     *               the target
     * @return <tt>true</tt> if the node is copyable; otherwise <tt>false</tt>
     */
    @Override
    protected boolean isCopyable(NodeDescriptor node, boolean source) {
        return super.isCopyable(node, source);
    }

}
