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

package org.openvpms.web.component.im.filter;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Basic implementation of the {@link NodeFilter} interface, that enables hidden
 * and optional fields to be included or excluded.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BasicNodeFilter implements NodeFilter {

    /**
     * If <code>true</code> show optional fields, as well as mandatory ones.
     */
    private final boolean _showOptional;

    /**
     * If <code>true</code>, show hidden fields.
     */
    private final boolean _showHidden;


    /**
     * Construct a new <code>BasicNodeFilter</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     */
    public BasicNodeFilter(boolean showOptional) {
        this(showOptional, false);
    }

    /**
     * Construct a new <code>BasicNodeFilter</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     * @param showHidden   if <code>true</code> show hidden fields
     */
    public BasicNodeFilter(boolean showOptional, boolean showHidden) {
        _showOptional = showOptional;
        _showHidden = showHidden;
    }

    /**
     * Determines if optional fields should be displayed.
     *
     * @return <code>true</code> if optional fields should be displayed
     */
    public boolean showOptional() {
        return _showOptional;
    }

    /**
     * Determines if hidden fields should be displayed.
     *
     * @return <code>true</code> if hidden fields should be displayed
     */
    public boolean showHidden() {
        return _showHidden;
    }

    /**
     * Determines if a node should be included.
     *
     * @param descriptor the node descriptor
     * @param object     the object
     * @return <code>true</code> if the node should be included; otherwise
     *         <code>false</code>
     */
    public boolean include(NodeDescriptor descriptor, IMObject object) {
        boolean result = false;
        if (descriptor.isHidden()) {
            if (showHidden()) {
                result = true;
            }
        } else if (showOptional()) {
            result = true;
        } else {
            result = descriptor.isRequired();
        }
        return result;
    }
}
