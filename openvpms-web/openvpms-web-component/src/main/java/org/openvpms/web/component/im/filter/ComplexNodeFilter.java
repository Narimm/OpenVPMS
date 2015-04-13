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

package org.openvpms.web.component.im.filter;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Complex implementation of the {@link NodeFilter} interface, that enables hidden
 * fields and optional complex fields to be included or excluded.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ComplexNodeFilter extends BasicNodeFilter {

    /**
     * Construct a new <code>BasicNodeFilter</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     */
    public ComplexNodeFilter(boolean showOptional) {
        super(showOptional);
    }

    /**
     * Construct a new <code>BasicNodeFilter</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     * @param showHidden   if <code>true</code> show hidden fields
     */
    public ComplexNodeFilter(boolean showOptional, boolean showHidden) {
        super(showOptional, showHidden);
    }

    /**
     * Determines if a node should be included.
     *
     * @param descriptor the node descriptor
     * @param object     the object
     * @return <code>true</code> if the node should be included; otherwise
     *         <code>false</code>
     */
    @Override
    public boolean include(NodeDescriptor descriptor, IMObject object) {
        boolean result = false;
        if (descriptor.isHidden()) {
            if (showHidden()) {
                result = true;
            }
        } else if (showOptional()) {
            result = true;
        } else if (!descriptor.isComplexNode()) {
            result = true;
        } else {
            result = descriptor.isRequired();
        }
        return result;
    }

}
