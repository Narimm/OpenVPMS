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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Represents a property of an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class IMObjectProperty implements Property, CollectionProperty {

    /**
     * The object that the property belongs to.
     */
    private final IMObject _object;

    /**
     * The property descriptor.
     */
    private final NodeDescriptor _descriptor;


    /**
     * Construct a new <code>IMObjectProperty</code>.
     *
     * @param object     the object that the property belongs t
     * @param descriptor the property descriptor
     */
    public IMObjectProperty(IMObject object, NodeDescriptor descriptor) {
        _object = object;
        _descriptor = descriptor;
    }

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    public Object getValue() {
        return getDescriptor().getValue(getObject());
    }

    /**
     * Returns the collection.
     *
     * @return the collection
     */
    public Collection getValues() {
        NodeDescriptor descriptor = getDescriptor();
        List<IMObject> values = descriptor.getChildren(getObject());
        if (values != null) {
            values = Collections.unmodifiableList(values);
        }
        return values;
    }

    /**
     * Returns the property descriptor.
     *
     * @return the property descriptor
     */
    public NodeDescriptor getDescriptor() {
        return _descriptor;
    }

    /**
     * Returns the object that the property belongs to.
     *
     * @return the object
     */
    protected IMObject getObject() {
        return _object;
    }

}
