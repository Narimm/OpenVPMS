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

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.AbstractPropertySet;
import org.openvpms.component.system.common.util.PropertySetException;
import static org.openvpms.component.system.common.util.PropertySetException.ErrorCode.PropertyNotFound;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Set of nodes associated with an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeSet extends AbstractPropertySet implements Serializable {

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * A reference to the object that the nodes belong to.
     */
    private IMObjectReference reference;

    /**
     * The nodes.
     */
    private Map<String, Object> nodes = new HashMap<String, Object>();


    /**
     * Constructs a new <code>NodeSet</code>.
     *
     * @param reference a reference to the object that the nodes belong to.
     */
    public NodeSet(IMObjectReference reference) {
        this.reference = reference;
    }

    /**
     * Constructs a new <code>NodeSet</code>. This is provided for serialization
     * purposes.
     */
    protected NodeSet() {
    }

    /**
     * Returns a reference to the object that the nodes belong to.
     *
     * @return the object reference
     */
    public IMObjectReference getObjectReference() {
        return reference;
    }

    /**
     * Returns the node names.
     *
     * @return the node names
     */
    public Set<String> getNames() {
        return nodes.keySet();
    }

    /**
     * Sets the value of a property.
     *
     * @param name  the propery name
     * @param value the property value
     * @throws OpenVPMSException if the property cannot be set
     */
    public void set(String name, Object value) {
        nodes.put(name, value);
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws PropertySetException if the property doesn't exist
     */
    public Object get(String name) {
        if (nodes.containsKey(name)) {
            return nodes.get(name);
        }
        throw new PropertySetException(PropertyNotFound, name);
    }


}
