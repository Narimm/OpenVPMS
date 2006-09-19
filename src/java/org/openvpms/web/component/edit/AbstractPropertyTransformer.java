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
 * PropertyTransformer is responsible for processing user input prior to it
 * being set on {@link Property}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractPropertyTransformer
        implements PropertyTransformer {

    /**
     * The parent object.
     */
    private final IMObject parent;

    /**
     * The node descriptor.
     */
    private final NodeDescriptor descriptor;


    /**
     * Construct a new <code>PropertyTransformer</code>.
     *
     * @param parent     the parent object
     * @param descriptor the node descriptor.
     */
    public AbstractPropertyTransformer(IMObject parent,
                                       NodeDescriptor descriptor) {
        this.parent = parent;
        this.descriptor = descriptor;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object
     */
    public IMObject getParent() {
        return parent;
    }

    /**
     * Returns the node descriptor.
     *
     * @return the node descriptor
     */
    public NodeDescriptor getDescriptor() {
        return descriptor;
    }

}
