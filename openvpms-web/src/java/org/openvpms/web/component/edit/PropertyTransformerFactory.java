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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Factory for {@link AbstractPropertyTransformer} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PropertyTransformerFactory {

    /**
     * Creates a new property transformer.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor
     */
    public static PropertyTransformer create(IMObject parent,
                                             NodeDescriptor descriptor) {
        PropertyTransformer result;
        if (descriptor.isString()) {
            result = new StringPropertyTransformer(parent, descriptor);
        } else if (descriptor.isMoney()) {
            result = new MoneyPropertyTransformer(descriptor);
        } else if (descriptor.isNumeric()) {
            result = new NumericPropertyTransformer(descriptor);
        } else {
            result = new DefaultPropertyTransformer();
        }
        return result;
    }
}
